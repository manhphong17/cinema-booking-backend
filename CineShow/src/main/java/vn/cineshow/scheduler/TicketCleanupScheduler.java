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


    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void cleanupExpiredTickets() {
        log.info("[TICKET CLEANUP] Start clean ticket AVAILABLE/BLOCKED...");
        
        LocalDate thresholdDate = LocalDate.now();
        
        List<TicketStatus> statusesToClean = List.of(TicketStatus.AVAILABLE, TicketStatus.BLOCKED);
        
        List<Ticket> ticketsToDelete = ticketRepository.findAvailableOrBlockedTicketsAfterShowtimeDate(
                statusesToClean,
                thresholdDate
        );
        
        ticketRepository.deleteAll(ticketsToDelete);
        
        log.info("[TICKET CLEANUP] Date threshold: {}", thresholdDate);
    }
}

