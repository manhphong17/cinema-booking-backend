package vn.cineshow.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vn.cineshow.config.SecurityAuditor;
import vn.cineshow.dto.response.order.OrderCheckTicketResponse;
import vn.cineshow.dto.response.order.OrderResponse;
import vn.cineshow.enums.OrderStatus;
import vn.cineshow.exception.AppException;
import vn.cineshow.exception.ErrorCode;
import vn.cineshow.model.*;
import vn.cineshow.repository.OrderRepository;
import vn.cineshow.service.OrderQueryService;
import vn.cineshow.service.QrTokenService;
import vn.cineshow.utils.validator.OwnershipValidator;
import vn.cineshow.utils.validator.QrPolicy;

import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
@Slf4j(topic = "ORDER-QUERY-SERVICE")
public class OrderQueryServiceImpl implements OrderQueryService {

    private static final Duration RESEND_DEBOUNCE = Duration.ofSeconds(60);
    private final OrderRepository orderRepository;       // <- đúng là 'private final'
    private final OwnershipValidator ownershipValidator;
    private final QrPolicy qrPolicy;
    private final QrTokenService qrTokenService;
    private final EmailServiceImpl emailService;
    private final SecurityAuditor securityAuditor;
    private final ConcurrentHashMap<String, Instant> resendDebounceMap = new ConcurrentHashMap<>();



    private String safeMovieName(Order order) {
        Ticket t = pickPrimaryTicket(order);
        if (t == null) return null;
        ShowTime st = t.getShowTime();
        return st != null && st.getMovie() != null ? st.getMovie().getName() : null;
    }

    private LocalDateTime resolveShowtimeStart(Order order) {
        Ticket t = pickPrimaryTicket(order);
        return t != null && t.getShowTime() != null ? t.getShowTime().getStartTime() : null;
    }

    private String safeRoomName(Order order) {
        Ticket t = pickPrimaryTicket(order);
        if (t == null) return null;
        ShowTime st = t.getShowTime();
        Room room = st != null ? st.getRoom() : null;
        return room != null ? room.getName() : null;
    }

    private String safeSeatLabel(Ticket t) {
        if (t == null || t.getSeat() == null) return null;
        Seat s = t.getSeat();
        String row = s.getRow();
        String col = s.getColumn();
        return (row != null ? row : "") + (col != null ? col : "");
    }

