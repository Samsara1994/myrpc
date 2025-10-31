package com.springboot.rpcclient.config;

import com.springboot.rpcclient.circuitbreaker.CircuitBreaker;
import com.springboot.rpcclient.loadbalance.LoadBalancer;
import com.springboot.rpcclient.RpcProxyFactory;
import com.springboot.rpcclient.loadbalance.LoadBalancerStrategyFactory;
import com.springboot.rpcclient.loadbalance.RandomStrategy;
import com.springboot.rpcclient.loadbalance.RoundRobinStrategy;
import com.springboot.rpcclient.processor.RpcReferenceBeanPostProcessor;
import com.springboot.rpccommon.ZooKeeperServiceRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RpcClientProperties.class)
public class RpcClientAutoConfiguration {

    // 注册ZK注册中心
    @Bean
    @ConditionalOnMissingBean
    public ZooKeeperServiceRegistry zooKeeperServiceRegistry(RpcClientProperties properties) {
        return new ZooKeeperServiceRegistry(properties.getZkAddress());
    }

    // 注册负载均衡策略工厂
    @Bean
    public LoadBalancerStrategyFactory loadBalancerStrategyFactory(){
        return new LoadBalancerStrategyFactory();
    }

    // 注册负载均衡器
    @Bean
    @ConditionalOnMissingBean
    public LoadBalancer loadBalancer(LoadBalancerStrategyFactory factory, RpcClientProperties properties) {
        return new LoadBalancer(factory, properties);
    }

    @Bean
    public RandomStrategy randomStrategy(){
        return new RandomStrategy();
    }

    @Bean
    public RoundRobinStrategy roundRobinStrategy(){
        return new RoundRobinStrategy();
    }

    // 注册熔断组件
    @Bean
    @ConditionalOnMissingBean
    public CircuitBreaker circuitBreaker(RpcClientProperties properties) {
        return new CircuitBreaker(
                properties.getCircuitBreaker().getFailureThreshold(),
                properties.getCircuitBreaker().getResetTimeout()
        );
    }

    // 注册RPC代理工厂（生成服务代理）
    @Bean
    @ConditionalOnMissingBean
    public RpcProxyFactory rpcProxyFactory(
            ZooKeeperServiceRegistry registry,
            LoadBalancer loadBalancer,
            CircuitBreaker circuitBreaker) {
        return new RpcProxyFactory(registry, loadBalancer, circuitBreaker);
    }

    // 注册代理注入处理器（扫描@RpcReference注解）
    @Bean
    public RpcReferenceBeanPostProcessor rpcReferenceBeanPostProcessor(RpcProxyFactory proxyFactory) {
        return new RpcReferenceBeanPostProcessor(proxyFactory);
    }
}
