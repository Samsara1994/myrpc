package com.springboot.rpcclient.loadbalance;

import com.springboot.rpccommon.annotation.LoadBalancerType;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class LoadBalancerStrategyFactory implements ApplicationContextAware {
    // 缓存：策略名称 → 策略实例
    private final Map<String, LoadBalancerStrategy> strategyMap = new HashMap<>();

    // 默认策略（轮询）
    private static final String DEFAULT_STRATEGY = "roundRobin";

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 1. 从Spring容器中获取所有 LoadBalancerStrategy 类型的Bean
        Map<String, LoadBalancerStrategy> beans = applicationContext.getBeansOfType(LoadBalancerStrategy.class);

        // 2. 遍历Bean，通过 @LoadBalancerType 注解的value作为key，存入strategyMap
        for (LoadBalancerStrategy strategy : beans.values()) {
            LoadBalancerType annotation = strategy.getClass().getAnnotation(LoadBalancerType.class);
            if (annotation != null) {
                String strategyName = annotation.value();
                strategyMap.put(strategyName, strategy);
            }
        }
    }

    // 根据策略名称获取实例（如果不存在，返回默认策略）
    public LoadBalancerStrategy getStrategy(String strategyName) {
        LoadBalancerStrategy strategy = strategyMap.get(strategyName);
        if (strategy == null) {
            // 找不到指定策略，使用默认轮询
            strategy = strategyMap.get(DEFAULT_STRATEGY);
        }
        return strategy;
    }
}
