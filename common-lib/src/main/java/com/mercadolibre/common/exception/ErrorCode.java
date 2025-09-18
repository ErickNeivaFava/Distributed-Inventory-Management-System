package com.mercadolibre.common.exception;

public final class ErrorCode {

    // General errors
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String FORBIDDEN = "FORBIDDEN";
    public static final String BAD_REQUEST = "BAD_REQUEST";

    // Inventory specific errors
    public static final String INVENTORY_NOT_FOUND = "INVENTORY_NOT_FOUND";
    public static final String INSUFFICIENT_INVENTORY = "INSUFFICIENT_INVENTORY";
    public static final String INVENTORY_UPDATE_CONFLICT = "INVENTORY_UPDATE_CONFLICT";

    // Store specific errors
    public static final String STORE_NOT_FOUND = "STORE_NOT_FOUND";
    public static final String STORE_ALREADY_EXISTS = "STORE_ALREADY_EXISTS";

    // Sync specific errors
    public static final String SYNC_FAILED = "SYNC_FAILED";
    public static final String CONFLICT_RESOLUTION_FAILED = "CONFLICT_RESOLUTION_FAILED";

    // Authentication & Authorization errors
    public static final String INVALID_TOKEN = "INVALID_TOKEN";
    public static final String TOKEN_EXPIRED = "TOKEN_EXPIRED";
    public static final String ACCESS_DENIED = "ACCESS_DENIED";

    // Rate limiting errors
    public static final String RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED";

    // Database errors
    public static final String DATABASE_ERROR = "DATABASE_ERROR";
    public static final String OPTIMISTIC_LOCKING_FAILURE = "OPTIMISTIC_LOCKING_FAILURE";

    // External service errors
    public static final String EXTERNAL_SERVICE_UNAVAILABLE = "EXTERNAL_SERVICE_UNAVAILABLE";
    public static final String EXTERNAL_SERVICE_TIMEOUT = "EXTERNAL_SERVICE_TIMEOUT";

    private ErrorCode() {
        // Utility class
    }

    public static String getHttpStatusMessage(String errorCode) {
        switch (errorCode) {
            case NOT_FOUND:
            case INVENTORY_NOT_FOUND:
            case STORE_NOT_FOUND:
                return "Resource not found";
            case UNAUTHORIZED:
            case INVALID_TOKEN:
            case TOKEN_EXPIRED:
                return "Authentication required";
            case FORBIDDEN:
            case ACCESS_DENIED:
                return "Access denied";
            case BAD_REQUEST:
            case VALIDATION_ERROR:
                return "Invalid request";
            case RATE_LIMIT_EXCEEDED:
                return "Rate limit exceeded";
            case EXTERNAL_SERVICE_UNAVAILABLE:
                return "External service unavailable";
            default:
                return "Internal server error";
        }
    }

    public static int getHttpStatusCode(String errorCode) {
        switch (errorCode) {
            case NOT_FOUND:
            case INVENTORY_NOT_FOUND:
            case STORE_NOT_FOUND:
                return 404;
            case UNAUTHORIZED:
            case INVALID_TOKEN:
            case TOKEN_EXPIRED:
                return 401;
            case FORBIDDEN:
            case ACCESS_DENIED:
                return 403;
            case BAD_REQUEST:
            case VALIDATION_ERROR:
            case INSUFFICIENT_INVENTORY:
            case INVENTORY_UPDATE_CONFLICT:
            case STORE_ALREADY_EXISTS:
                return 400;
            case RATE_LIMIT_EXCEEDED:
                return 429;
            case EXTERNAL_SERVICE_UNAVAILABLE:
            case EXTERNAL_SERVICE_TIMEOUT:
                return 503;
            default:
                return 500;
        }
    }
}
