package net.noahf.firegen.backend.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
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
        return new ApiResponse<>(false, new ErrorResponse(exception)).complete();
    }

    public static ResponseEntity<?> respond(Supplier<?> supplier) {
        try {
            ApiResponse<?> response = new ApiResponse<>(true, supplier.get());
            if (response.message == null) {
                throw new IllegalStateException("Expected data with response, received 'null'");
            }
            return response.complete();
        } catch (Exception exception) {
            ApiResponse<?> response = new ApiResponse<>(false, new ErrorResponse(exception));
            if (exception instanceof ResponseStatusException error) {
                response = new ApiResponse<>(false, new ErrorResponse(error.getCause() != null ? error.getCause() : error));
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

    public static class ErrorResponse {
        public String type;
        public String error;
        public ErrorResponse causedBy;

        private transient final int depth;

        public ErrorResponse(Throwable exception) {
            this(exception, 0);
        }

        private ErrorResponse(Throwable exception, int depth) {
            this.type = exception.getClass().getSimpleName();
            this.error = exception.getMessage();
            this.depth = depth;
            if (exception.getCause() != null) {
                if (this.depth < 10) {
                    causedBy = new ErrorResponse(exception.getCause(), depth + 1);
                } else {
                    causedBy = new ErrorResponse(new IllegalStateException(
                            "Cannot recurse past this depth of exceptions (depth=" + depth + "), last exception and will show no further: " + exception.getCause()
                    ));
                }
            }
        }
    }

}
