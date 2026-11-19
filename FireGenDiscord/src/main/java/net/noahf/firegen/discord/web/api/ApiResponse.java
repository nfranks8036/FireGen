package net.noahf.firegen.discord.web.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

public record ApiResponse<T>(
        int status,
        boolean success,
        LocalDateTime timestamp,
        T response
) {

    public static ResponseEntity<ApiResponse<?>> ok(Object response) {
        return ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK, true, response)
        );
    }

    public static ResponseEntity<ApiResponse<?>> fail(String message) {
        return ResponseEntity.badRequest().body(
                new ApiResponse<>(HttpStatus.BAD_REQUEST, false, message)
        );
    }

    public static ResponseEntity<ApiResponse<?>> fail(String message, Throwable throwable) {
        return ResponseEntity.badRequest().body(
                new ApiResponse<>(HttpStatus.BAD_REQUEST, false, new IncludeError(message, throwable.toString()))
        );
    }

    public static ResponseEntity<ApiResponse<?>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ApiResponse<>(HttpStatus.NOT_FOUND, false, message)
        );
    }

    public ApiResponse(HttpStatus status, boolean success, T response) {
        this(status.value(), success, LocalDateTime.now(), response);
    }



    public record IncludeError(String errorGeneral, String errorExact) {

    }

}
