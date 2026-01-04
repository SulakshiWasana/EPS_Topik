/**
 * Created By Dilsha Prasanna
 * Date : 2/27/2024
 * Time : 5:54 PM
 * Project Name : upay-sso
 */

package com.sejong.academy.eps_topik.exception;

public class DeviceIdMisMatchException extends RuntimeException {

    public DeviceIdMisMatchException(String message) {
        super(message);
    }

    public DeviceIdMisMatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
