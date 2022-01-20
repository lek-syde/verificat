package com.nphcda.demo.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;


@Entity
public class Validator {

    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long Id;

    String device;

    String name;


    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
