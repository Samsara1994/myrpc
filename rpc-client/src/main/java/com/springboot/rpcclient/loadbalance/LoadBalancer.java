package com.springboot.rpcclient.loadbalance;

import com.springboot.rpcclient.config.RpcClientProperties;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadBalancer {
    private final LoadBalancerStrategy strategy;

    // 构造函数：依赖工厂和配置，动态获取策略
    public LoadBalancer(LoadBalancerStrategyFactory factory, RpcClientProperties properties) {
        String strategyName = properties.getLoadBalancer();
        this.strategy = factory.getStrategy(strategyName);
    }

    public String selectInstance(List<String> instances) {
        return strategy.selectInstance(instances);
    }
}
