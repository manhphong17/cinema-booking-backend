package vn.cineshow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import vn.cineshow.dto.response.booking.SeatHold;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, SeatHold> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, SeatHold> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // Key serializer: lưu key dạng text
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value serializer: lưu value dạng JSON (dễ đọc, không lỗi version)
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }


}
