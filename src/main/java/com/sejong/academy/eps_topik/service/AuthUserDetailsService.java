package com.sejong.academy.eps_topik.service;


import com.sejong.academy.eps_topik.entities.AppUser;
import com.sejong.academy.eps_topik.entities.Role;
import com.sejong.academy.eps_topik.model.AuthUser;
import com.sejong.academy.eps_topik.repository.AppUserRepository;
import com.sejong.academy.eps_topik.repository.RoleRepository;
import com.sejong.academy.eps_topik.util.LogMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthUserDetailsService implements UserDetailsService {


    private final AppUserRepository userRepository;

    private final RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser;
        try {
            appUser = getAppUserDetails(username);
            if (appUser == null) {
                throw new UsernameNotFoundException("User " + username + " was not found in the database");
            }

            return new AuthUser(appUser);
        } catch (Exception e) {
            log.error(LogMessageUtil.EXCEPTION, e);
            throw new UsernameNotFoundException("User " + username + " was not found in the database");
        }
    }

    private AppUser getAppUserDetails(String userIdentify) {
        Collection<GrantedAuthority> grantedAuthoritiesList = new ArrayList<>();
        AppUser appUser = userRepository.findOneByUsername(userIdentify);
        if (appUser != null) {
            Hibernate.initialize(appUser.getRoles());
            appUser.getRoles().forEach(role -> {
                Role role1 = roleRepository.findRoleById(role.getId());
                if (role1 != null) {
                    Hibernate.initialize(role1.getPermissions());
                    role1.getPermissions().forEach(permission -> {
                        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(permission.getName());
                        grantedAuthoritiesList.add(grantedAuthority);
                    });
                }
            });
            appUser.setGrantedAuthoritiesList(grantedAuthoritiesList);
            return appUser;
        } else {
            log.warn(LogMessageUtil.NOT_FOUND + " user not found for this user identity : " + userIdentify);
            return null;
        }
    }

}
