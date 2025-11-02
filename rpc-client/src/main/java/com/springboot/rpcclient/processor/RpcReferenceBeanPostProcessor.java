package com.springboot.rpcclient.processor;

import com.springboot.rpcclient.RpcProxyFactory;
import com.springboot.rpccommon.annotation.RpcReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;

public class RpcReferenceBeanPostProcessor implements BeanPostProcessor{
    private static final Logger log = LoggerFactory.getLogger(RpcReferenceBeanPostProcessor.class);
    private final RpcProxyFactory proxyFactory;

    public RpcReferenceBeanPostProcessor(RpcProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            RpcReference annotation = field.getAnnotation(RpcReference.class);
            if (annotation != null) {
                Class<?> serviceInterface = annotation.value() == void.class ? field.getType() : annotation.value();
                Object proxy = proxyFactory.createProxy(serviceInterface, annotation.version());
                field.setAccessible(true);
                try {
                    field.set(bean, proxy);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("注入代理失败", e);
                }
            }
        }
        return bean;
    }
}
