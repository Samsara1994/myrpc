package com.springboot.rpcserver.server;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.springboot.rpccommon.ServiceRegistry;
import com.springboot.rpccommon.dto.GenericRpcRequest;
import com.springboot.rpccommon.dto.GenericRpcResponse;
import com.springboot.rpccommon.util.JsonSerializer;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class RpcServer {
    private final int port;
    private final ServiceRegistry registry;
    private final Map<String, Object> serviceMap = new HashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    // 关键：用volatile确保线程可见性，避免多线程下判断Socket状态错误
    private volatile ServerSocket serverSocket;
    private volatile boolean isRunning = false; // 标记服务是否运行

    public RpcServer(int port, ServiceRegistry registry) {
        this.port = port;
        this.registry = registry;
    }

    public void registerService(Class<?> serviceInterface, Object serviceImpl) {
        serviceMap.put(serviceInterface.getName(), serviceImpl);
        registry.register(serviceInterface.getName(), "localhost:" + port);
    }

    // 启动服务（用@EventListener监听Spring容器初始化完成事件，替代@PostConstruct）
    @EventListener(ContextRefreshedEvent.class) // 容器完全初始化后再启动服务
    public void start() {
        try {
            if (isRunning) {
                System.out.println("服务端已启动，无需重复启动");
                return;
            }
            serverSocket = new ServerSocket(port);
            isRunning = true;
            System.out.println("RPC服务端启动成功，端口：" + port);

            // 启动线程监听请求
            new Thread(this::listen).start();
        } catch (IOException e) {
            isRunning = false;
            throw new RuntimeException("服务端启动失败", e);
        }
    }

    // 单独抽取监听逻辑，便于状态判断
    private void listen() {
        while (isRunning && !serverSocket.isClosed()) { // 双重判断，避免Socket已关闭仍循环
            try {
                // 接收客户端连接（若Socket已关闭，accept()会抛出IOException）
                Socket socket = serverSocket.accept();
                System.out.println("接收到客户端连接：" + socket.getInetAddress());
                executor.submit(new RequestHandler(socket));
            } catch (IOException e) {
                // 若服务已停止，忽略关闭异常；否则打印错误
                if (isRunning) {
                    System.err.println("服务端接收连接失败：" + e.getMessage());
                } else {
                    System.out.println("服务端已停止，停止接收连接");
                }
                break; // Socket已关闭，退出监听循环
            }
        }
    }

    @PreDestroy
    public void stop() throws IOException {
        if (serverSocket != null) serverSocket.close();
        executor.shutdown();
    }

    private class RequestHandler implements Runnable {
        private final Socket socket;

        public RequestHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

                // 1. 读取泛型请求（JSON反序列化）
                String requestJson = reader.readLine();
                GenericRpcRequest<?> request = JsonSerializer.deserialize(requestJson, new TypeReference<GenericRpcRequest<?>>() {});

                // 2. 反射调用目标方法（获取结果和返回类型）
                Object result = invokeTargetMethod(request);
                Class<?> returnType =  result == null ? Object.class : result.getClass();

                // 3. 构建并返回泛型响应（使用方法返回类型，强制转换result）
                GenericRpcResponse<?> response = GenericRpcResponse.createSuccess(result, returnType);
                writer.println(JsonSerializer.serialize(response));

            } catch (Exception e) {
                // 处理异常
                GenericRpcResponse<Void> errorResponse = GenericRpcResponse.fail(e.getMessage(), Void.class);
                try (PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
                    writer.println(JsonSerializer.serialize(errorResponse));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        // 反射调用服务实现类的方法
        private Object invokeTargetMethod(GenericRpcRequest<?> request) throws Exception {
            String serviceName = request.getServiceName();
            Object serviceImpl = serviceMap.get(serviceName);
            if (serviceImpl == null) {
                throw new RuntimeException("服务未注册：" + serviceName);
            }

            // 获取目标方法（根据方法名和参数类型）
            Method method = serviceImpl.getClass().getMethod(
                    request.getMethodName(),
                    request.getParameterType()
            );
            // 调用方法
            String req = JsonSerializer.serialize(request.getParameter());
            return method.invoke(serviceImpl, JsonSerializer.deserialize(req, request.getParameterType()));
        }
    }
}
