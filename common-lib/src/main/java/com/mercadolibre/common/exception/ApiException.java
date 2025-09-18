package com.mercadolibre.common.exception;

import java.time.LocalDateTime;
import java.util.Map;

public class ApiException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;
    private final LocalDateTime timestamp;
    private final Map<String, Object> details;

    public ApiException(String message) {
        super(message);
        this.errorCode = ErrorCode.INTERNAL_ERROR;
        this.httpStatus = ErrorCode.getHttpStatusCode(ErrorCode.INTERNAL_ERROR);
        this.timestamp = LocalDateTime.now();
        this.details = null;
    }

    public ApiException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = ErrorCode.getHttpStatusCode(errorCode);
        this.timestamp = LocalDateTime.now();
        this.details = null;
    }

    public ApiException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.timestamp = LocalDateTime.now();
        this.details = null;
    }

    public ApiException(String message, Throwable cause, String errorCode, int httpStatus) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.timestamp = LocalDateTime.now();
        this.details = null;
    }

    public ApiException(String message, String errorCode, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = ErrorCode.getHttpStatusCode(errorCode);
        this.timestamp = LocalDateTime.now();
        this.details = details;
    }

    public ApiException(String message, String errorCode, int httpStatus, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.timestamp = LocalDateTime.now();
        this.details = details;
    }

    // Static factory methods for common exceptions
    public static ApiException notFound(String message) {
        return new ApiException(message, ErrorCode.NOT_FOUND);
    }

    public static ApiException validationError(String message) {
        return new ApiException(message, ErrorCode.VALIDATION_ERROR);
    }

    public static ApiException validationError(String message, Map<String, Object> details) {
        return new ApiException(message, ErrorCode.VALIDATION_ERROR, details);
    }

    public static ApiException unauthorized(String message) {
        return new ApiException(message, ErrorCode.UNAUTHORIZED);
    }

    public static ApiException forbidden(String message) {
        return new ApiException(message, ErrorCode.FORBIDDEN);
    }

    public static ApiException badRequest(String message) {
        return new ApiException(message, ErrorCode.BAD_REQUEST);
    }

    public static ApiException inventoryNotFound(String storeId, String productId) {
        String message = String.format("Inventory not found for store: %s, product: %s", storeId, productId);
        return new ApiException(message, ErrorCode.INVENTORY_NOT_FOUND);
    }

    public static ApiException insufficientInventory(String storeId, String productId, int requested, int available) {
        String message = String.format("Insufficient inventory for store: %s, product: %s. Requested: %d, Available: %d", 
                storeId, productId, requested, available);
        Map<String, Object> details = Map.of(
                "storeId", storeId,
                "productId", productId,
                "requested", requested,
                "available", available
        );
        return new ApiException(message, ErrorCode.INSUFFICIENT_INVENTORY, details);
    }

    public static ApiException storeNotFound(String storeId) {
        String message = String.format("Store not found: %s", storeId);
        return new ApiException(message, ErrorCode.STORE_NOT_FOUND);
    }

    public static ApiException rateLimitExceeded(String message) {
        return new ApiException(message, ErrorCode.RATE_LIMIT_EXCEEDED);
    }

    public static ApiException syncConflict(String message) {
        return new ApiException(message, ErrorCode.CONFLICT_RESOLUTION_FAILED);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return "ApiException{" +
                "errorCode='" + errorCode + '\'' +
                ", httpStatus=" + httpStatus +
                ", timestamp=" + timestamp +
                ", message='" + getMessage() + '\'' +
                ", details=" + details +
                '}';
    }
}
