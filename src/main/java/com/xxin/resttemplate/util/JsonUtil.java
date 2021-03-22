package com.xxin.resttemplate.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @description:
 * @author: chenyixin7
 * @create: 2021-03-19 17:25
 */

public final class JsonUtil {
    private static Logger logger = LoggerFactory.getLogger(JsonUtil.class);
    private static ObjectMapper objectMapper = new ObjectMapper();

    public JsonUtil() {
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static <T> String toJson(T pojo) {
        try {
            String json = objectMapper.writeValueAsString(pojo);
            return json;
        } catch (Exception var3) {
            throw new RuntimeException("transform json string error", var3);
        }
    }

    public static <T> T fromJson(String json, Class<T> type) {
        T pojo = null;
        try {
            pojo = objectMapper.readValue(json, type);
        } catch (Exception e) {
            logger.error("- parse json object error,json string:{}\n class type:{}",json,type);
            throw new RuntimeException("parse json object error",e);
        }
        return pojo;
    }

    public static <T> T toCollection(String jsonStr, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(jsonStr, typeReference);
        } catch (Exception var3) {
            logger.error("- parse json collection error,json string:{}\n class type:{}", jsonStr, typeReference);
            throw new RuntimeException("parse json collection error ", var3);
        }
    }

    public static <T> T toPojo(String jsonStr, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(jsonStr, typeReference);
        } catch (Exception var3) {
            logger.error("- parse json pojo error,json string:{}\n class type:{}", jsonStr, typeReference);
            throw new RuntimeException("parse json pojo error", var3);
        }
    }

    public static <T> T toPojo(String jsonStr, Class outer, Class inner) {
        try {
            JavaType javaType = objectMapper.getTypeFactory().constructParametricType(outer, new Class[]{inner});
            return objectMapper.readValue(jsonStr, javaType);
        } catch (Exception var4) {
            logger.error("- parse json pojo error,json string:{}\n class outer:{},class inner", new Object[]{jsonStr, outer, inner});
            throw new RuntimeException("parse json pojo with class type error", var4);
        }
    }

    public static Map<String, Object> json2map(String jsonStr) {
        try {
            return (Map)objectMapper.readValue(jsonStr, Map.class);
        } catch (Exception var2) {
            logger.error("- parse json pojo error,json string:{}", jsonStr);
            throw new RuntimeException("parse json map error", var2);
        }
    }

    public static <T> T map2pojo(Map map, Class<T> clazz) {
        return objectMapper.convertValue(map, clazz);
    }

    public static <T> T map2pojo(Map map, TypeReference<T> typeReference) {
        return objectMapper.convertValue(map, typeReference);
    }
}

