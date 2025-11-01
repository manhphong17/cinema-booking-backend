package vn.cineshow.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cineshow.dto.redis.ConcessionOrderRequest;
import vn.cineshow.dto.redis.OrderSessionDTO;
import vn.cineshow.dto.redis.OrderSessionRequest;
import vn.cineshow.enums.OrderSessionStatus;
import vn.cineshow.repository.TicketRepository;
import vn.cineshow.service.OrderSessionService;
import vn.cineshow.service.RedisService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrderSessionServiceImpl implements OrderSessionService {

    private final RedisService redisService;
    private final TicketRepository ticketRepository;

    @Value("${booking.ttl.default}")
    private long TTL_DEFAULT; //600

    @Value("${booking.ttl.payment}")
    private long PAYMENT_EXTENSION; //1200

    private String getKey(Long userId, Long showtimeId) {
        return String.format("orderSession:showtime:%d:userId:%d", showtimeId, userId);
    }

    @Override
    public void createOrUpdate(OrderSessionRequest request) {
        String key = getKey(request.getUserId(), request.getShowtimeId());

        //get order session in redis, if not exist-> create, else -> update
        OrderSessionDTO orderExist = redisService.get(key, OrderSessionDTO.class);

        boolean isExist = orderExist != null;

        if (!isExist) {
            orderExist = new OrderSessionDTO();
            orderExist.setCreatedAt(LocalDateTime.now());
            orderExist.setExpiredAt(LocalDateTime.now().plusSeconds(TTL_DEFAULT));
            orderExist.setStatus(OrderSessionStatus.PENDING);
            orderExist.setUserId(request.getUserId());
            orderExist.setShowtimeId(request.getShowtimeId());
            log.info("[ORDER_SESSION] [CREATE] key = {}", key);
        }

        orderExist.setTicketIds(request.getTicketIds());


        //count total amount
        double totalPrice = 0.0;
        for (int i = 0; i < request.getTicketIds().size(); i++) {
            totalPrice += ticketRepository.findById(request.getTicketIds().get(i)).get().getTicketPrice().getPrice();
        }
        orderExist.setTotalPrice(totalPrice);

        //save to redis
        if (!isExist) {
            redisService.save(key, orderExist, TTL_DEFAULT);
            log.debug("[ORDER_SESSION] [SAVE] key = {}", key);
        } else {
            redisService.update(key, orderExist);
            log.debug("[ORDER_SESSION] [UPDATE] key = {}", key);
        }

    }

    @Override
    public void addOrUpdateCombos(Long userId, Long showtimeId, List<ConcessionOrderRequest> concessionOrders) {

    }

    @Override
    public void removeTickets(OrderSessionRequest request) {
        String key = getKey(request.getUserId(), request.getShowtimeId());

        //get order session
        OrderSessionDTO order = redisService.get(key, OrderSessionDTO.class);

        if (order == null) {
            log.debug("[ORDER_SESSION_REMOVE] No session found for userId={}, showtimeId={}",
                    request.getUserId(), request.getShowtimeId());
            return;
        }

        order.getTicketIds().removeAll(request.getTicketIds());

        if (!order.getTicketIds().isEmpty()) {
            redisService.update(key, order);
            log.debug("[ORDER_SESSION] [REMOVE] key = {}", key);
        } else {
            redisService.delete(key);
        }
    }

    @Override
    public Optional<OrderSessionDTO> find(Long userId, Long showtimeId) {
        String key = getKey(userId, showtimeId);
        return Optional.ofNullable(redisService.get(key, OrderSessionDTO.class));
    }

    @Override
    public void delete(Long userId, Long showtimeId) {
        redisService.delete(getKey(userId, showtimeId));
        log.info("[ORDER_SESSION] [DELETE] key = {}", getKey(userId, showtimeId));
    }

    @Override
    public void extendTTL(Long userId, Long showtimeId) {
        String key = getKey(userId, showtimeId);

        //get order session
        OrderSessionDTO order = redisService.get(key, OrderSessionDTO.class);

        if (order == null) {
            log.warn("[ORDER_SESSION][EXTEND] no session order found for userId ={}, showtimrId ={}", userId, showtimeId);
            return;
        }

        order.setExpiredAt(LocalDateTime.now().plusSeconds(PAYMENT_EXTENSION));

        redisService.save(key, order, PAYMENT_EXTENSION);
        log.info("[ORDER_SESSION][EXTEND] Payment start with ttl extended, key = {}", key);
    }
}
