package vn.cineshow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;

@Configuration
@Slf4j
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // Key serializer: lưu key dạng text
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Configure ObjectMapper to support Java 8 date/time
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Enable default typing to preserve class information
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
        );

        // Value serializer: lưu value dạng JSON với type information
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory factory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        return container;
    }

    // Thông báo khi TTL hết hạn
    @Bean
    public KeyExpirationEventMessageListener keyExpirationListener(
            RedisMessageListenerContainer container,
            SimpMessagingTemplate messagingTemplate) {
        return new KeyExpirationEventMessageListener(container) {
            @Override
            public void onMessage(org.springframework.data.redis.connection.Message message, byte[] pattern) {
                String expiredKey = message.toString();
                if (!expiredKey.startsWith("seatHold:showtime:")) return;

                String[] parts = expiredKey.split(":");
                if (parts.length < 5) return;
                Long showtimeId = Long.parseLong(parts[2]);
                Long userId = Long.parseLong(parts[4]);

                log.info("SeatHold expired -> showtime={}, user={}", showtimeId, userId);

                messagingTemplate.convertAndSend(
                        "/topic/seat-status/" + showtimeId,
                        Map.of(
                                "event", "EXPIRED",
                                "userId", userId,
                                "showtimeId", showtimeId
                        )
                );
            }
        };
    }

}
