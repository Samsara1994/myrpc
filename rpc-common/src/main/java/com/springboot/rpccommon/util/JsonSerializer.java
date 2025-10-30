package com.springboot.rpccommon.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSerializer {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // 序列化：对象→JSON
    public static <T> String serialize(T obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化失败", e);
        }
    }

    // 反序列化：JSON→泛型对象（关键：通过TypeReference保留泛型类型）
    public static <T> T deserialize(String json, TypeReference<T> typeRef) {
        try {
            return OBJECT_MAPPER.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("反序列化失败", e);
        }
    }

    // 新增：支持JavaType反序列化（用于动态泛型类型）
    public static <T> T deserialize(String json, JavaType javaType) {
        try {
            return OBJECT_MAPPER.readValue(json, javaType);
        } catch (Exception e) {
            throw new RuntimeException("反序列化失败：" + e.getMessage(), e);
        }
    }

    // 新增：反序列化（根据 Class 类型）
    public static <T> T deserialize(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            // 核心：Jackson 会根据 clazz 类型将 JSON 转为对应对象
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("反序列化失败（类型：" + clazz.getName() + "）：" + e.getMessage(), e);
        }
    }
}
