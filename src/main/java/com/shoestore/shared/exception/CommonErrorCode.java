package com.shoestore.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Shared error codes that are not owned by a specific business module.
 */
public enum CommonErrorCode implements ErrorCode {

    RESOURCE_NOT_FOUND(
            "RESOURCE_NOT_FOUND",
            HttpStatus.NOT_FOUND,
            "Requested resource was not found"
    ),

    RESOURCE_ALREADY_EXISTS(
            "RESOURCE_ALREADY_EXISTS",
            HttpStatus.CONFLICT,
            "Resource already exists"
    ),

    INVALID_REQUEST(
            "INVALID_REQUEST",
            HttpStatus.BAD_REQUEST,
            "Request is invalid"
    ),

    OPERATION_NOT_ALLOWED(
            "OPERATION_NOT_ALLOWED",
            HttpStatus.CONFLICT,
            "Operation is not allowed in the current state"
    );

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;

    CommonErrorCode(
            String code,
            HttpStatus httpStatus,
            String message
    ) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
