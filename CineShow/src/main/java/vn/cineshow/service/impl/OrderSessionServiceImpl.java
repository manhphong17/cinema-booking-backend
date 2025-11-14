package vn.cineshow.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.cineshow.dto.redis.OrderSessionDTO;
import vn.cineshow.dto.redis.OrderSessionRequest;
import vn.cineshow.dto.request.booking.ConcessionListRequest;
import vn.cineshow.dto.request.booking.ConcessionOrderRequest;
import vn.cineshow.enums.OrderSessionStatus;
import vn.cineshow.exception.AppException;
import vn.cineshow.exception.ErrorCode;
import vn.cineshow.exception.ResourceNotFoundException;
import vn.cineshow.model.Concession;
import vn.cineshow.repository.ConcessionRepository;
import vn.cineshow.repository.TicketRepository;
import vn.cineshow.service.OrderSessionService;
import vn.cineshow.service.RedisService;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrderSessionServiceImpl implements OrderSessionService {

    private final RedisService redisService;
    private final TicketRepository ticketRepository;
    private final ConcessionRepository concessionRepository;

    @Value("${booking.ttl.default}")
    private long TTL_DEFAULT; //600

//    @Value("${booking.ttl.payment}")
//    private long PAYMENT_EXTENSION; //1200

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
    public void addOrUpdateCombos(ConcessionListRequest concessionListRequest) {
        String key = getKey(concessionListRequest.getUserId(), concessionListRequest.getShowtimeId());

        //get Order session in redis
        OrderSessionDTO orderExist = redisService.get(key, OrderSessionDTO.class);

        boolean isExist = orderExist != null;

        // Auto-create order-session if it doesn't exist (for staff orders without tickets)
        if (!isExist) {
            log.info("[ORDER_SESSION] [COMBO] key = {} not found, creating new order session", key);
            orderExist = new OrderSessionDTO();
            orderExist.setCreatedAt(LocalDateTime.now());
            orderExist.setExpiredAt(LocalDateTime.now().plusSeconds(TTL_DEFAULT));
            orderExist.setStatus(OrderSessionStatus.PENDING);
            orderExist.setUserId(concessionListRequest.getUserId());
            orderExist.setShowtimeId(concessionListRequest.getShowtimeId());
            orderExist.setTicketIds(new ArrayList<>()); // Empty ticket list for staff orders without tickets
            orderExist.setConcessionOrders(new ArrayList<>());
        }

        // At this point, orderExist is guaranteed to be non-null
        final OrderSessionDTO order = orderExist;

        // Ensure concessionOrders list exists and clear it
        if (order.getConcessionOrders() == null) {
            order.setConcessionOrders(new ArrayList<>());
        }
        order.getConcessionOrders().clear();

        order.getConcessionOrders().addAll(concessionListRequest.getConcessions());
        order.setTotalPrice(countTotalAmount(order));
        log.info("[ORDER_SESSION][COMBO] Combo updated, key = {}", key);

        // Save or update in redis
        if (!isExist) {
            redisService.save(key, order, TTL_DEFAULT);
            log.info("[ORDER_SESSION] [SAVE] Created new order session with concessions, key = {}", key);
        } else {
            redisService.update(key, order);
        }
    }

    private double countTotalAmount(OrderSessionDTO order) {

        double totalAmountConcession = 0.0;
        for (ConcessionOrderRequest concessionOrder : order.getConcessionOrders()) {
            Concession cossession = concessionRepository.findById(concessionOrder.getComboId()).orElseThrow(
                    () -> new ResourceNotFoundException("concession order not found with id: " + concessionOrder.getComboId())
            );
            totalAmountConcession += cossession.getPrice() * concessionOrder.getQuantity();
        }

        double totalAmountTicket = 0.0;
        for (Long ticketId : order.getTicketIds()) {
            totalAmountTicket += ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new ResourceNotFoundException("TicketRepository not found, id: " + ticketId))
                    .getTicketPrice().getPrice();
        }

        return totalAmountConcession + totalAmountTicket;
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
    public OrderSessionDTO getOrderSession(Long showtimeId, Long userId) {
        //  Tạo key theo convention chung
        String key = getKey(userId, showtimeId);


        //  Lấy OrderSessionDTO từ Redis
        OrderSessionDTO orderExist = redisService.get(key, OrderSessionDTO.class);

        //  Nếu không tồn tại → ném exception
        if (orderExist == null) {
            log.warn("[ORDER_SESSION][FETCH] No session found for key={}", key);
            throw new AppException(ErrorCode.ORDER_SESSION_NOT_FOUND);
        }

        log.info("[ORDER_SESSION][FETCH] Successfully retrieved order session for key={}", key);
        return orderExist;
    }

}
