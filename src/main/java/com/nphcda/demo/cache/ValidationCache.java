package com.nphcda.demo.cache;

import com.nphcda.demo.entity.Validation;
import com.nphcda.demo.repo.ValidationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@Transactional(readOnly = true)
public class ValidationCache {

    @Autowired
    ValidationRepo validationRepo;

    @Cacheable(value = "validationCache", key = "#qrcode")
    public List<Validation> getValidation(String qrcode) {
        System.out.println("Retrieving from Database for qrcode: " + qrcode);

        return validationRepo.findByQrcode(qrcode);

    }


}
