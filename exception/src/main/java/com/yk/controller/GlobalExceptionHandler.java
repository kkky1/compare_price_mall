package com.yk.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yk.entity.BasicException;
import com.yk.entity.BusinessException;
import com.yk.entity.ExceptionEntity;
import com.yk.utils.ResultJsonTools;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局异常控制类
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    ResultJsonTools resultJsonTools;

    /**
     * 404异常处理
     */
    @ExceptionHandler(value = BusinessException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView errorHandler(HttpServletRequest request, BusinessException exception, HttpServletResponse response) {
        return commonHandler(request, response,
                exception.getClass().getSimpleName(),
                HttpStatus.NOT_FOUND.value(),
                exception.getMessage());
    }


    /**
     * 405异常处理
     */
 /*   @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ModelAndView errorHandler(HttpServletRequest request, HttpRequestMethodNotSupportedException exception, HttpServletResponse response) {
        return commonHandler(request, response,
                exception.getClass().getSimpleName(),
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                exception.getDetailMessageCode());
    }*/

    /**
     * 415异常处理
     */
/*    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ModelAndView errorHandler(HttpServletRequest request, HttpMediaTypeNotSupportedException exception, HttpServletResponse response) {
        return commonHandler(request, response,
                exception.getClass().getSimpleName(),
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                exception.getDetailMessageCode());
    }*/

    /**
     * 500异常处理
     */
    @ExceptionHandler(value = Exception.class)
    public ModelAndView errorHandler (HttpServletRequest request, Exception exception, HttpServletResponse response) {
        return commonHandler(request, response,
                exception.getClass().getSimpleName(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                exception.getMessage());
    }


    @ExceptionHandler(value = BasicException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 或者用 HttpStatus.INTERNAL_SERVER_ERROR，看你需求
    public ModelAndView handleBasicException(HttpServletRequest request, BasicException exception, HttpServletResponse response) {
        return commonHandler(request, response,
                exception.getClass().getSimpleName(),
                exception.getCode(),
                exception.getMessage());
    }




    /**
     * 表单验证异常处理
     */
    @ExceptionHandler(value = BindException.class)
    @ResponseBody
    public ExceptionEntity validExceptionHandler(BindException exception, HttpServletRequest request, HttpServletResponse response) {
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        Map<String,String> errors = new HashMap<>();
        for (FieldError error:fieldErrors) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        ExceptionEntity entity = new ExceptionEntity();
        entity.setMessage(JSON.toJSONString(errors));
        entity.setPath(request.getRequestURI());
        entity.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        entity.setError(exception.getClass().getSimpleName());
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return entity;
    }

    /**
     * 异常处理数据处理
     */
    private ModelAndView commonHandler (HttpServletRequest request, HttpServletResponse response,
                                        String error, int httpCode, String message) {
        ExceptionEntity entity = new ExceptionEntity();
        entity.setPath(request.getRequestURI());
        entity.setError(error);
        entity.setCode(httpCode);
        entity.setMessage(message);
        return determineOutput(request, response, entity);
    }

    /**
     * 异常输出处理
     */
    private ModelAndView determineOutput(HttpServletRequest request, HttpServletResponse response, ExceptionEntity entity) {
        if (!(
                request.getHeader("accept").contains("application/json")
                || (request.getHeader("X-Requested-With") != null && request.getHeader("X-Requested-With").contains("XMLHttpRequest"))
        )) {
            ModelAndView modelAndView = new ModelAndView("error");
            modelAndView.addObject("exception", entity);
            return modelAndView;
        } else {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setCharacterEncoding("UTF8");
            response.setHeader("Content-Type", "application/json");
            try {
                response.getWriter().write(JSONObject.toJSONString(entity));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}