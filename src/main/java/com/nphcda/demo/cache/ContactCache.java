package com.nphcda.demo.cache;

import com.nphcda.demo.entity.Contact;
import com.nphcda.demo.entity.Validation;
import com.nphcda.demo.repo.ContactRepo;
import com.nphcda.demo.repo.ValidationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@Transactional(readOnly = true)
public class ContactCache {

    @Autowired
    ContactRepo contactRepo;

    @Cacheable(value = "contactCache", key = "#statecode")
    public List<Contact> getContactPerson(String statecode) {
        System.out.println("Retrieving from Database for contact: " + statecode);

        return contactRepo.findByStatecode(statecode);

    }

    @Cacheable(value = "contactCachetwo", key = "#validationdate")
    public Contact getValidationDate(String statecode) {
        System.out.println("Retrieving from Database for contact: " + statecode);

        return contactRepo.findFirstByStatecode(statecode);

    }
}
