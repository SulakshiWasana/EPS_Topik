package com.sejong.academy.eps_topik.model.response;

import com.sejong.academy.eps_topik.enums.ResponseStatus;
import com.sejong.academy.eps_topik.util.AppConstants;
import com.sejong.academy.eps_topik.util.ResponseCodeUtil;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class DefaultResponse {
    private String code = ResponseCodeUtil.FAILED_CODE;
    private String title;
    private String message;
    private Object data = new Object();

    public DefaultResponse(String code, String title, String message) {
        this.code = code;
        this.title = title;
        this.message = message;
        this.data = new Object();
    }


    public static DefaultResponse success(String title, String message_code) {
        return new DefaultResponse(ResponseCodeUtil.SUCCESS_CODE, title, message_code);
    }

    public static DefaultResponse success(String title, String message, Object data) {
        return new DefaultResponse(ResponseCodeUtil.SUCCESS_CODE, title, message, data);
    }


    public static DefaultResponse error() {
        String title = ResponseStatus.FAILED.name();
        String message = AppConstants.DEFAULT_ERROR;
        return new DefaultResponse(ResponseCodeUtil.FAILED_CODE, title, message);
    }

    public static DefaultResponse error(String title, String message) {
        return new DefaultResponse(ResponseCodeUtil.FAILED_CODE, title, message);
    }

    public static DefaultResponse error(String title, String message, Object data) {
        return new DefaultResponse(ResponseCodeUtil.FAILED_CODE, title, message, data);
    }

    public DefaultResponse setMessage(String message) {
        this.message = message;
        return this;
    }

}
