package com.springboot.rpcclient;
import com.springboot.rpcclient.cache.ServiceCacheManager;
import com.springboot.rpccommon.ServiceGovernance;
import com.springboot.rpccommon.util.ServiceKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.Watcher;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
public class ZookeeperServiceDiscovery implements ServiceGovernance {
    private static final String BASE_PATH = "/rpc-microservice";
    private final CuratorFramework client;
    private final ServiceCacheManager cacheManager; // 本地缓存管理器

    // 初始化ZooKeeper连接（如zk地址：127.0.0.1:2181）
    public ZookeeperServiceDiscovery(String zkAddress) {
        this.client = CuratorFrameworkFactory.builder()
                .connectString(zkAddress)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        this.client.start();
        this.cacheManager = new ServiceCacheManager();
    }

    // 发现服务：获取服务的所有实例地址
    @Override
    public List<String> getServiceInstances(String serviceName, String version) {
        try {
            String servicePath = buildZkPath(serviceName, version);
            if (client.checkExists().forPath(servicePath) == null) {
                return new ArrayList<>();
            }
            // 获取所有实例节点
            List<String> services = client.getChildren().forPath(servicePath);

            // 只保留符合 "IP:端口" 格式的有效实例（排除版本目录如 "1.0"）
            List<String> validInstances = filterValidInstances(services);
            cacheManager.updateCache(serviceName, validInstances);
            return validInstances;
        } catch (Exception e) {
            log.warn("从 ZK 拉取服务 {} 实例失败，使用本地缓存", serviceName, e);
            return cacheManager.getCachedInstances(serviceName);
        }

    }

    /**
     * 初始化时注册Watcher，监听服务列表变更（主动更新缓存）
     */
    public void watchService(String serviceName, String version) {
        String serviceKey = ServiceKeyUtil.buildServiceKey(serviceName, version);
        String servicePath = buildZkPath(serviceName, version);
        try {
            // 注册一次性监听，触发后重新注册
            client.getChildren().usingWatcher((Watcher) event -> {
                if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                    log.info("服务 {} 实例列表变化，更新缓存", serviceKey);
                    // 重新拉取并更新缓存
                    getServiceInstances(serviceName, version);
                    // 重新注册监听
                    watchService(serviceName, version);
                }
            }).forPath(servicePath);
        } catch (Exception e) {
            log.error("注册服务 {} 监听失败", serviceKey, e);
        }
    }

    private String buildZkPath(String serviceName, String version) {
        if (version.isEmpty()) {
            return BASE_PATH + "/" + serviceName;
        } else {
            return BASE_PATH + "/" + serviceName + "/" + version;
        }
    }

    /**
     * 过滤有效实例（只保留 "IP:端口" 格式的节点）
     */
    private List<String> filterValidInstances(List<String> rawNodes) {
        // 正则：匹配 "IP:端口"（IP可以是域名、localhost，端口是数字）
        String instancePattern = "^[^:]+:\\d+$";
        return rawNodes.stream()
                .filter(node -> node.matches(instancePattern))
                .collect(Collectors.toList());
    }
}
