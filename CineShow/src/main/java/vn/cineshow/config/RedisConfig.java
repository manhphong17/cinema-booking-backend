package vn.cineshow.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import jakarta.annotation.PostConstruct;
import java.time.Duration;

@Configuration
@Slf4j
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.username:}")
    private String redisUsername;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.ssl.enabled:false}")
    private boolean sslEnabled;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        log.info("🔴 Connecting to Redis: {}:{} (SSL: {})", redisHost, redisPort, sslEnabled);

        // Base configuration
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        if (!redisUsername.isEmpty()) config.setUsername(redisUsername);
        if (!redisPassword.isEmpty()) config.setPassword(redisPassword);

        // Socket and timeout settings
        SocketOptions socketOptions = SocketOptions.builder()
                .connectTimeout(Duration.ofSeconds(10))
                .keepAlive(true)
                .build();

        TimeoutOptions timeoutOptions = TimeoutOptions.builder()
                .fixedTimeout(Duration.ofSeconds(10))
                .build();

        ClientOptions clientOptions = ClientOptions.builder()
                .socketOptions(socketOptions)
                .timeoutOptions(timeoutOptions)
                .autoReconnect(true)
                .build();

        // Lettuce client config
        LettuceClientConfiguration.LettuceClientConfigurationBuilder clientConfigBuilder =
                LettuceClientConfiguration.builder()
                        .clientOptions(clientOptions)
                        .commandTimeout(Duration.ofSeconds(10));

        // SSL for ElastiCache
        if (sslEnabled) {
            log.info("🔒 SSL enabled for Redis connection (AWS ElastiCache)");
            clientConfigBuilder.useSsl().disablePeerVerification(); // <--- Bỏ verify chứng chỉ AWS
        } else {
            log.info("⚪ SSL disabled for Redis connection");
        }

        LettuceConnectionFactory factory = new LettuceConnectionFactory(config, clientConfigBuilder.build());
        factory.setValidateConnection(true);

        try {
            factory.afterPropertiesSet();
            log.info("✅ RedisConnectionFactory initialized successfully");
        } catch (Exception e) {
            log.error("❌ Failed to initialize RedisConnectionFactory: {}", e.getMessage(), e);
            throw e;
        }

        return factory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // Key serializers
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Object mapper config
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        // Value serializer
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @PostConstruct
    public void testRedisConnection() {
        try {
            LettuceConnectionFactory factory = (LettuceConnectionFactory) redisConnectionFactory();
            factory.afterPropertiesSet();

            RedisTemplate<String, Object> redisTemplate = redisTemplate(factory);
            redisTemplate.opsForValue().set("test:ping", "pong");
            String value = (String) redisTemplate.opsForValue().get("test:ping");
            log.info("🟢 Redis test write OK: {}", value);
        } catch (Exception e) {
            log.error("🔴 Redis test write FAILED: {}", e.getMessage(), e);
        }
    }
}
