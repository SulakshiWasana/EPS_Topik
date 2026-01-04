/**
 * Created By Dilsha Prasanna
 * Date : 1/5/2024
 * Time : 2:21 PM
 * Project Name : upay-sso
 */

package com.sejong.academy.eps_topik.exception;

public class MissingParameterException extends RuntimeException {

    public MissingParameterException(String message) {
        super(message);
    }

    public MissingParameterException(String message, Throwable cause) {
        super(message, cause);
    }
}