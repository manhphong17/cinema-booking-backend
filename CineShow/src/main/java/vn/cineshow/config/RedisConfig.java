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

    // Message when ttl expired
    @Bean
    public KeyExpirationEventMessageListener keyExpirationListener(
            RedisMessageListenerContainer container,
            SimpMessagingTemplate messagingTemplate) {
        KeyExpirationEventMessageListener listener = new KeyExpirationEventMessageListener(container) {
            @Override
            public void onMessage(org.springframework.data.redis.connection.Message message, byte[] pattern) {
                String expiredKey = message.toString();
                log.info("[Redis Expiration] Key expired: {}", expiredKey);

                // Chỉ xử lý các key seatHold
                if (!expiredKey.startsWith("seatHold:showtime:")) {
                    return;
                }

                // Parse key: seatHold:showtime:{showtimeId}:user:{userId}
                String[] parts = expiredKey.split(":");
                if (parts.length < 5) {
                    log.warn("[Redis Expiration] Invalid key format: {}", expiredKey);
                    return;
                }

                try {
                    long showtimeId = Long.parseLong(parts[2]);
                    long userId = Long.parseLong(parts[4]);

                    log.info("[Redis Expiration] SeatHold expired -> showtimeId={}, userId={}", showtimeId, userId);

                    // Gửi message với format giống như broadcast trong BookingServiceImpl
                    // Frontend đang subscribe vào /topic/seat/{showtimeId} và expect format:
                    // {seats: [], status: "EXPIRED", userId: number, showtimeId: number}
                    messagingTemplate.convertAndSend(
                            "/topic/seat/" + showtimeId,
                            Map.of(
                                    "seats", java.util.Collections.emptyList(), // Empty list vì key đã hết hạn
                                    "status", "EXPIRED",
                                    "userId", userId,
                                    "showtimeId", showtimeId
                            )
                    );

                    log.info("[Redis Expiration] Sent EXPIRED message to /topic/seat/{} for userId={}", showtimeId, userId);
                } catch (NumberFormatException e) {
                    log.error("[Redis Expiration] Failed to parse showtimeId or userId from key: {}", expiredKey, e);
                }
            }
        };

        // Bắt buộc phải set listener vào container
        container.addMessageListener(listener, new org.springframework.data.redis.listener.PatternTopic("__keyevent@*__:expired"));
        return listener;
    }

}
