package com.springboot.rpccommon.util;

public class ServiceKeyUtil {
    /**
     * 构建服务键（格式："接口全类名:版本"，无版本则直接返回接口名）
     */
    public static String buildServiceKey(String serviceName, String version) {
        return version.isEmpty() ? serviceName : serviceName + ":" + version;
    }

    /**
     * 解析服务键，获取接口名（如 "com.xxx.UserService:v1" → "com.xxx.UserService"）
     */
    public static String parseServiceName(String serviceKey) {
        int index = serviceKey.indexOf(":");
        return index == -1 ? serviceKey : serviceKey.substring(0, index);
    }

    /**
     * 解析服务键，获取版本（如 "com.xxx.UserService:v1" → "v1"，无版本则返回空）
     */
    public static String parseVersion(String serviceKey) {
        int index = serviceKey.indexOf(":");
        return index == -1 ? "" : serviceKey.substring(index + 1);
    }
}
