package com.springboot.rpccommon.annotation;

import java.lang.annotation.*;

// 服务端注解：标注在服务实现类上，自动注册到RPC服务端
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcService {
    // 服务接口类型（默认取实现的第一个接口）
    Class<?> value() default void.class;
}
