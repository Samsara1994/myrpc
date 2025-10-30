package com.springboot.rpccommon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericRpcRequest <T> implements Serializable {
    private String serviceName;       // 服务接口全类名
    private String methodName;        // 方法名
    private Class<T> parameterType;   // 参数类型（解决泛型擦除）
    private T parameter;              // 泛型参数
}
