package com.sejong.academy.eps_topik.repository;

import com.sejong.academy.eps_topik.entities.AdminUser;
import com.sejong.academy.eps_topik.entities.AppUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminUserRepository extends CrudRepository<AdminUser, Long> {

    AdminUser findAdminUserByNic(String nic);

}
