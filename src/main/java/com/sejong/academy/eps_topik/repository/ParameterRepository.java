package com.sejong.academy.eps_topik.repository;

import com.sejong.academy.eps_topik.entities.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParameterRepository extends JpaRepository<Parameter, Long> {

    Parameter findParameterByName(String parameter);
}
