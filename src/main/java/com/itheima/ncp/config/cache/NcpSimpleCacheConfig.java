package com.itheima.ncp.config.cache;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Role;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * 无 Redis 时的「退路」：用进程内的 {@link java.util.concurrent.ConcurrentHashMap} 当缓存
 *（Spring 的 {@link ConcurrentMapCacheManager} 封装了它）。
 * <p>
 * <b>什么时候会进这个类？</b> 看 {@code @ConditionalOnMissingBean(RedisConnectionFactory.class)}——
 * 当 classpath 上没有成功创建出 Redis 连接工厂时（例如
 * {@code src/test/resources/application.yml} 里排掉了
 * {@code org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration}），
 * 就不会用 {@link NcpRedisCacheConfig}，而用这个，避免启动失败。
 * </p>
 * <p>
 * <b>和 Redis 里缓存有什么区别？</b> 不跨进程、不跨机，JVM 一停就丢；但 {@code @Cacheable}
 * 的写法完全一样，所以业务代码不用改。
 * </p>
 * <p>
 * 使用 {@code ConcurrentMapCacheManager( CacheNames.ALL )}：预先建出 {@link CacheNames#ALL} 里列出的
 * 各分区名，避免漏名导致调试时找不着缓存。
 * </p>
 */
@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@ConditionalOnMissingBean(RedisConnectionFactory.class)
public class NcpSimpleCacheConfig {

    @Bean
    @Primary
    public org.springframework.cache.CacheManager simpleCacheManager() {
        return new ConcurrentMapCacheManager(CacheNames.ALL);
    }
}
