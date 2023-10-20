package com.fsa.syums.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JSONUtil {
    private final static ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static String objToString(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException:{}", e.getMessage());
            return null;
        }
    }

    public static <T> T strToObject(String str, Class<T> tClass) {
        try {
            return mapper.readValue(str, tClass);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException:{}", e.getMessage());
            return null;
        }
    }

    public static <T> T strToObject(String str, TypeReference<T> tTypeReference) {
        try {
            return mapper.readValue(str, tTypeReference);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException:{}", e.getMessage());
            return null;
        }
    }

    public static Map<String, Object> objToMap(Object obj) {
        try {
            String str = mapper.writeValueAsString(obj);
            return mapper.readValue(str, HashMap.class);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException:{}", e.getMessage());
            return null;
        }
    }
}
