package com.sejong.academy.eps_topik.exception;

import lombok.Getter;

@Getter
public class UPayResponseException extends Exception {

    private final String message;

    public UPayResponseException(String message) {
        this.message = message;
    }
}