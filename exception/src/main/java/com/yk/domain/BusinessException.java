package com.yk.domain;

/**
 * 业务异常
 */
public class BusinessException extends BasicException {

    private static final long serialVersionUID = 1L;

    public BusinessException(int code, String message) {
        super(code, message);
    }
}