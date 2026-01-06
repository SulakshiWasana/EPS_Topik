package com.sejong.academy.eps_topik.controllers;

import com.sejong.academy.eps_topik.model.request.AdminUserRegisterRequest;
import com.sejong.academy.eps_topik.model.response.CommonResponse;
import com.sejong.academy.eps_topik.model.response.DefaultResponse;
import com.sejong.academy.eps_topik.service.InternalService;
import com.sejong.academy.eps_topik.util.ReturnResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal")
public class InternalController {

    private final InternalService internalService;

    @PostMapping("/register-admin")
    public ResponseEntity<DefaultResponse> registerAdminUser(@Valid @RequestBody AdminUserRegisterRequest request) {
        CommonResponse commonResponse = internalService.registerAdminUser(request);
        return ReturnResponseUtil.returnResponse(commonResponse);
    }

}
