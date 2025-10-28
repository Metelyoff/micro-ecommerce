package com.ecommerce.inventory.configs;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.cache.caffeine.CaffeineCache;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig implements CachingConfigurer {

    public static final String ITEMS_ALL = "items_all";
    public static final String ITEM_BY_ID = "item_by_id";
    public static final String RESERVED_ITEMS_BY_ORDER = "reserved_items_by_order";

    @Value("${inventory.cache.items-all.ttl-seconds:30}")
    private long itemsAllTtlSeconds;
    @Value("${inventory.cache.items-all.max-size:100}")
    private long itemsAllMaxSize;

    @Value("${inventory.cache.item-by-id.ttl-seconds:60}")
    private long itemByIdTtlSeconds;
    @Value("${inventory.cache.item-by-id.max-size:500}")
    private long itemByIdMaxSize;

    @Value("${inventory.cache.reserved-items-by-order.ttl-seconds:300}")
    private long reservedItemsByOrderTtlSeconds;
    @Value("${inventory.cache.reserved-items-by-order.max-size:1000}")
    private long reservedItemsByOrderMaxSize;

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                buildCaffeineCache(ITEMS_ALL, itemsAllTtlSeconds, itemsAllMaxSize),
                buildCaffeineCache(ITEM_BY_ID, itemByIdTtlSeconds, itemByIdMaxSize),
                buildCaffeineCache(RESERVED_ITEMS_BY_ORDER, reservedItemsByOrderTtlSeconds, reservedItemsByOrderMaxSize)
        ));
        return manager;
    }

    @NonNull
    private CaffeineCache buildCaffeineCache(String name, long ttlSeconds, long maxSize) {
        return new CaffeineCache(
                name,
                Caffeine.newBuilder()
                        .expireAfterWrite(ttlSeconds, TimeUnit.SECONDS)
                        .maximumSize(maxSize)
                        .recordStats()
                        .build()
        );
    }

}
