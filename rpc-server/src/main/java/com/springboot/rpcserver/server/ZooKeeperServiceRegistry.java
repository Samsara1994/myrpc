package com.springboot.rpcserver.server;
import com.springboot.rpccommon.ServiceGovernance;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;


@Slf4j
public class ZooKeeperServiceRegistry implements ServiceGovernance {
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
    public void register(String serviceName, String version, String serviceAddress) {
        try {
            String servicePath = version.isEmpty() ? BASE_PATH + "/" + serviceName : BASE_PATH + "/" + serviceName + "/" + version;
            // 创建服务根节点（持久节点）
            if (client.checkExists().forPath(servicePath) == null) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(servicePath);
            }
            // 注册实例（临时节点，服务断开连接后自动删除）
            String instancePath = servicePath + "/" + serviceAddress;
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath);
            log.info("register service: {} success", serviceName);
        } catch (Exception e) {
            log.error("register service: {} error", serviceName, e);
        }
    }
}
