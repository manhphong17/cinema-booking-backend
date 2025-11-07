package vn.cineshow.scheduler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.cineshow.enums.OrderStatus;
import vn.cineshow.enums.PaymentStatus;
import vn.cineshow.model.Order;
import vn.cineshow.model.Payment;
import vn.cineshow.model.Ticket;
import vn.cineshow.repository.OrderRepository;
import vn.cineshow.repository.PaymentRepository;
import vn.cineshow.repository.TicketRepository;

import java.time.LocalDateTime;
import java.util.List;
@Slf4j
@Component
@RequiredArgsConstructor
class Order_PaymentCleanupScheduler {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;

    // Test
//    @Scheduled(fixedDelay = 120000)

    //  Chạy mỗi ngày lúc 1h sáng
    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Ho_Chi_Minh")
    public void cleanOldPendingOrders() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        List<Order> oldOrders = orderRepository.findPendingBefore(threshold);

        if (oldOrders.isEmpty()) {
            log.info("[CronJob] Không có Order Pending nào quá 24h.");
            return;
        }

        for (Order order : oldOrders) {
            Payment payment = order.getPayment();

            order.setOrderStatus(OrderStatus.CANCELED);
            order.setUpdatedAt(LocalDateTime.now());

            if (payment != null) {
                payment.setPaymentStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
            }

            orderRepository.save(order);
            log.info("[CronJob]  Dọn Order Pending {} do Pending quá 24h.", order.getCode());
        }

        log.info("[CronJob]  Đã dọn {} Order Pending quá hạn.", oldOrders.size());
    }
}
