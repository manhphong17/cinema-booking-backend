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
     * Save any value to Redis with a specific TTL (time-to-live).
     * <p>
     * Behavior:
     * - Redis will automatically delete the key when TTL expires.
     * - Used for temporary cache or real-time data (e.g., seat hold, OTP, token, etc.)
     *
     * @param key        Redis key
     * @param value      Any serializable object
     * @param ttlSeconds Expiration time in seconds
     */
    @Override
    public <T> void save(String key, T value, long ttlSeconds) {
        if (key == null || value == null) {
            log.warn("Cannot save null key or value to Redis");
            return;
        }
        redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
        log.debug("Saved key={} with TTL={}s", key, ttlSeconds);
    }


    /**
     * Update an existing key value but keep the current TTL unchanged.
     * Prevents extending the seat hold time unintentionally.
     *
     * @param key      Redis key
     * @param newValue Updated value
     */
    @Override
    public <T> void update(String key, T newValue) {
        if (key == null || newValue == null) {
            log.warn("Cannot update null key or value in Redis");
            return;
        }

        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (ttl == null || ttl <= 0) {
            log.warn("Cannot update key={} because TTL expired or key not found", key);
            return;
        }

        redisTemplate.opsForValue().set(key, newValue, ttl, TimeUnit.SECONDS);
        log.debug("Updated key={} and kept existing TTL={}s", key, ttl);
    }

    /**
     * Get value from Redis by key.
     *
     * @param key   Redis key
     * @param clazz Expected type
     * @return Value if present, or null
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        if (key == null) return null;
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            log.trace("Redis miss for key={}", key);
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
     * Delete a key from Redis.
     *
     * @param key Redis key
     */
    @Override
    public void delete(String key) {
        if (key == null) return;
        Boolean result = redisTemplate.delete(key);
        if (result)
            log.debug("Deleted Redis key={}", key);
    }

    /**
     * Check if a key exists in Redis.
     *
     * @param key Redis key
     * @return true if exists, false otherwise
     */
    @Override
    public boolean exists(String key) {
        return key != null && redisTemplate.hasKey(key);
    }

    /**
     * Find all Redis keys by pattern.
     * Example: "seatHold:showtime:*"
     * <p>
     * Note:
     * - keys() has O(n) complexity.
     * - Should only be used for debugging or admin tools, not in production flow.
     *
     * @param pattern Pattern to match keys
     * @return Set of matching keys
     */
    @Override
    public Set<String> findKeys(String pattern) {
        if (pattern == null || pattern.isEmpty()) return Set.of();
        return redisTemplate.keys(pattern);
    }

    /**
     * Get the remaining TTL (in seconds) of a key.
     *
     * @param key Redis key
     * @return Remaining TTL in seconds, or -2 if not found
     */
    @Override
    public long getTTL(String key) {
        if (key == null) return -2;
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return ttl != null ? ttl : -2;
    }
}
