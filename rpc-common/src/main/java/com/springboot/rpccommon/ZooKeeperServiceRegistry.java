package com.springboot.rpccommon;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.ArrayList;
import java.util.List;


@Slf4j
public class ZooKeeperServiceRegistry implements ServiceRegistry {
    private static final String BASE_PATH = "/rpc-microservice";
    private final CuratorFramework client;

    // 初始化ZooKeeper连接（如zk地址：127.0.0.1:2181）
    public ZooKeeperServiceRegistry(String zkAddress) {
        this.client = CuratorFrameworkFactory.builder()
                .connectString(zkAddress)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        this.client.start();
    }

    // 注册服务：/rpc-microservice/服务名/实例地址（临时节点，服务下线自动删除）
    @Override
    public void register(String serviceName, String serviceAddress) {
        try {
            String servicePath = BASE_PATH + "/" + serviceName;
            // 创建服务根节点（持久节点）
            if (client.checkExists().forPath(servicePath) == null) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(servicePath);
            }
            // 注册实例（临时节点，服务断开连接后自动删除）
            String instancePath = servicePath + "/" + serviceAddress;
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath);
            log.info("register service: {} success", serviceName);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
