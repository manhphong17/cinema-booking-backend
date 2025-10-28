package vn.cineshow.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import vn.cineshow.dto.response.booking.SeatHold;
import vn.cineshow.service.RedisService;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {
    private final RedisTemplate<String, SeatHold> redisTemplate;

    /**
     * save seat hold to redis
     *
     * @param key        exp:seatHold:showtime:15:user:7
     * @param hold       seat hold
     * @param ttlSeconds time to live in seconds
     *                   <p>
     *                   Co che:
     *                   - Luu value voi ttl (het han -> Redis tu xoa)
     *                   - TTL dam bao neu nguoi dung roi trang, ghe tu giai phong
     *                   </p>
     */
    @Override
    public void saveSeatHold(String key, SeatHold hold, long ttlSeconds) {
        if (key == null || hold == null) {
            log.warn("Cannot save null key or value to Redis");
            return;
        }
        redisTemplate.opsForValue().set(key, hold, ttlSeconds, TimeUnit.SECONDS);
        log.info("Save seat successfully, key: {}", key);
    }

    /**
     * get seat hold from redis
     *
     * @param key exp:seatHold:showtime:15:user:7
     * @return seat hold or null if not found
     * <p>
     * Ung dung:
     * <ul>
     * <li>dung de kiem tra user hien tai dang giu ghe nao</li>
     * <li>phuc vu merge voi DB khi FE load lai trang</li>
     * </ul>
     */

    @Override
    public SeatHold getSeatHold(String key) {
        if (key == null) return null;
        SeatHold hold = redisTemplate.opsForValue().get(key);
        if (hold == null)
            log.debug("SeatHold not found in Redis key={}", key);
        return hold;
    }


    /**
     * Xoa key Redis khi user thanh toan hoac bo tat ca ghe
     *
     * @param key khoa Redis can xoa
     *            <p>
     *            Redis tra ve true neu xoa thanh cong
     */
    @Override
    public void delete(String key) {
        if (key == null) return;
        Boolean result = redisTemplate.delete(key);
        if (result)
            log.debug("Deleted SeatHold key={}", key);
    }

    /**
     * Kiem tra key co ton tai trong Redis khong
     *
     * @param key khoa Redis
     * @return true neu ton tai, false neu khong
     * <p>
     * Dung de xac dinh user da co phien giu ghe chua
     */
    @Override
    public boolean exists(String key) {
        return key != null && redisTemplate.hasKey(key);
    }

    /**
     * Tim tat ca cac key theo pattern
     *
     * @param pattern chuoi pattern, vi du: seatHold:showtime:15:*
     * @return tap hop cac key phu hop
     * <p>
     * Chu y: ham keys() duyet toan bo Redis (O(n))
     * chi nen dung trong admin hoac debug, khong dung trong luong realtime
     */
    @Override
    public Set<String> findKeys(String pattern) {
        if (pattern == null || pattern.isEmpty()) return Set.of();
        return redisTemplate.keys(pattern);
    }
}
