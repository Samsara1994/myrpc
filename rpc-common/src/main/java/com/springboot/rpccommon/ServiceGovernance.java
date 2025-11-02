package com.springboot.rpccommon;

import java.util.Collections;
import java.util.List;

public interface ServiceGovernance {
    // 注册服务（服务接口名 -> 服务地址）
    default void register(String serviceName, String version, String serviceAddress){

    }

    // 获取服务地址
    default List<String> getServiceInstances(String serviceName, String version){
        return Collections.emptyList();
    }
}
