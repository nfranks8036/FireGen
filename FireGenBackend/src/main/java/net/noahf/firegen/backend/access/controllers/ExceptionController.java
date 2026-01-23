package net.noahf.firegen.backend.access.controllers;

import jakarta.servlet.http.HttpServletRequest;
import net.noahf.firegen.backend.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleError(HttpServletRequest request, Exception exception) {
        System.err.println("Request " + request.getRequestURI() + " raised " + exception.toString());

        return ApiResponse.fail(exception);
    }


}
