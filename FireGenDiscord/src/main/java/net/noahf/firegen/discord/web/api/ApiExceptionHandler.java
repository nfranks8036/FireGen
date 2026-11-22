package net.noahf.firegen.discord.web.api;

import jakarta.servlet.http.HttpServletRequest;
import net.noahf.firegen.discord.users.UnauthorizedException;
import net.noahf.firegen.discord.utilities.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@SuppressWarnings("unused")
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(
            MissingServletRequestParameterException exception
    ) {
        return ApiResponse.fail("Missing required parameter: " + exception.getParameterName());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(
            NoResourceFoundException ignored,
            HttpServletRequest req
    ) {
        return ApiResponse.notFound("Endpoint not found: " + req.getRequestURI());
    }

    @ExceptionHandler(exception = {BadCredentialsException.class, UnauthorizedException.class})
    public ResponseEntity<ApiResponse<?>> handleNotAuthorized(
            Exception ignored,
            HttpServletRequest req
    ) {
        return ApiResponse.status(HttpStatus.UNAUTHORIZED, false, "Invalid credentials.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception exception) {
        Log.error("An unexpected API error occurred: " + exception, exception);
        return ApiResponse.fail("An internal API error has occurred.", exception);
    }

}
