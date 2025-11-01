package com.springboot.rpcclient;
import com.springboot.rpccommon.ServiceGovernance;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.ArrayList;
import java.util.List;


@Slf4j
public class ZookeeperServiceDiscovery implements ServiceGovernance {
    private static final String BASE_PATH = "/rpc-microservice";
    private final CuratorFramework client;

    // 初始化ZooKeeper连接（如zk地址：127.0.0.1:2181）
    public ZookeeperServiceDiscovery(String zkAddress) {
        this.client = CuratorFrameworkFactory.builder()
                .connectString(zkAddress)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        this.client.start();
    }

    // 发现服务：获取服务的所有实例地址
    @Override
    public List<String> getServiceInstances(String serviceName) {
        try {
            String servicePath = BASE_PATH + "/" + serviceName;
            if (client.checkExists().forPath(servicePath) == null) {
                return new ArrayList<>();
            }
            // 获取所有实例节点（IP:Port）
            return client.getChildren().forPath(servicePath);
        } catch (Exception e) {
            log.error("getServiceInstances error", e);
            return new ArrayList<>();
        }
    }
}
