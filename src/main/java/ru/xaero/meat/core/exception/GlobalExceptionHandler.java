package ru.xaero.meat.core.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoSuchElementException ex) {
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), ex, false);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), ex, false);
    }

    // чтобы "swagger-ui" / "/" не превращались в 500
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResource(NoResourceFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Not Found", "Resource not found", ex, false);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", "Файл слишком большой! Максимальный размер: 10MB", ex, false);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, Object>> handleIOException(IOException ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Ошибка при обработке файла", ex, true);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        // логируем ПОЛНЫЙ stacktrace
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Произошла внутренняя ошибка сервера", ex, true);
    }

    private ResponseEntity<Map<String, Object>> build(
            HttpStatus status,
            String error,
            String message,
            Exception ex,
            boolean includeCause
    ) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", error);
        response.put("message", message);

        if (includeCause) {
            Throwable root = ex;
            while (root.getCause() != null && root.getCause() != root) root = root.getCause();
            response.put("cause", root.getClass().getSimpleName() + ": " + root.getMessage());
        }

        return new ResponseEntity<>(response, status);
    }
}
