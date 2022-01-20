package com.nphcda.demo.repo;

import com.nphcda.demo.entity.Validator;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValidatorRepo extends JpaRepository<Validator, Long> {

 boolean existsByDevice(String device);
}
