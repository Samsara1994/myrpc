package com.springboot.rpcserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "rpc.server")
public class RpcServerProperties {
    private int port = 8888; // 默认端口
    private String zkAddress = "127.0.0.1:2181"; // 默认ZK地址
}
