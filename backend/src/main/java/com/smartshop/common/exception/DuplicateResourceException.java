package com.smartshop.common.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(String errorCode, String message) {
        super(HttpStatus.CONFLICT, errorCode, message);
    }
}
