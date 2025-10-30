package com.springboot.rpccommon.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class GenericRpcResponse <R> implements Serializable {
    private boolean success;          // 调用是否成功
    private R data;                   // 泛型返回数据
    private String errorMsg;          // 错误信息（失败时）
    private Class<R> resultType;      // 返回值实际类型

    public static <R> GenericRpcResponse<R> createSuccess(Object data, Class<R> resultType) {
        // 强制转换data为R类型（运行时保证安全，因为反射调用的返回值一定匹配方法声明类型）
        R castData = resultType.cast(data);
        return success(castData, resultType);
    }

    // 静态工厂方法
    public static <R> GenericRpcResponse<R> success(R data, Class<R> resultType) {
        GenericRpcResponse<R> response = new GenericRpcResponse<>();
        response.success = true;
        response.data = data;
        response.resultType = resultType;
        return response;
    }

    public static <R> GenericRpcResponse<R> fail(String errorMsg, Class<R> resultType) {
        GenericRpcResponse<R> response = new GenericRpcResponse<>();
        response.success = false;
        response.errorMsg = errorMsg;
        response.resultType = resultType;
        return response;
    }
}
