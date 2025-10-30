package com.springboot.rpccommon;

import java.util.List;

public interface ServiceRegistry {
    // 注册服务（服务接口名 -> 服务地址）
    void register(String serviceName, String serviceAddress);

    // 获取服务地址
    List<String> getServiceInstances(String serviceName);
}
