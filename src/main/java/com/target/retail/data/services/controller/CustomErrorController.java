package com.target.retail.data.services.controller;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

@RestController
public class CustomErrorController extends ResponseEntityExceptionHandler implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(WebRequest webRequest) {
        Map<String, Object> body = getErrorAttributes(webRequest, ErrorAttributeOptions.defaults());
        HttpStatus status = HttpStatus.valueOf((int) body.get("status"));
        return new ResponseEntity<>(body, status);
    }

    private Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        // Implement the logic to get error attributes
        // This is a placeholder implementation
        return Map.of(
                "status", 500,
                "error", "Internal Server Error",
                "message", "An unexpected error occurred",
                "error_message", options.toString()
        );
    }
}