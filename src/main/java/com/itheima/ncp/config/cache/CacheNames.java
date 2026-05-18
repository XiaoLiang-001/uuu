package com.itheima.ncp.config.cache;

/**
 * Spring Cache 的「缓存区」名称（在 Redis 里会作为逻辑分区名，实际 key 还会加前缀，见
 * {@link NcpRedisCacheConfig} 中的 {@code prefixCacheNameWith}）。
 * <p>
 * 在 {@code application.yml} 里可写 {@code spring.cache.cache-names: ...} 与这里保持一致，便于
 * 启动时就知道有哪些缓存分桶（不写也行，很多场景下首次 @Cacheable 时也会建）。
 * </p>
 * <p>
 * 与代码中注解对应关系：{@code @Cacheable(cacheNames = ...)}、{@code @CacheEvict} 会引用下列常量。
 * </p>
 */
public final class CacheNames {

    /** 按主键查 {@link com.itheima.ncp.entity.product.Product}，key 一般是商品 id。 */
    public static final String PRODUCT_BY_ID = "productById";
    /**
     * 用户侧：全部「上架中」商品列表（不分页），本项目中 key 固定为 {@code 'all'}，见
     * {@code ProductServiceImpl#listOnShelf} 上的 @Cacheable。
     */
    public static final String MARKET_LIST = "marketList";
    /** 上架商品总数，key 一般为固定字符串。 */
    public static final String MARKET_COUNT = "marketCount";
    /**
     * 上架商品分页，key 一般为 {@code "offset + '-' + limit"} 这种拼出来的字符串，避免每页数据串台。
     */
    public static final String MARKET_PAGE = "marketPage";
    /**
     * 按登录名查 {@link com.itheima.ncp.entity.user.User}，key 为 trim 后的 username。
     * 供 Security 登录链路等高频读库使用。
     */
    public static final String USER_BY_NAME = "userByName";

    /** 本地回退实现 {@link NcpSimpleCacheConfig} 需要预先声明的名字列表。 */
    public static final String[] ALL = {
            PRODUCT_BY_ID, MARKET_LIST, MARKET_COUNT, MARKET_PAGE, USER_BY_NAME
    };

    private CacheNames() {
    }
}
