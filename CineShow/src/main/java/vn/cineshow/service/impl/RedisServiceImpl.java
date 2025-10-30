package vn.cineshow.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import vn.cineshow.service.RedisService;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Luu bat ky du lieu nao vao Redis voi TTL (time to live)
     *
     * @param key        khoa Redis
     * @param value      doi tuong bat ky
     * @param ttlSeconds thoi gian ton tai (giay)
     *                   <p>
     *                   Co che:
     *                   - Redis tu dong xoa key khi het TTL
     *                   - Dung cho cache tam thoi hoac du lieu real-time (vi du seatInfo, seatHold, OTP, token, ...)
     */
    @Override
    public <T> void save(String key, T value, long ttlSeconds) {
        if (key == null || value == null) {
            log.warn("Cannot save null key or value to Redis");
            return;
        }
        redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
        log.debug("Saved key={} TTL={}s", key, ttlSeconds);
    }

    /**
     * Lay du lieu bat ky tu Redis
     *
     * @param key   khoa Redis
     * @param clazz kieu du lieu mong muon tra ve
     * @return doi tuong neu co, hoac null neu khong ton tai
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        if (key == null) return null;
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            log.trace("Redis miss key={}", key);
            return null;
        }
        if (!clazz.isInstance(value)) {
            log.error("Type mismatch for key={}, expected={}, actual={}",
                    key, clazz.getSimpleName(), value.getClass().getSimpleName());
            return null;
        }
        return (T) value;
    }

    /**
     * Xoa key khoi Redis
     *
     * @param key khoa Redis
     */
    @Override
    public void delete(String key) {
        if (key == null) return;
        Boolean result = redisTemplate.delete(key);
        if (result)
            log.debug("Deleted Redis key={}", key);
    }

    /**
     * Kiem tra key co ton tai trong Redis khong
     *
     * @param key khoa Redis
     * @return true neu ton tai, false neu khong
     */
    @Override
    public boolean exists(String key) {
        return key != null && redisTemplate.hasKey(key);
    }

    /**
     * Tim tat ca cac key theo mau pattern
     *
     * @param pattern vi du: seatInfo:showtime:*
     * @return tap hop cac key phu hop
     * <p>
     * Luu y:
     * - Ham keys() co do phuc tap O(n)
     * - Chi nen dung cho muc dich admin hoac debug, khong dung trong luong realtime
     */
    @Override
    public Set<String> findKeys(String pattern) {
        if (pattern == null || pattern.isEmpty()) return Set.of();
        return redisTemplate.keys(pattern);
    }
}
