package com.springboot.rpcclient.cache;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务列表本地缓存管理器
 */
public class ServiceCacheManager {
    // 缓存结构：key=服务名（如 "com.xxx.UserService"），value=服务实例列表（如 ["192.168.1.1:8888", ...]）
    private final ConcurrentHashMap<String, List<String>> serviceCache = new ConcurrentHashMap<>();

    // 获取缓存的服务实例列表（如果没有，返回空列表）
    public List<String> getCachedInstances(String serviceName) {
        return serviceCache.getOrDefault(serviceName, Collections.emptyList());
    }

    public void updateCache(String serviceName, List<String> newInstances) {
        serviceCache.put(serviceName, newInstances);
    }

    // 清空某个服务的缓存（可选，如检测到实例全部不可用时）
    public void clearCache(String serviceName) {
        serviceCache.remove(serviceName);
    }
}