    private List<String> safeSeatLabels(Order order) {
        if (order.getTickets() == null) return List.of();
        return order.getTickets().stream()
                .map(this::safeSeatLabel)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());
    }

    private Ticket pickPrimaryTicket(Order order) {
        if (order == null || order.getTickets() == null || order.getTickets().isEmpty()) return null;
        return order.getTickets().stream()
                .min(Comparator.comparing(t -> {
                    Seat s = t.getSeat();
                    String label = s != null ? (Objects.toString(s.getRow(), "") + Objects.toString(s.getColumn(), "")) : "";
                    return label;
                }))
                .orElse(null);
    }

    private boolean isQrExpired(LocalDateTime start, LocalDateTime now) {
        if (start == null) return true;
        return qrPolicy.isExpired(start, now);
    }

    private boolean canRegenerate(boolean paid, boolean canceled, LocalDateTime start, LocalDateTime now) {
        if (!paid || canceled || start == null) return false;
        return !qrPolicy.isExpired(start, now);
    }

    private String generateQrToken(Order order) {
        Ticket t = pickPrimaryTicket(order);
        if (t == null || t.getShowTime() == null) return null;
        Long showtimeId = t.getShowTime().getId();
        List<Long> seatIds = order.getTickets() == null ? List.of() : order.getTickets().stream()
                .map(Ticket::getSeat)
                .filter(Objects::nonNull)
                .map(Seat::getId)
                .collect(Collectors.toList());
        return qrTokenService.generateToken(order.getId(), showtimeId, seatIds, qrPolicy.graceMinutes(), "v1");
    }

    // endregion
    @Override
    public Map<String, Object> getOrdersByStatus(String status, LocalDate date, int page, int size) {
        if (date == null) date = LocalDate.now();

        LocalDateTime startOfDay = date.atStartOfDay(); // 2025-11-05T00:00
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX); // 2025-11-05T23:59:59.999999999

        OrderStatus filter = null;
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            filter = OrderStatus.valueOf(status.toUpperCase());
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orders = orderRepository.findAllByStatusAndDateRange(filter, startOfDay, endOfDay, pageable);

        Page<OrderResponse> orderDTOs = orders.map(this::mapToDTO);
        Map<String, Object> summary = getDailySummary(date);

        Map<String, Object> response = new HashMap<>();
        response.put("orders", orderDTOs);
        response.put("summary", summary);
        return response;
    }


    @Override
    public Map<String, Object> getDailySummary(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRevenueToday",
                Optional.ofNullable(orderRepository.getRevenueByDate(startOfDay, endOfDay)).orElse(0.0));
        summary.put("totalTicketsSold",
                Optional.ofNullable(orderRepository.getTicketsByDate(startOfDay, endOfDay)).orElse(0L));
        summary.put("totalCompletedOrders",
                Optional.ofNullable(orderRepository.getCompletedOrdersByDate(startOfDay, endOfDay)).orElse(0L));
        summary.put("totalConcessionsSold",
                Optional.ofNullable(orderRepository.getConcessionSoldByDate(startOfDay, endOfDay)).orElse(0L));

        return summary;
    }

    @Override
    public OrderCheckTicketResponse checkTicketByOrderCode(String orderCode) {
        Order order = orderRepository.findByCodeWithTickets(orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // Map tickets to TicketInfo
        List<OrderCheckTicketResponse.TicketInfo> ticketInfos = order.getTickets() == null ? List.of() :
                order.getTickets().stream()
                        .map(ticket -> {
                            Seat seat = ticket.getSeat();
                            ShowTime showTime = ticket.getShowTime();
                            Movie movie = showTime != null ? showTime.getMovie() : null;
                            Room room = showTime != null ? showTime.getRoom() : null;

                            String seatCode = (seat != null && seat.getRow() != null && seat.getColumn() != null)
                                    ? seat.getRow() + seat.getColumn() : null;
                            String seatType = (seat != null && seat.getSeatType() != null)
                                    ? seat.getSeatType().getName() : null;
                            Double ticketPrice = ticket.getPriceSnapshot() != null
                                    ? ticket.getPriceSnapshot()
                                    : (ticket.getTicketPrice() != null ? ticket.getTicketPrice().getPrice() : null);

                            return OrderCheckTicketResponse.TicketInfo.builder()
                                    .ticketId(ticket.getId())
                                    .seatCode(seatCode)
                                    .seatType(seatType)
                                    .ticketPrice(ticketPrice)
                                    .showtimeId(showTime != null ? showTime.getId() : null)
                                    .showtimeStart(showTime != null ? showTime.getStartTime() : null)
                                    .showtimeEnd(showTime != null ? showTime.getEndTime() : null)
                                    .movieName(movie != null ? movie.getName() : null)
                                    .posterUrl(movie != null ? movie.getPosterUrl() : null)
                                    .roomName(room != null ? room.getName() : null)
                                    .build();
                        })
                        .collect(Collectors.toList());

        return OrderCheckTicketResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getCode())
                .createdAt(order.getCreatedAt())
                .userName(order.getUser() != null ? order.getUser().getName() : null)
                .totalPrice(order.getTotalPrice())
                .orderStatus(order.getOrderStatus() != null ? order.getOrderStatus().name() : null)
                .ticketCount(ticketInfos.size())
                .tickets(ticketInfos)
                .build();
    }


    private OrderResponse mapToDTO(Order o) {
        return OrderResponse.builder()
                .id(o.getId())
                .code(o.getCode())
                .customerName(o.getUser() != null ? o.getUser().getName() : "Khách tại quầy")
                .createdAt(o.getCreatedAt())
                .totalPrice(o.getTotalPrice() != null ? o.getTotalPrice() : 0.0)
                .status(o.getOrderStatus().name())
                .paymentMethod(
                        o.getPayment() != null ? o.getPayment().getMethod().getMethodName() : "N/A"
                )
                .build();
    }
}
