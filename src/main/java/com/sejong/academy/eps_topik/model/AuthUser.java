package com.sejong.academy.eps_topik.model;

import com.sejong.academy.eps_topik.entities.AppUser;

public class AuthUser extends org.springframework.security.core.userdetails.User {
    public static final String TYPE_USER = "user";

    private static final long serialVersionUID = 1L;

    public AuthUser(AppUser appUser) {
        super(appUser.getUsername(), appUser.getPassword() == null ? "" : appUser.getPassword(), appUser.getGrantedAuthoritiesList());
    }
}
