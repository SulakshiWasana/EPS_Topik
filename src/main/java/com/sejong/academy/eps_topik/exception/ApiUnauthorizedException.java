package com.sejong.academy.eps_topik.exception;

import lombok.Getter;

@Getter
public class ApiUnauthorizedException extends Exception {

    private final String message;

    public ApiUnauthorizedException(String message) {
        this.message = message;
    }
}
