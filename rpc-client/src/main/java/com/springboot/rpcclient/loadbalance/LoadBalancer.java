package com.springboot.rpcclient.loadbalance;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadBalancer {
    private final AtomicInteger index = new AtomicInteger(0);

    public String selectInstance(List<String> instances) {
        if (instances.isEmpty()) return null;
        int i = index.getAndIncrement() % instances.size();
        return instances.get(i);
    }
}
