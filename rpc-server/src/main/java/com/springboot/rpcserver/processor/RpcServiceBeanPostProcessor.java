package com.springboot.rpcserver.processor;

import com.springboot.rpccommon.annotation.RpcService;
import com.springboot.rpcserver.server.RpcServer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class RpcServiceBeanPostProcessor implements BeanPostProcessor {
    private final RpcServer rpcServer;

    public RpcServiceBeanPostProcessor(RpcServer rpcServer) {
        this.rpcServer = rpcServer;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        RpcService annotation = bean.getClass().getAnnotation(RpcService.class);
        if (annotation != null) {
            Class<?> serviceInterface = annotation.value() == void.class
                    ? bean.getClass().getInterfaces()[0]
                    : annotation.value();
            String version = annotation.version();
            rpcServer.registerService(serviceInterface, version, bean);
        }
        return bean;
    }
}
