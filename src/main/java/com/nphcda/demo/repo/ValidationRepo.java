package com.nphcda.demo.repo;

import com.nphcda.demo.entity.Validation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ValidationRepo extends JpaRepository<Validation, Long> {


    List<Validation> findByQrcode(String qrcode);
    boolean existsByQrcode(String qrcode);


}
