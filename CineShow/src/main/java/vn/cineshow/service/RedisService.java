package vn.cineshow.service;

import vn.cineshow.dto.response.booking.SeatHold;

import java.util.Set;

public interface RedisService {
    void saveSeatHold(String key, SeatHold hold, long ttlSeconds);

    SeatHold getSeatHold(String key);

    void delete(String key);

    boolean exists(String key);

    Set<String> findKeys(String pattern);
}
