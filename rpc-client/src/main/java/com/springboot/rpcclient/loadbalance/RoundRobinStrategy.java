package com.springboot.rpcclient.loadbalance;

import com.springboot.rpccommon.annotation.LoadBalancerType;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@LoadBalancerType("roundRobin")
public class RoundRobinStrategy implements LoadBalancerStrategy{
    private final AtomicInteger index = new AtomicInteger(0);
    @Override
    public String selectInstance(List<String> instances) {
        if (instances.isEmpty()) {
            return null;
        }
        // 防止index溢出，取模保证在实例范围内
        int i = index.getAndIncrement() % instances.size();
        // 处理负数（index溢出时）
        return instances.get((i + instances.size()) % instances.size());
    }
}
