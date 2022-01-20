package com.nphcda.demo.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.nphcda.demo.entity.Validation;
import com.nphcda.demo.kobo.Validator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;



import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Checker {

    public static WebClient webClient;
    @Value("${kobo-url}")
    public static String koboUrl;

    @Value("${kobo-username}")
    public static String kobousername;

    @Value("${kobo-password}")
    public static String kobopassword;


    public static void main(String[] args){
        boolean res= extractData("https://nphcdaict.com.ng/verify?id=nphcda003913215");

    }

    // Method to encode a string value using `UTF-8` encoding scheme
    private static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    public static Boolean extractData(String barcod) {
        System.out.println("trying to get image");

        String image="";
        try {

            String name = "_id:1";

            webClient= WebClient.builder().exchangeStrategies(ExchangeStrategies.builder()
                    .codecs(configurer -> configurer
                            .defaultCodecs()
                            .maxInMemorySize(6000000 * 1024 * 1024))
                    .build())
                    .defaultHeaders(header -> header.set("Authorization", "7a18d5d19a30b1ac42cf82a3560806c8178476a4"))
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)

                    .build();


            // String url = "https://jmeter.e4eweb.space/dhis2/api/trackedEntityInstances.json?ou=s5DPBsdoE8b&program=gWuxRU2yJ1x&ouMode=CAPTURE&filter=izttywqePh2:EQ:NG-RJ89430232GV&fields=*";





            String encodedurl = "https://kf.kobotoolbox.org/assets/aUxpuQ9g2bKpiBuVJhpuVu/submissions/?format=json";
            // create auth credentials







            // Validator validator = restTemplate.getForObject(url2, Validator.class);




            String response =  webClient.get().uri(encodedurl)
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

                    System.out.println("checking");
                  if(list.get(i).getBarcode().contains(barcod)){
                      System.out.println("Foundddd!!");
                      System.out.println("This"+list.get(i).getBarcode());
                      return true;
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

        return false;

    }

}
