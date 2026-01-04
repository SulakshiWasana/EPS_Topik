package com.sejong.academy.eps_topik.exception;

import com.sejong.academy.eps_topik.model.response.DefaultResponse;
import com.sejong.academy.eps_topik.util.ResponseCodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BadRequestHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("BadRequestHandler-> input validation error");

        Map<String, Object> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        DefaultResponse thirdPartyDefaultResponse = DefaultResponse.builder()
                .code(ResponseCodeUtil.INPUT_VALIDATION_ERROR_CODE)
                .title(ResponseCodeUtil.FAILED)
                .message("Input validation error")
                .data(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(thirdPartyDefaultResponse);
    }
}