package com.sejong.academy.eps_topik.util;

public class ResponseCodeUtil {

    public static final String SUCCESS_CODE = "0000";
    public static final String INTERNAL_SERVER_ERROR_CODE = "1010";
    public static final String FAILED_CODE = "2020";

    public static final String PARAMETER_MISSING = "2026";

    public static final String FAILED = "FAILED";
    public static final String DECRYPTION_FAILED = "2027";


    //for input details
    public static final String INPUT_VALIDATION_ERROR_CODE = "3000";
    public static final String USER_NOT_FOUND_ERROR_CODE = "3001";
    public static final String INVALID_INPUT_FORMAT_ERROR_CODE = "3002";
    public static final String INVALID_PASSWORD_ERROR_CODE = "3003";
    public static final String DEVICE_NOT_FOUND_ERROR_CODE = "3004";
    public static final String INVALID_INPUT_ERROR_CODE = "3005"; //wrong value
    public static final String INVALID_CREDENTIAL_ERROR_CODE = "3006";
    public static final String INVALID_FCM_TOKEN_ERROR_CODE = "3007";
    public static final String PUBLIC_KEY_EMPTY_ERROR_CODE = "3008";

    //for jwt token
    public static final String JWT_TOKEN_VALIDATE_ERROR_CODE = "4000";
    public static final String JWT_TOKEN_EXPIRED_ERROR_CODE = "4001";
    public static final String INVALID_TOKEN_ERROR_CODE = "4002";
    public static final String OTP_EXPIRED = "4006";
    public static final String DEVICE_ID_MISMATCH_ERROR_CODE = "402";


    //for otp
    public static final String OTP_SENT_FAILED_ERROR_CODE = "5000";
    public static final String INVALID_OTP_ERROR_CODE = "5001";
    public static final String OTP_ATTEMPTS_EXCEED_ERROR_CODE = "5002";
    public static final String OTP_NOT_FOUND_ERROR_CODE = "5003";
    public static final String OTP_VERIFICATION_FAILED_ERROR_CODE = "5004";
    public static final String TIME_EXCEED_ERROR_CODE = "5005";


    //
    public static final String USER_EXISTS_ERROR_CODE = "6001";
    public static final String DISABLE_USER_ERROR_CODE = "6002";
    public static final String DEVICE_LIMIT_EXCEED_ERROR_CODE = "6003";
    public static final String INVALID_LOGIN_METHOD_ERROR_CODE = "6004";
    public static final String USER_NOT_ACTIVE_ERROR_CODE = "6005";
    public static final String LOCKED_USER_ERROR_CODE = "6006";
    public static final String REGISTER_INCOMPLETE_USER_ERROR_CODE = "6007";


    public static final String CANNOT_FIND_USER_ROLES = "6010";
    public static final String USER_DOESNT_HAVE_PERMISSION = "6020";
    public static final String CANNOT_FIND_USER = "6030";
    public static final String EMPTY_PARAMETER = "6050";

    public static final String INCOMPLETE_REGISTRATION = "Incomplete Registration!";
    public static final String INCOMPLETE_REGISTRATION_CODE = "5030";

}
