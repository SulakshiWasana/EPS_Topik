package com.sejong.academy.eps_topik.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserRegisterRequest {
    private String password;
    private String role;
    private String name;
    private String nic;
}
