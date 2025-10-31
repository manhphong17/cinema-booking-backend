package vn.cineshow.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import vn.cineshow.dto.request.order.QrRegenerateRequest;
import vn.cineshow.dto.response.order.OrderDetailResponse;
import vn.cineshow.dto.response.order.OrderListItemResponse;
import vn.cineshow.dto.response.order.OrderListResponse;
import vn.cineshow.dto.response.order.ResendEmailResponse;
import vn.cineshow.enums.OrderStatus;
import vn.cineshow.exception.AppException;
import vn.cineshow.exception.ErrorCode;
import vn.cineshow.config.SecurityAuditor;
import vn.cineshow.model.Order;
import vn.cineshow.model.Room;
import vn.cineshow.model.Seat;
import vn.cineshow.model.ShowTime;
import vn.cineshow.model.Ticket;
import vn.cineshow.model.Account;
import vn.cineshow.repository.OrderRepository;
import vn.cineshow.service.OrderQueryService;
import vn.cineshow.service.QrTokenService;
import vn.cineshow.utils.validator.OwnershipValidator;
import vn.cineshow.utils.validator.QrPolicy;

import java.time.LocalDateTime;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
@Slf4j(topic = "ORDER-QUERY-SERVICE")
public class OrderQueryServiceImpl implements OrderQueryService {

    private final OrderRepository orderRepository;       // <- đúng là 'private final'
    private final OwnershipValidator ownershipValidator;
    private final QrPolicy qrPolicy;
    private final QrTokenService qrTokenService;
    private final EmailServiceImpl emailService;
    private final SecurityAuditor securityAuditor;

    private static final Duration RESEND_DEBOUNCE = Duration.ofSeconds(60);
    private final ConcurrentHashMap<String, Instant> resendDebounceMap = new ConcurrentHashMap<>();


    @Override
    public OrderListResponse myOrders(Long currentUserId, Pageable pageable) {
        Page<Order> page = orderRepository.findByUser_IdOrderByCreatedAtDesc(currentUserId, pageable);
        List<OrderListItemResponse> items = page.getContent().stream().map(o -> {
            String movie = safeMovieName(o);
            LocalDateTime start = resolveShowtimeStart(o);
            String room = safeRoomName(o);
            List<String> seats = o.getTickets() == null ? List.of()
                    : o.getTickets().stream().map(this::safeSeatLabel).toList();

            return OrderListItemResponse.builder()
                    .orderId(o.getId())
                    .createdAt(o.getCreatedAt())
                    .movieName(movie)
                    .showtimeStart(start)
                    .roomName(room)
                    .seats(seats)
                    .totalPrice(o.getTotalPrice())
                    .status(o.getOrderStatus().name())
                    .build();
        }).toList();

        return OrderListResponse.builder()
                .items(items)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }


