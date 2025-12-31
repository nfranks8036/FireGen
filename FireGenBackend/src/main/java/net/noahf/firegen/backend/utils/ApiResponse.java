package net.noahf.firegen.backend.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.function.Supplier;

public class ApiResponse<T> {

    private boolean success;
    private T message;
    private HttpStatusCode status;

    ApiResponse(boolean success, T message) {
        this(success, message, success ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    ApiResponse(boolean success, T message, HttpStatus status) {
        this.success = success;
        this.message = message;
        this.status = status;
    }

    public static <T> ResponseEntity<?> success(T data) {
        return new ApiResponse<>(true, data).complete();
    }

    public static ResponseEntity<?> fail(Throwable exception) {
        return new ApiResponse<>(true, exception.toString()).complete();
    }

    public static ResponseEntity<?> respond(Supplier<?> supplier) {
        try {
            ApiResponse<?> response = new ApiResponse<>(true, supplier.get());
            if (response.message == null) {
                throw new IllegalStateException("Expected data with response, received 'null'");
            }
            return response.complete();
        } catch (Exception exception) {
            ApiResponse<?> response = new ApiResponse<>(false, exception.toString());
            if (exception instanceof ResponseStatusException error) {
                response = new ApiResponse<>(false, (error.getCause() != null ? error.getCause().toString() : error.getReason()));
                response.status = error.getStatusCode();
            }
            exception.printStackTrace(System.err);
            return response.complete();
        }
    }



    public boolean isSuccess() {
        return this.success;
    }

    public T getMessage() {
        return this.message;
    }

    public ResponseEntity<?> complete() {
        return new ResponseEntity<>(this, this.status);
    }

}
