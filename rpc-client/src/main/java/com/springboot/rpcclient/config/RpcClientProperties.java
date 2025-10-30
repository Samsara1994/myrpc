package com.springboot.rpcclient.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "rpc.client")
@Data
public class RpcClientProperties {
    private String zkAddress = "127.0.0.1:2181"; // ZK地址
    private CircuitBreakerProperties circuitBreaker = new CircuitBreakerProperties(); // 熔断配置

    // 熔断子配置
    @Data
    public static class CircuitBreakerProperties {
        private int failureThreshold = 3; // 失败阈值
        private long resetTimeout = 5000; // 熔断重置时间（毫秒）

    }
}