    @Override
    public OrderDetailResponse getOrderDetail(Long orderId, Long currentUserId) {
        ownershipValidator.mustOwnOrder(orderId, currentUserId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        Ticket primary = pickPrimaryTicket(order);
        LocalDateTime start = primary != null && primary.getShowTime() != null ? primary.getShowTime().getStartTime() : null;
        boolean paid = order.getOrderStatus() == OrderStatus.COMPLETED;
        boolean canceled = order.getOrderStatus() == OrderStatus.CANCELED;
        LocalDateTime now = LocalDateTime.now();
        boolean expired = isQrExpired(start, now);
        boolean available = paid && !canceled && start != null && !expired;
        boolean regenerateAllowed = canRegenerate(paid, canceled, start, now);

        String token = available ? generateQrToken(order) : null;

        return OrderDetailResponse.builder()
                .orderId(order.getId())
                .bookingCode(primary != null ? primary.getCode() : null)
                .movieName(safeMovieName(order))
                .roomName(safeRoomName(order))
                .showtimeStart(start)
                .seats(safeSeatLabels(order))
                .totalPrice(order.getTotalPrice())
                .orderStatus(order.getOrderStatus() != null ? order.getOrderStatus().name() : null)
                .qrAvailable(available)
                .qrExpired(expired)
                .regenerateAllowed(regenerateAllowed)
                .qrJwt(token)
                .qrImageUrl(null)
                .graceMinutes(qrPolicy.graceMinutes())
                .build();
    }

    @Override
    public OrderDetailResponse regenerateQr(Long orderId, Long currentUserId, QrRegenerateRequest req) {
        ownershipValidator.mustOwnOrder(orderId, currentUserId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getOrderStatus() == OrderStatus.CANCELED) {
            throw new AppException(ErrorCode.ORDER_CANCELED);
        }
        if (order.getOrderStatus() != OrderStatus.COMPLETED) {
            throw new AppException(ErrorCode.ORDER_NOT_PAID);
        }

        Ticket primary = pickPrimaryTicket(order);
        LocalDateTime start = primary != null && primary.getShowTime() != null ? primary.getShowTime().getStartTime() : null;
        if (start == null) {
            throw new AppException(ErrorCode.SHOWTIME_NOT_FOUND);
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(start)) {
            throw new AppException(ErrorCode.SHOWTIME_PASSED);
        }
        boolean expired = isQrExpired(start, now);
        if (expired) {
            // within expired window but before show start+grace → still allow regeneration
            if (qrPolicy.isExpired(start, now)) {
                throw new AppException(ErrorCode.QR_EXPIRED);
            }
        }

        String token = generateQrToken(order);

        return OrderDetailResponse.builder()
                .orderId(order.getId())
                .bookingCode(primary != null ? primary.getCode() : null)
                .movieName(safeMovieName(order))
                .roomName(safeRoomName(order))
                .showtimeStart(start)
                .seats(safeSeatLabels(order))
                .totalPrice(order.getTotalPrice())
                .orderStatus(order.getOrderStatus() != null ? order.getOrderStatus().name() : null)
                .qrAvailable(true)
                .qrExpired(false)
                .regenerateAllowed(true)
                .qrJwt(token)
                .qrImageUrl(null)
                .graceMinutes(qrPolicy.graceMinutes())
                .build();
    }

    @Override
    public byte[] buildEticketPdf(Long orderId, Long currentUserId) {
        ownershipValidator.mustOwnOrder(orderId, currentUserId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        if (order.getOrderStatus() != OrderStatus.COMPLETED) {
            throw new AppException(ErrorCode.ORDER_NOT_PAID);
        }
        // Placeholder implementation: return empty PDF bytes
        return new byte[0];
    }

    @Override
    public void resendEmail(Long orderId, Long currentUserId) {
        ownershipValidator.mustOwnOrder(orderId, currentUserId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        if (order.getOrderStatus() != OrderStatus.COMPLETED) {
            throw new AppException(ErrorCode.ORDER_NOT_PAID);
        }
        // No-op placeholder. Integrate with email service if available.
    }

    @Override
    public ResendEmailResponse resendETicket(Long orderId, String toEmail, String lang, Authentication auth) {
        Long currentUserId = null;
        if (auth != null && auth.getPrincipal() instanceof Account acc) {
            if (acc.getUser() != null) {
                currentUserId = acc.getUser().getId();
            }
        }
        if (currentUserId == null) {
            currentUserId = securityAuditor.currentUserIdOrThrow();
        }
        ownershipValidator.mustOwnOrder(orderId, currentUserId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        if (order.getOrderStatus() != OrderStatus.COMPLETED) {
            throw new AppException(ErrorCode.ORDER_NOT_PAID);
        }

        String fallbackEmail = (order.getUser() != null && order.getUser().getAccount() != null)
                ? order.getUser().getAccount().getEmail()
                : null;
        String recipient = (toEmail != null && !toEmail.isBlank()) ? toEmail : fallbackEmail;
        if (recipient == null || recipient.isBlank()) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_FOUND);
        }

        String language = (lang == null || lang.isBlank()) ? "vi" : lang;

        String debounceKey = orderId + ":" + recipient.toLowerCase();
        Instant now = Instant.now();
        Instant last = resendDebounceMap.get(debounceKey);
        if (last != null && Duration.between(last, now).compareTo(RESEND_DEBOUNCE) < 0) {
            return ResendEmailResponse.builder()
                    .orderId(orderId)
                    .toEmail(recipient)
                    .language(language)
                    .queuedAt(now)
                    .message("Too many requests. Please try again later.")
                    .build();
        }

        resendDebounceMap.put(debounceKey, now);

        String subject = switch (language) {
            case "en" -> "Your E-ticket";
            default -> "Vé điện tử của bạn";
        };
        String body = switch (language) {
            case "en" -> "Your e-ticket for order " + order.getCode() + " has been resent.";
            default -> "Vé điện tử cho đơn hàng " + order.getCode() + " đã được gửi lại.";
        };

        emailService.send(recipient, subject, body);
        log.info("resend_e_ticket orderId={} to={}", orderId, recipient);

        return ResendEmailResponse.builder()
                .orderId(orderId)
                .toEmail(recipient)
                .language(language)
                .queuedAt(now)
                .message("queued")
                .build();
    }

    // region helpers

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
}
