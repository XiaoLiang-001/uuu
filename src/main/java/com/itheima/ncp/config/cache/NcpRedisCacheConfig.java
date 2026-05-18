package com.itheima.ncp.config.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 生产环境 / 有 Redis 时的缓存实现：把 {@link org.springframework.cache.annotation.Cacheable}
 * 的读写落到 Redis 上，而不是本机 Map。
 * <p>
 * <b>为什么要有这个类？</b>
 * 引入 {@code spring-boot-starter-data-redis} 后，项目里会存在
 * {@link org.springframework.data.redis.connection.RedisConnectionFactory} 这个 Bean；
 * 这里基于它再声明一个主 {@code CacheManager}，和 {@link NcpSimpleCacheConfig} 二选一
 *（见各自类上的 @Conditional*）。
 * </p>
 * <p>
 * <b>key / value 怎么存？</b>
 * <ul>
 *   <li>key：用字符串序列化，Redis 里看得见的是可读字符串，而不是 JDK 乱码；</li>
 *   <li>value：用 JSON（GenericJackson2Json）存 Java 对象，能带上 {@link java.time.LocalDateTime} 等，</li>
 * </ul>
 * </p>
 * <p>
 * {@code transactionAware()}：在带事务的方法里，与 Spring 事务一起提交/回滚时缓存行为更一致（本项目简单场景不依赖它也行）。
 * </p>
 */
@Configuration
@ConditionalOnBean(RedisConnectionFactory.class)
public class NcpRedisCacheConfig {

    /** 没在某个分区单独设 TTL 时，默认 10 分钟。 */
    private static final Duration DEFAULT_TTL = Duration.ofSeconds(600);

    @Bean
    @Primary
    public org.springframework.cache.CacheManager cacheManager(
            @Autowired RedisConnectionFactory connectionFactory) {
        // 反序列化时要能还原 LocalDateTime，所以注册 JavaTimeModule
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        // 用 ISO-8601 写日期，而不是 [year,month,day] 数组
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 开启类型信息，避免从 Redis 反序列化后变成 LinkedHashMap 导致类型转换失败。
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        // 值以 JSON 形式进 Redis
        GenericJackson2JsonRedisSerializer json = new GenericJackson2JsonRedisSerializer(om);
        StringRedisSerializer keySer = new StringRedisSerializer();

        // 默认分区的共同规则：有过期时间、不缓存 null（与 yml 中 cache-null-values: false 一致思想）
        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(DEFAULT_TTL)
                // 所有缓存名在 Redis 里会带前缀；升级为 v2 避免读取历史不兼容缓存值。
                .prefixCacheNameWith("ncp:v2::")
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(keySer))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(json));

        // 为每个「缓存区」可单独设 TTL；不改名的分区仍用 base + 其 entryTtl 覆盖
        Map<String, RedisCacheConfiguration> per = new HashMap<String, RedisCacheConfiguration>(8);
        // 集市类数据变化更频繁，TTL 略短
        per.put(CacheNames.MARKET_LIST, base.entryTtl(Duration.ofMinutes(5)));
        per.put(CacheNames.MARKET_PAGE, base.entryTtl(Duration.ofMinutes(5)));
        per.put(CacheNames.MARKET_COUNT, base.entryTtl(Duration.ofMinutes(5)));
        // 单条商品、按用户名，读写相对没列表那么“热点风暴”，可略长
        per.put(CacheNames.PRODUCT_BY_ID, base.entryTtl(Duration.ofMinutes(10)));
        per.put(CacheNames.USER_BY_NAME, base.entryTtl(Duration.ofMinutes(15)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(base)
                .withInitialCacheConfigurations(per)
                .transactionAware()
                .build();
    }
}
