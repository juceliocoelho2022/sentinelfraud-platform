package br.com.jucelio.sentinelfraud.velocity;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;

@Component
public class RedisVelocityCounter implements VelocityCounter {
    private static final DefaultRedisScript<Long> RECORD_AND_COUNT = new DefaultRedisScript<>("""
            redis.call('ZREMRANGEBYSCORE', KEYS[1], '-inf', ARGV[2])
            redis.call('ZADD', KEYS[1], ARGV[1], ARGV[3])
            redis.call('PEXPIRE', KEYS[1], ARGV[4])
            return redis.call('ZCARD', KEYS[1])
            """, Long.class);

    private final StringRedisTemplate redis;
    private final Clock clock;

    @Autowired
    public RedisVelocityCounter(StringRedisTemplate redis) {
        this(redis, Clock.systemUTC());
    }

    RedisVelocityCounter(StringRedisTemplate redis, Clock clock) {
        this.redis = redis;
        this.clock = clock;
    }

    @Override
    public long recordAndCount(String scope, String value, String transactionId, Duration window) {
        long now = clock.millis();
        long cutoff = now - window.toMillis();
        String key = "fraud:velocity:" + scope + ":" + hash(value);
        Long count = redis.execute(RECORD_AND_COUNT, List.of(key),
                Long.toString(now), Long.toString(cutoff), transactionId, Long.toString(window.toMillis()));
        return count == null ? 0 : count;
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 must be available", ex);
        }
    }
}
