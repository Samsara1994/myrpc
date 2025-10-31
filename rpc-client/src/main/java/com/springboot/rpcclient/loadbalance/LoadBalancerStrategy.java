package com.springboot.rpcclient.loadbalance;

import java.util.List;

public interface LoadBalancerStrategy {
    // 从实例列表中选择一个实例
    String selectInstance(List<String> instances);
}
