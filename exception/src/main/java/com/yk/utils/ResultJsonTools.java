package com.yk.utils;

import com.alibaba.fastjson.JSON;
import com.yk.domain.ExceptionEntity;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * JSON响应工具类
 */
@Component
public class ResultJsonTools {

    /**
     * 返回成功响应
     */
    public static Map<String, Object> success() {
        return success(null, "操作成功");
    }

    /**
     * 返回成功响应
     */
    public static Map<String, Object> success(Object data) {
        return success(data, "操作成功");
    }

    /**
     * 返回成功响应
     */
    public static Map<String, Object> success(Object data, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("code", 200);
        result.put("message", message);
        result.put("data", data);
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 返回失败响应
     */
    public static Map<String, Object> error(String message) {
        return error(500, message, null);
    }

    /**
     * 返回失败响应
     */
    public static Map<String, Object> error(int code, String message) {
        return error(code, message, null);
    }

    /**
     * 返回失败响应
     */
    public static Map<String, Object> error(int code, String message, Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("code", code);
        result.put("message", message);
        result.put("data", data);
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 根据异常实体返回失败响应
     */
    public static Map<String, Object> error(ExceptionEntity entity) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("code", entity.getCode());
        result.put("message", entity.getMessage());
        result.put("data", entity);
        return result;
    }

    /**
     * 直接向响应写入JSON数据
     */
    public static void writeJson(HttpServletResponse response, Object data) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        
        try (PrintWriter writer = response.getWriter()) {
            writer.write(JSON.toJSONString(data));
            writer.flush();
        } catch (IOException e) {
            System.err.println("写入JSON响应失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 写入成功响应
     */
    public static void writeSuccess(HttpServletResponse response) {
        writeJson(response, success());
    }

    /**
     * 写入成功响应
     */
    public static void writeSuccess(HttpServletResponse response, Object data) {
        writeJson(response, success(data));
    }

    /**
     * 写入成功响应
     */
    public static void writeSuccess(HttpServletResponse response, Object data, String message) {
        writeJson(response, success(data, message));
    }

    /**
     * 写入错误响应
     */
    public static void writeError(HttpServletResponse response, String message) {
        response.setStatus(500);
        writeJson(response, error(message));
    }

    /**
     * 写入错误响应
     */
    public static void writeError(HttpServletResponse response, int code, String message) {
        response.setStatus(code);
        writeJson(response, error(code, message));
    }

    /**
     * 写入异常响应
     */
    public static void writeError(HttpServletResponse response, ExceptionEntity entity) {
        response.setStatus(entity.getCode());
        writeJson(response, error(entity));
    }

    /**
     * 转换为JSON字符串
     */
    public static String toJsonString(Object obj) {
        return JSON.toJSONString(obj);
    }
}