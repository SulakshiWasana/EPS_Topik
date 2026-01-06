package com.sejong.academy.eps_topik.util;

import com.sejong.academy.eps_topik.model.response.CommonResponse;
import com.sejong.academy.eps_topik.model.response.DefaultResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReturnResponseUtil {
    public static ResponseEntity<DefaultResponse> returnResponse(CommonResponse commonResponse) {
        if (commonResponse.getCode().equals(ResponseCodeUtil.SUCCESS_CODE)) {
            log.info(LogMessageUtil.RETURN_RESPONSE_UTIL, LogMessageUtil.SUCCESS_RESPONSE);
            return ResponseEntity.ok(DefaultResponse.builder()
                    .code(commonResponse.getCode())
                    .title(commonResponse.getTitle())
                    .message(commonResponse.getMessage())
                    .data(commonResponse.getData())
                    .build());
        } else if (commonResponse.getCode().equals(ResponseCodeUtil.INTERNAL_SERVER_ERROR_CODE)) {
            log.info(LogMessageUtil.RETURN_RESPONSE_UTIL, LogMessageUtil.INTERNAL_SERVER_ERROR_RESPONSE);
            return ResponseEntity.internalServerError().body(DefaultResponse.builder()
                    .code(commonResponse.getCode())
                    .title(commonResponse.getTitle())
                    .message(commonResponse.getMessage())
                    .data(commonResponse.getData())
                    .build());
        } else {
            log.info(LogMessageUtil.RETURN_RESPONSE_UTIL, LogMessageUtil.FAILED_RESPONSE);
            return ResponseEntity.badRequest().body(DefaultResponse.builder()
                    .code(commonResponse.getCode())
                    .title(commonResponse.getTitle())
                    .message(commonResponse.getMessage())
                    .data(commonResponse.getData())
                    .build());
        }
    }
}
