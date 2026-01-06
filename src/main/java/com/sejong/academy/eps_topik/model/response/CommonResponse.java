package com.sejong.academy.eps_topik.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonResponse {
    private String code;
    private String title;
    private String message;
    private Object data = new Object();

    public CommonResponse(String code, String title, String message) {
        this.code = code;
        this.title = title;
        this.message = message;
        this.data = new Object();
    }
}
