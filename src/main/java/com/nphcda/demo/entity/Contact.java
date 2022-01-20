package com.nphcda.demo.entity;


import org.springframework.stereotype.Indexed;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
@Indexed
public class Contact implements Serializable {

    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long Id;


    String state;
    String name;
    String phone;
    String statecode;
    String email;
    String validationDate;




    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatecode() {
        return statecode;
    }

    public void setStatecode(String statecode) {
        this.statecode = statecode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getValidationDate() {
        return validationDate;
    }

    public void setValidationDate(String validationDate) {
        this.validationDate = validationDate;
    }
}
