package com.nphcda.demo.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.nphcda.demo.entity.Validation;
import com.nphcda.demo.kobo.Validator;
import com.nphcda.demo.repo.ValidationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

@Component
public class DeamonController {



    @Value("${kobo-url}")
    private String koboUrl;

    @Value("${kobo-username}")
    private String kobousername;

    @Value("${kobo-password}")
    private String kobopassword;

    @Autowired
    ValidationRepo validationRepo;

    WebClient webClient;



    private Validator extractData() {
        System.out.println("trying to get image");

        String image="";
        try {

            webClient= WebClient.builder().exchangeStrategies(ExchangeStrategies.builder()
                    .codecs(configurer -> configurer
                            .defaultCodecs()
                            .maxInMemorySize(6000000 * 1024 * 1024))
                    .build())
                    .defaultHeaders(header -> header.setBasicAuth(kobousername, kobopassword))
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .baseUrl(koboUrl)
                    .build();


            // String url = "https://jmeter.e4eweb.space/dhis2/api/trackedEntityInstances.json?ou=s5DPBsdoE8b&program=gWuxRU2yJ1x&ouMode=CAPTURE&filter=izttywqePh2:EQ:NG-RJ89430232GV&fields=*";

            String url2 = koboUrl;


            // create auth credentials





            String search = "{'query':{\"Use_the_camera_to_scan_a_barcode\":\"firstName\"}}";

            String url = koboUrl+"/api/v1/data/799464?query={search}";


            // Validator validator = restTemplate.getForObject(url2, Validator.class);




            String response =  webClient.get().uri(url2)
                    .retrieve().bodyToMono(String.class).block();


            // get JSON response

            System.out.println(response);



            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);

            // Start by checking if this is a list -> the order is important here:
            if (rootNode instanceof ArrayNode) {
                // Read the json as a list:
                System.out.println("here");

                List<Validator> list = mapper.readValue(response, new TypeReference<List<Validator>>(){});



                System.out.println("size"+list.size());
                for (int i=0; i<list.size(); i++){

                    System.out.println("copying"+list.get(i).getBarcode());
                    Validation validation= new Validation();

                        System.out.println(list.get(i).getBarcode());



                            validation.setQrcode(list.get(i).getBarcode());
                            validation.setDeviceID(list.get(i).getDeviceid());
                            validation.setStart(list.get(i).getStart());
                            validation.setEnd(list.get(i).getEnd());
                            validation.setLocation(list.get(i).getLocation());
                            validation.setSubmittedby(list.get(i).getSubmittedBy());
                            validation.setSubmissiontime(list.get(i).getSubmissionTime());

                            URL imageUrl = new URL(list.get(i).getAttachments().get(i).getDownloadSmallUrl());





                        validationRepo.save(validation);





                }

            } else if (rootNode instanceof JsonNode) {
                // Read the json as a single object:
                System.out.println("hereeee");
                Validator object = mapper.readValue(rootNode.toString(), Validator.class);






            } else {

            }


        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;

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

    public WebClient getWebClientBuilder(){
        return   WebClient.builder().exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(16 * 1024 * 1024))
                .build())
                .build();
    }

    private Validator getImage(String QRcode) {
        System.out.println("trying to get image");

        String image="";
        try {
            webClient = WebClient.builder().exchangeStrategies(ExchangeStrategies.builder()
                    .codecs(configurer -> configurer
                            .defaultCodecs()
                            .maxInMemorySize(600000 * 1024 * 1024))
                    .build())
                    .defaultHeaders(header -> header.setBasicAuth(kobousername, kobopassword))
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .baseUrl(koboUrl)
                    .build();
            // String url = "https://jmeter.e4eweb.space/dhis2/api/trackedEntityInstances.json?ou=s5DPBsdoE8b&program=gWuxRU2yJ1x&ouMode=CAPTURE&filter=izttywqePh2:EQ:NG-RJ89430232GV&fields=*";

            String url2 = koboUrl;


            // create auth credentials





            String search = "{'query':{\"Use_the_camera_to_scan_a_barcode\":\"firstName\"}}";

            String url = koboUrl+"/api/v1/data/799464?query={search}";

            // Validator validator = restTemplate.getForObject(url2, Validator.class);




            String response =  webClient.get().uri(url2)
                    .retrieve().bodyToMono(String.class).block();


            // get JSON response

            System.out.println(response);



            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);

            // Start by checking if this is a list -> the order is important here:
            if (rootNode instanceof ArrayNode) {
                // Read the json as a list:
                System.out.println("here");

                List<Validator> list = mapper.readValue(response, new TypeReference<List<Validator>>(){});



                System.out.println(list);
                for (int i=0; i<list.size(); i++){

                    if(list.get(i)!=null){
                        System.out.println(list.get(i).getBarcode());
                        if(list.get(i).getGeolocation()!=null){


                            if(list.get(i).getBarcode().equals(QRcode)){
                                System.out.println("found it!");



                                return list.get(i);





                            }
                        }
                    }




                }

            } else if (rootNode instanceof JsonNode) {
                // Read the json as a single object:
                System.out.println("hereeee");
                Validator object = mapper.readValue(rootNode.toString(), Validator.class);






            } else {

            }


        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;

    }


}
