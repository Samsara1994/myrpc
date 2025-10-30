package com.springboot.rpccommon.annotation;

import java.lang.annotation.*;

// 客户端注解：标注在接口字段上，自动注入RPC代理对象
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcReference {
    // 服务接口类型（默认取字段类型）
    Class<?> value() default void.class;
}
