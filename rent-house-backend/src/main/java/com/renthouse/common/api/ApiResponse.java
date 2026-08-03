package com.renthouse.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(String code, String message, T data, String traceId) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>("OK", "success", data, null);
    }

    public static ApiResponse<Void> ok() {
        return ok(null);
    }

    public static <T> ApiResponse<T> fail(String code, String message) {
        return new ApiResponse<>(code, message, null, null);
    }
}
