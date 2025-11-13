package vn.cineshow.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.cineshow.enums.TicketStatus;
import vn.cineshow.model.Ticket;
import vn.cineshow.repository.TicketRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketCleanupScheduler {

    private final TicketRepository ticketRepository;

    /**
     * Scheduler tự động dọn vé ở trạng thái AVAILABLE hoặc BLOCKED
     * vào 1h sáng của ngày sau ngày suất chiếu
     * 
     * Cron: "0 0 1 * * *" = Chạy lúc 1:00 AM mỗi ngày
     * Zone: Asia/Ho_Chi_Minh
     */
    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void cleanupExpiredTickets() {
        log.info("[TICKET CLEANUP] Bắt đầu dọn vé AVAILABLE/BLOCKED sau ngày suất chiếu...");
        
        // Lấy ngày hôm qua (ngày sau ngày suất chiếu)
        // Nếu hôm nay là ngày 13, thì dọn vé của showtime kết thúc trước ngày 13 (tức là ngày 12 trở về trước)
        LocalDate thresholdDate = LocalDate.now();
        
        List<TicketStatus> statusesToClean = List.of(TicketStatus.AVAILABLE, TicketStatus.BLOCKED);
        
        List<Ticket> ticketsToDelete = ticketRepository.findAvailableOrBlockedTicketsAfterShowtimeDate(
                statusesToClean,
                thresholdDate
        );
        
        if (ticketsToDelete.isEmpty()) {
            log.info("[TICKET CLEANUP] Không có vé nào cần dọn.");
            return;
        }
        
        int deletedCount = ticketsToDelete.size();
        
        // Xóa vé
        ticketRepository.deleteAll(ticketsToDelete);
        
        log.info("[TICKET CLEANUP] Đã xóa {} vé (AVAILABLE/BLOCKED) của các showtime đã qua ngày suất chiếu.", deletedCount);
        log.info("[TICKET CLEANUP] Ngày threshold: {}", thresholdDate);
    }
}

