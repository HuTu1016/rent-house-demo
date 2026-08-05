package com.renthouse.common.exception;

import com.renthouse.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.beans.factory.annotation.Value;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @Value("${app.errors.include-details:false}")
    private boolean includeDetails;
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException exception) {
        return ResponseEntity.status(exception.status()).body(ApiResponse.fail(exception.code(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception) {
        FieldError error = exception.getBindingResult().getFieldError();
        String message = error == null ? "请求参数不合法" : error.getField() + "：" + error.getDefaultMessage();
        return ResponseEntity.badRequest().body(ApiResponse.fail("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("Unhandled exception for request: {}", request.getRequestURI(), exception);
        String message = includeDetails ? detail(exception) : "服务暂时不可用";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("INTERNAL_ERROR", message));
    }

    private String detail(Throwable exception) {
        StringBuilder result = new StringBuilder();
        Throwable current = exception;
        int depth = 0;
        while (current != null && depth++ < 6) {
            if (result.length() > 0) result.append(" | cause: ");
            result.append(current.getClass().getSimpleName());
            if (current.getMessage() != null && !current.getMessage().isBlank()) result.append(": ").append(current.getMessage());
            current = current.getCause();
        }
        return result.toString();
    }
}
