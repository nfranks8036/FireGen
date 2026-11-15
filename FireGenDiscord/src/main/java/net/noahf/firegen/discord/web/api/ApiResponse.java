package net.noahf.firegen.discord.web.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public record ApiResponse<T>(
        boolean success,
        T response
) {

    public static ResponseEntity<ApiResponse<?>> ok(Object response) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, response)
        );
    }

    public static ResponseEntity<ApiResponse<?>> fail(String message) {
        return ResponseEntity.badRequest().body(
                new ApiResponse<>(false, message)
        );
    }

    public static ResponseEntity<ApiResponse<?>> fail(String message, Throwable throwable) {
        return ResponseEntity.badRequest().body(
                new ApiResponse<>(false, new IncludeError(message, throwable.toString()))
        );
    }

    public static ResponseEntity<ApiResponse<?>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ApiResponse<>(false, message)
        );
    }



    public record IncludeError(String errorGeneral, String errorExact) {

    }

}
