package com.sejong.academy.eps_topik.repository;

import com.sejong.academy.eps_topik.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findRoleById(long id);
    Role findRoleByName(String name);
}
