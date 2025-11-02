package com.springboot.rpcserver.config;

import com.springboot.rpcserver.server.ZooKeeperServiceRegistry;
import com.springboot.rpcserver.server.RpcServer;
import com.springboot.rpcserver.processor.RpcServiceBeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RpcServerProperties.class) // 绑定配置文件
public class RpcServerAutoConfiguration {
    // 注册ZooKeeper服务注册中心（依赖rpc-common中的ZooKeeperServiceRegistry）
    @Bean
    public ZooKeeperServiceRegistry zooKeeperServiceRegistry(RpcServerProperties properties) {
        return new ZooKeeperServiceRegistry(properties.getZkAddress());
    }

    // 注册RPC服务端核心Bean
    @Bean
    @ConditionalOnMissingBean
    public RpcServer rpcServer(RpcServerProperties properties, ZooKeeperServiceRegistry registry) {
        return new RpcServer(properties.getIp(), properties.getPort(), registry);
    }

    // 扫描@RpcService注解的Bean，自动注册到服务端
    @Bean
    public RpcServiceBeanPostProcessor rpcServiceBeanPostProcessor(RpcServer rpcServer) {
        return new RpcServiceBeanPostProcessor(rpcServer);
    }
}
