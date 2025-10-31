package com.springboot.rpcclient.loadbalance;

import com.springboot.rpccommon.annotation.LoadBalancerType;

import java.util.List;
import java.util.Random;

@LoadBalancerType("random")
public class RandomStrategy implements LoadBalancerStrategy{
    private final Random random = new Random();

    @Override
    public String selectInstance(List<String> instances) {
        if (instances.isEmpty()) {
            return null;
        }
        int index = random.nextInt(instances.size());
        return instances.get(index);
    }
}
