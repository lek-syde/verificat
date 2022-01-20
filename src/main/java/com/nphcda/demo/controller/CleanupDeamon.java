package com.nphcda.demo.controller;

import com.nphcda.demo.DTO.VerificationEntity;
import com.nphcda.demo.entity.Validation;
import com.nphcda.demo.kobo.Validator;
import com.nphcda.demo.repo.ValidationRepo;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

@RestController
public class CleanupDeamon {


    @Autowired
    ValidationRepo validationRepo;

    @CrossOrigin(maxAge = 3600)
    @PostMapping(value = "/hookforvalidationdata")
    public void showIndex(Model model, @RequestBody Validator validationdata) throws IOException {

        Validation validation = new Validation();
        validation.setStart(validationdata.getStart());
        validation.setEnd(validationdata.getEnd());
        validation.setSubmissiontime(validationdata.getSubmissionTime());
        validation.setLocation(validationdata.getLocation());
        validation.setSubmittedby(validationdata.getSubmittedBy());
        validation.setDeviceID(validationdata.getDeviceid());
        validation.setQrcode(validationdata.getBarcode());

        try {

            String watermarkkedimg="https://neutrinoapi.net/image-watermark?image-url="+ validationdata.getAttachments().get(0).getDownloadSmallUrl()+"&watermark-url=https://res.cloudinary.com/nphcdaict/image/upload/c_scale,w_200/v1633926858/watermark_mvfiyj.png&api-key=YqXLmTK9RGaMBX56EDyYTMwKbjbEiOCSmmGbdiYdeAcxofh6&user-id=leksyde&opacity=30";

            validation.setImage(getBase64EncodedImage(watermarkkedimg));
            validation.setWatermarked("true");

            //https://neutrinoapi.net/image-watermark?image-url=https://kc.kobotoolbox.org/media/small?media_file=nphcdaemid%2Fattachments%2Fef760ad498194714b9c579702733e66a%2F0dca0051-adcf-4458-a5eb-e0317075f0fc%2F1637677592026.jpg&watermark-url=https://res.cloudinary.com/nphcdaict/image/upload/c_scale,w_200/v1633926858/watermark_mvfiyj.png&api-key=YqXLmTK9RGaMBX56EDyYTMwKbjbEiOCSmmGbdiYdeAcxofh6&user-id=leksyde&opacity=30
        } catch (Exception e) {
            validation.setWatermarked("false");
            validation.setImage(getBase64EncodedImage(validationdata.getAttachments().get(0).getDownloadSmallUrl()));

            System.out.println("watermark full");
            e.printStackTrace();
        }

        validationRepo.save(validation);

        System.out.println("validation saved");

    }


    @CrossOrigin(maxAge = 3600)
    @PostMapping(value = "/hookforvalidationdataa")
    public void showIndex(Model model, @RequestBody List<Validator> validationdata) throws IOException {

        for(Validator val: validationdata){
            Validation validation = new Validation();
            validation.setStart(val.getStart());
            validation.setEnd(val.getEnd());
            validation.setSubmissiontime(val.getSubmissionTime());
            validation.setLocation(val.getLocation());
            validation.setSubmittedby(val.getSubmittedBy());
            validation.setDeviceID(val.getDeviceid());
            validation.setQrcode(val.getBarcode());

            try {

                String watermarkkedimg="https://neutrinoapi.net/image-watermark?image-url="+ val.getAttachments().get(0).getDownloadSmallUrl()+"&watermark-url=https://res.cloudinary.com/nphcdaict/image/upload/c_scale,w_200/v1633926858/watermark_mvfiyj.png&api-key=YqXLmTK9RGaMBX56EDyYTMwKbjbEiOCSmmGbdiYdeAcxofh6&user-id=leksyde&opacity=30";

                validation.setImage(getBase64EncodedImage(watermarkkedimg));
                validation.setWatermarked("true");

                //https://neutrinoapi.net/image-watermark?image-url=https://kc.kobotoolbox.org/media/small?media_file=nphcdaemid%2Fattachments%2Fef760ad498194714b9c579702733e66a%2F0dca0051-adcf-4458-a5eb-e0317075f0fc%2F1637677592026.jpg&watermark-url=https://res.cloudinary.com/nphcdaict/image/upload/c_scale,w_200/v1633926858/watermark_mvfiyj.png&api-key=YqXLmTK9RGaMBX56EDyYTMwKbjbEiOCSmmGbdiYdeAcxofh6&user-id=leksyde&opacity=30
            } catch (Exception e) {
                validation.setWatermarked("false");
                validation.setImage(getBase64EncodedImage(val.getAttachments().get(0).getDownloadSmallUrl()));

                System.out.println("watermark full");
                e.printStackTrace();
            }

            validationRepo.save(validation);

            System.out.println("validation saved");
        }

    }

    public String getBase64EncodedImage(String imageURL) throws IOException {
        java.net.URL url = new java.net.URL(imageURL);
        InputStream is = url.openStream();
        byte[] bytes = org.apache.commons.io.IOUtils.toByteArray(is);
        return Base64.encodeBase64String(bytes);
    }


    public static byte[] convertImageByte(URL url) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = null;
        try {
            is = url.openStream ();
            byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
            int n;

            while ( (n = is.read(byteChunk)) > 0 ) {
                baos.write(byteChunk, 0, n);
            }
            return byteChunk;
        }
        catch (IOException e) {
            System.err.printf ("Failed while reading bytes from %s: %s", url.toExternalForm(), e.getMessage());
            e.printStackTrace ();
            // Perform any other exception handling that's appropriate.
        }
        finally {
            if (is != null) { is.close(); }
        }
        return null;
    }
}
