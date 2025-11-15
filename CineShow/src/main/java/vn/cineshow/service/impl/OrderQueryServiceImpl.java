package vn.cineshow.service.impl;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.cineshow.config.SecurityAuditor;
import vn.cineshow.dto.response.order.OrderCheckTicketResponse;
import vn.cineshow.dto.response.order.OrderResponse;
import vn.cineshow.enums.OrderStatus;
import vn.cineshow.exception.AppException;
import vn.cineshow.exception.ErrorCode;
import vn.cineshow.model.Movie;
import vn.cineshow.model.Order;
import vn.cineshow.model.Room;
import vn.cineshow.model.Seat;
import vn.cineshow.model.ShowTime;
import vn.cineshow.model.Ticket;
import vn.cineshow.repository.OrderRepository;
import vn.cineshow.service.OrderQueryService;
import vn.cineshow.service.QrTokenService;
import vn.cineshow.utils.validator.OwnershipValidator;
import vn.cineshow.utils.validator.QrPolicy;


@Service
@AllArgsConstructor
@Slf4j(topic = "ORDER-QUERY-SERVICE")
public class OrderQueryServiceImpl implements OrderQueryService {

    private final OrderRepository orderRepository;

    private String safeSeatLabel(Ticket t) {
        if (t == null || t.getSeat() == null) return null;
        Seat s = t.getSeat();
        String row = s.getRow();
        String col = s.getColumn();

        int rowNumber = Integer.parseInt(row);
        int colNumber = Integer.parseInt(col);

        String rowLetter = convertNumberToLetter(rowNumber);
        
        return rowLetter + colNumber;
    }
    
    private String convertNumberToLetter(int number) {
        if (number < 1) return null;
        
        StringBuilder result = new StringBuilder();
        
        while (number > 0) {
            number--;
            result.insert(0, (char)('A' + (number % 26)));
            number = number / 26;
        }
        
        return result.toString();
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

        List<OrderCheckTicketResponse.TicketInfo> ticketInfos = order.getTickets() == null ? List.of() :
                order.getTickets().stream()
                        .map(ticket -> {
                            Seat seat = ticket.getSeat();
                            ShowTime showTime = ticket.getShowTime();
                            Movie movie = showTime != null ? showTime.getMovie() : null;
                            Room room = showTime != null ? showTime.getRoom() : null;

                            String seatCode = safeSeatLabel(ticket);
                            if (seatCode == null || seatCode.isEmpty()) {
                                throw new AppException(ErrorCode.TICKET_NOT_FOUND);
                            }

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
                .isCheckIn(order.getIsCheckIn() != null ? order.getIsCheckIn() : false)
                .tickets(ticketInfos)
                .build();
    }

    @Override
    @Transactional
    public OrderCheckTicketResponse checkInOrder(Long orderId) {
        Order order = orderRepository.findOneById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getOrderStatus() != OrderStatus.COMPLETED) {
            throw new AppException(ErrorCode.ORDER_NOT_COMPLETED);
        }

        if (Boolean.TRUE.equals(order.getIsCheckIn())) {
            throw new AppException(ErrorCode.ORDER_ALREADY_CHECKED_IN);
        }

        order.setIsCheckIn(true);
        orderRepository.save(order);

        return checkTicketByOrderCode(order.getCode());
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
