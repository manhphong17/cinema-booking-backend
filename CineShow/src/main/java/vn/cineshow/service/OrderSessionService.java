package vn.cineshow.service;

import vn.cineshow.dto.redis.OrderSessionDTO;
import vn.cineshow.dto.redis.OrderSessionRequest;
import vn.cineshow.dto.request.booking.ConcessionListRequest;

import java.util.Optional;

public interface OrderSessionService {

    void createOrUpdate(OrderSessionRequest request);

    void addOrUpdateCombos(ConcessionListRequest concessionListRequest);

    void removeTickets(OrderSessionRequest request);

    Optional<OrderSessionDTO> find(Long userId, Long showtimeId);

    void delete(Long userId, Long showtimeId);

    void extendTTL(Long userId, Long showtimeId);
}
