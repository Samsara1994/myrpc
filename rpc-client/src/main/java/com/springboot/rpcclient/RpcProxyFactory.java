package com.springboot.rpcclient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.springboot.rpcclient.circuitbreaker.CircuitBreaker;
import com.springboot.rpcclient.loadbalance.LoadBalancer;
import com.springboot.rpccommon.dto.GenericRpcRequest;
import com.springboot.rpccommon.dto.GenericRpcResponse;
import com.springboot.rpccommon.ZooKeeperServiceRegistry;
import com.springboot.rpccommon.util.JsonSerializer;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;
import java.util.List;

public class RpcProxyFactory {
    private final ZooKeeperServiceRegistry registry;
    private final LoadBalancer loadBalancer;
    private final CircuitBreaker circuitBreaker;

    public RpcProxyFactory(ZooKeeperServiceRegistry registry, LoadBalancer loadBalancer, CircuitBreaker circuitBreaker) {
        this.registry = registry;
        this.loadBalancer = loadBalancer;
        this.circuitBreaker = circuitBreaker;
    }

    // 生成服务接口的泛型代理
    @SuppressWarnings("unchecked")
    public <T> T createProxy(Class<T> serviceInterface) {
        return (T) Proxy.newProxyInstance(
                serviceInterface.getClassLoader(),
                new Class[]{serviceInterface},
                new GenericInvocationHandler(serviceInterface)
        );
    }

    // 泛型方法调用处理器
    private class GenericInvocationHandler implements InvocationHandler {
        private final Class<?> serviceInterface;

        public GenericInvocationHandler(Class<?> serviceInterface) {
            this.serviceInterface = serviceInterface;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 1. 检查熔断状态
            if (!circuitBreaker.allowRequest()) {
                throw new RuntimeException("服务已熔断，请稍后再试");
            }

            // 参数处理（单参数场景）
            Class<?>[] parameterTypes = method.getParameterTypes();
            Class<?> paramType = parameterTypes.length > 0 ? parameterTypes[0] : Void.class;
            // 参数值
            Object paramValue = args != null && args.length > 0 ? args[0] : null;

            // 2. 构建泛型请求
            GenericRpcRequest<?> request = buildGenericRequest(serviceInterface.getName(), method.getName(), paramType, paramValue);


            // 3. 服务发现：获取实例列表
            List<String> instances = registry.getServiceInstances(serviceInterface.getName());
            if (instances.isEmpty()) {
                throw new RuntimeException("无可用服务实例：" + serviceInterface.getName());
            }

            // 4. 负载均衡：选择一个实例
            String instance = loadBalancer.selectInstance(instances);
            String[] address = instance.split(":");
            String host = address[0];
            int port = Integer.parseInt(address[1]);

            // 5. 发送请求并处理响应
            try {
                String responseJson = sendRequest(host, port, request);

                // 关键：先判断响应是否包含"HTTP/"（可能是HTTP错误）
                if (StringUtils.isEmpty(responseJson) || responseJson.startsWith("HTTP/")) {
                    throw new RuntimeException("服务端返回HTTP错误：" + responseJson);
                }

                JavaType genericResponseType = TypeFactory.defaultInstance()
                        .constructParametricType(GenericRpcResponse.class, method.getReturnType());

                // 反序列化泛型响应
                GenericRpcResponse<?> response = JsonSerializer.deserialize(responseJson, genericResponseType);

                if (!response.isSuccess()) {
                    throw new RuntimeException("远程调用失败：" + response.getErrorMsg());
                }
                circuitBreaker.onSuccess(); // 调用成功，重置熔断计数
                return response.getData();

            } catch (Exception e) {
                circuitBreaker.onFailure(); // 调用失败，记录熔断
                throw e;
            }
        }

        // 发送TCP请求
        private String sendRequest(String host, int port, GenericRpcRequest<?> request) throws IOException {
            try (Socket socket = new Socket(host, port);
                 PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                // 序列化请求并发送
                writer.println(JsonSerializer.serialize(request));
                // 读取响应
                return reader.readLine();
            }
        }

        // 泛型方法：强制绑定paramType和paramValue的类型
        private <P> GenericRpcRequest<P> buildGenericRequest(
                String serviceName,
                String methodName,
                Class<P> paramType,
                Object paramValue) {
            GenericRpcRequest<P> request = new GenericRpcRequest<>();
            request.setServiceName(serviceName);
            request.setMethodName(methodName);
            request.setParameterType(paramType); // paramType是Class<P>，匹配泛型
            // 强制转换参数值为P类型（运行时安全，因为paramValue是方法调用的实际参数，必然匹配paramType）
            request.setParameter(paramType.cast(paramValue));
            return request;
        }
    }
}
