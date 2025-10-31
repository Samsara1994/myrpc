package com.springboot.rpccommon.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * 标记负载均衡策略的名称
 */
@Target(ElementType.TYPE) // 作用在类上
@Retention(RetentionPolicy.RUNTIME) // 运行时可见
public @interface LoadBalancerType {
    String value(); // 策略名称（如 "roundRobin"、"random"）
}