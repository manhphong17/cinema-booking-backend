package vn.cineshow.service;

import vn.cineshow.dto.redis.ConcessionOrderRequest;
import vn.cineshow.dto.redis.OrderSessionDTO;
import vn.cineshow.dto.redis.OrderSessionRequest;

import java.util.List;
import java.util.Optional;

public interface OrderSessionService {

    void createOrUpdate(OrderSessionRequest request);

    void addOrUpdateCombos(Long userId, Long showtimeId, List<ConcessionOrderRequest> concessionOrders);

    void removeTickets(OrderSessionRequest request);

    Optional<OrderSessionDTO> find(Long userId, Long showtimeId);

    void delete(Long userId, Long showtimeId);

    void extendTTL(Long userId, Long showtimeId);
}
