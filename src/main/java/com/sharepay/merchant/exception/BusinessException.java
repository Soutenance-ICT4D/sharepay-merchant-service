package com.sharepay.merchant.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

    private final String messageKey;
    private final HttpStatus status;

    public BusinessException(HttpStatus status, String messageKey, String message) {
        super(message);
        this.status = status;
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
