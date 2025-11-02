package com.springboot.rpcclient.processor;

import com.springboot.rpccommon.annotation.RpcReference;
import com.springboot.rpccommon.util.ServiceKeyUtil;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class RpcReferenceScanner implements BeanPostProcessor, Ordered {
    private static final Logger log = LoggerFactory.getLogger(RpcReferenceScanner.class);
    /**
     *
     *  对外提供收集到的服务列表（供缓存初始化使用）
     */
    @Getter
    private final Set<String> collectedServices = new ConcurrentSkipListSet<>();


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            RpcReference annotation = field.getAnnotation(RpcReference.class);
            if (annotation != null) {
                Class<?> serviceInterface = annotation.value() == void.class ? field.getType() : annotation.value();
                String serviceName = serviceInterface.getName();
                String version = annotation.version();
                // 拼接“接口名:版本”（用于区分同接口不同版本）
                String serviceKey = ServiceKeyUtil.buildServiceKey(serviceName, version);
                collectedServices.add(serviceKey);
            }
        }
        return bean;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * 构建服务唯一键：接口全类名 + 版本（如 "com.xxx.UserService:v1"）
     */
    private String buildServiceKey(String serviceName, String version) {
        return version.isEmpty() ? serviceName : serviceName + ":" + version;
    }

}
