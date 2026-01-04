package com.sejong.academy.eps_topik.repository;

import com.sejong.academy.eps_topik.entities.AppUser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepository extends CrudRepository<AppUser, Long> {

    @Query(value = "SELECT * FROM app_user WHERE username= :username", nativeQuery = true)
    AppUser findOneByUsername(@Param("username") String username);
}

