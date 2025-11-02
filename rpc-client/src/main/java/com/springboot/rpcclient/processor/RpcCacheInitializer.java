package com.springboot.rpcclient.processor;

import com.springboot.rpcclient.ZookeeperServiceDiscovery;
import com.springboot.rpccommon.util.ServiceKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 缓存初始化器：所有 Bean 扫描完成后，用收集到的服务名预热缓存
 */
@Slf4j
@Component
public class RpcCacheInitializer implements SmartInitializingSingleton {

    private final RpcReferenceScanner referenceScanner;
    private final ZookeeperServiceDiscovery serviceDiscovery;

    // 注入扫描器和服务发现组件
    public RpcCacheInitializer(RpcReferenceScanner referenceScanner, ZookeeperServiceDiscovery serviceDiscovery) {
        this.referenceScanner = referenceScanner;
        this.serviceDiscovery = serviceDiscovery;
    }

    /**
     * 所有单例 Bean 初始化完成后，执行缓存预热
     */
    @Override
    public void afterSingletonsInstantiated() {
        // 1. 获取扫描到的服务列表（去重后的“接口名:版本”）
        Set<String> serviceKeys = referenceScanner.getCollectedServices();
        if (serviceKeys.isEmpty()) {
            log.warn("未扫描到 @RpcReference 注解的服务，缓存初始化跳过");
            return;
        }

        // 2. 遍历服务，预热缓存 + 注册 ZK 监听
        for (String serviceKey : serviceKeys) {
            // 拆分“接口名”和“版本”（如 "com.xxx.UserService:v1" → 接口名=前者，版本=后者）
            String serviceName = ServiceKeyUtil.parseServiceName(serviceKey);
            String version = ServiceKeyUtil.parseVersion(serviceKey);

            // 3. 预热缓存（拉取实例列表并更新本地缓存）
            List<String> instances = serviceDiscovery.getServiceInstances(serviceName, version);
            log.info("服务 {}:{} 缓存预热完成，实例数：{}", serviceName, version, instances.size());

            // 4. 注册 ZK 监听（感知服务实例变更）
            serviceDiscovery.watchService(serviceName, version);
        }
    }
}