package vn.cineshow.service;


import java.util.Set;

public interface RedisService {
    <T> void save(String key, T value, long ttlSeconds);

    <T> T get(String key, Class<T> clazz);

    void delete(String key);

    boolean exists(String key);

    Set<String> findKeys(String pattern);
}
