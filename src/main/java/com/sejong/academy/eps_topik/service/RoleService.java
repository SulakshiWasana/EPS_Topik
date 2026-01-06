package com.sejong.academy.eps_topik.service;

import com.sejong.academy.eps_topik.entities.Role;
import com.sejong.academy.eps_topik.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public Role getRoleByName(String name) {
        return roleRepository.findRoleByName(name);
    }

}
