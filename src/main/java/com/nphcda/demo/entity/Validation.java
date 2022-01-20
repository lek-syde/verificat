package com.nphcda.demo.entity;

import org.springframework.stereotype.Indexed;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Indexed
public class Validation implements Serializable{

    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    String start;
    String end;
    String qrcode;
    String location;
    String submissiontime;
    String submittedby;
    String deviceID;
    String watermarked;


    private String image;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSubmissiontime() {
        return submissiontime;
    }

    public void setSubmissiontime(String submissiontime) {
        this.submissiontime = submissiontime;
    }

    public String getSubmittedby() {
        return submittedby;
    }

    public void setSubmittedby(String submittedby) {
        this.submittedby = submittedby;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getWatermarked() {
        return watermarked;
    }

    public void setWatermarked(String watermarked) {
        this.watermarked = watermarked;
    }


}
