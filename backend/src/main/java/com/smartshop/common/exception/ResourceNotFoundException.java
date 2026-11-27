package com.smartshop.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String errorCode, String message) {
        super(HttpStatus.NOT_FOUND, errorCode, message);
    }
}
