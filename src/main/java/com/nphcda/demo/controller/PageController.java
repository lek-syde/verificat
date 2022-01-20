package com.nphcda.demo.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.nphcda.demo.DTO.*;
import com.nphcda.demo.EventDTO.Event;
import com.nphcda.demo.EventDTO.Events;
import com.nphcda.demo.Service.EntityService;
import com.nphcda.demo.cache.ContactCache;
import com.nphcda.demo.cache.ValidationCache;
import com.nphcda.demo.entity.*;
import com.nphcda.demo.kobo.Validator;
import com.nphcda.demo.repo.*;
import org.joda.time.DateTimeComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.thymeleaf.util.DateUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.swing.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.nphcda.demo.DTO.TrackedEntityInstance.getWeeksBetween;

/**
 * @author Olalekan Folayan
 */
@Controller
public class PageController {


    @Autowired
    EntityService entityService;



    @Autowired
    HealthCenterRepo healthCenterRepo;

    @Value("${dhis-url}")
    private String dhisurl;

    @Value("${dhis-url-update}")
    private String dhisurlforupdate;

    @Value("${kobo-url}")
    private String koboUrl;

    @Value("${kobo-username}")
    private String kobousername;

    @Value("${kobo-password}")
    private String kobopassword;

    @Value("${dhis-username}")
    private String dhisusernam;

    @Value("${dhis-password}")
    private String dhispassword;

    @Autowired
    ContactCache contactCache;


    WebClient webClient;


    @Autowired
    RatingRepo ratingRepo;


    @Autowired
    RestTemplate restTemplate;


    @Autowired
    ValidationCache validationRepo;


    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String showIndex(Model model){
        model.addAttribute("verification", new VerificationEntity());
        return "index";
    }

    @RequestMapping(value = "/guide", method = RequestMethod.GET)
    public String showGuide(Model model){
        model.addAttribute("verification", new VerificationEntity());
        return "guide";
    }


    @RequestMapping(value = "/account", method = RequestMethod.GET)
    public String showAccount(Model model, HttpSession session) throws ParseException {

        TrackedEntityInstance firstresult = (TrackedEntityInstance) session.getAttribute("firstresult");


        String access = (String) session.getAttribute("accessgranted");
        model.addAttribute("verification", new VerificationEntity());
        List<Contact> contacts= new ArrayList<>();




        System.out.println("Access"+ access);
        List<Vaccination> myVaccinations = new ArrayList<>() ;
        if(firstresult!=null){
            String barcode = firstresult.getQRCode();
            String bar;


            if (!barcode.contains("http")) {
                bar="https://via.placeholder.com/200x200.png?text=QR+Not+Linked";
            } else {
                bar="https://api.qrserver.com/v1/create-qr-code/?size=200x200&data="+barcode;


            }


          myVaccinations= (List<Vaccination>) session.getAttribute("covid");

           String statecode= firstresult.getVaccinnatedFirstDoseCenter().substring(0,2);
            System.out.println("dosecenter"+ firstresult.getVaccinnatedFirstDoseCenter());
            System.out.println("dosecenter"+ firstresult.getVaccinatedSecondDoseCenter());


            contacts= contactCache.getContactPerson(firstresult.getEnrollments().get(0).getOrgUnitName().substring(0,2));


            System.out.println("validation date"+ contacts.get(0).getValidationDate());
            System.out.println("contacts-"+contacts.toString());

            model.addAttribute("covid", myVaccinations);
            model.addAttribute("contacts", contacts);
            model.addAttribute("ver", new EditDTO(firstresult.getTrackedEntityInstance(), firstresult.getIdtypee(), firstresult.getVaccinationid(), firstresult.getDocumentId(),firstresult.getDOB(), firstresult.getDHISPhoneNumber(), firstresult.getGender(), firstresult.getFamilyName(), firstresult.getOtherNames(), firstresult.getDHISEMAIL(),bar));

        }

        return "settings";
    }


    @RequestMapping(value = "/updatedetails", method = RequestMethod.POST)
    public String homePage(Model model, @ModelAttribute("ver") EditDTO trackedEntityInstance, HttpServletRequest request,HttpSession session) throws URISyntaxException, IOException, URISyntaxException {

        System.out.println("saving");

        TrackedEntityInstance recordDetails = (TrackedEntityInstance) session.getAttribute("firstresult");

        System.out.println("wow"+ recordDetails.getTrackedEntityInstance());
        trackedEntityInstance.setTrackedentitype(recordDetails.getTrackedEntityInstance());
        trackedEntityInstance.setOrgUnit(recordDetails.getOrgUnit());



            System.out.println("the date:"+ trackedEntityInstance.getDob());
            SimpleDateFormat formatter=new SimpleDateFormat("dd-MM-yyyy");

            try {
                Date date1=formatter.parse(trackedEntityInstance.getDob());
                final String NEW_FORMAT = "yyyy-MM-dd";
                formatter.applyPattern(NEW_FORMAT);


                trackedEntityInstance.setDob(formatter.format(date1));

                System.out.println("othername " +trackedEntityInstance.getOthername());


                System.out.println("vac-num"+ trackedEntityInstance.getVacnum());

                updateToDHIS(trackedEntityInstance);
            }catch (Exception e){
                trackedEntityInstance.setDob(trackedEntityInstance.getDob());
              updateToDHIS(trackedEntityInstance);
            }




        return "redirect:/verify?verificationID="+recordDetails.getVaccinnationID();
    }


    @Autowired
    private RestTemplateBuilder restTemplateBuilder;
    private void updateToDHIS(EditDTO user) throws URISyntaxException {
        System.out.println("sending");

        webClient = WebClient.builder()
                .defaultHeaders(header -> header.setBasicAuth(dhisusernam, dhispassword))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .baseUrl(dhisurlforupdate)
                .build();
        // String url = "https://jmeter.e4eweb.space/dhis2/api/trackedEntityInstances.json?ou=s5DPBsdoE8b&program=gWuxRU2yJ1x&ouMode=CAPTURE&filter=izttywqePh2:EQ:NG-RJ89430232GV&fields=*";




        String url2 = dhisurlforupdate+"/dhis2/api/trackedEntityInstances/"+user.getTrackedentitype();


        // create auth credentials




        TrackedEntityInstancem trackedEntityInstance= new TrackedEntityInstancem();
        trackedEntityInstance.setOrgUnit(user.getOrgUnit());
        trackedEntityInstance.setTrackedEntityType("ag6Yk7fwUEe");

        List<Attribute> attributes= new ArrayList<>();



        Attribute typeofid= new Attribute();
        typeofid.setAttribute("OvGXY097Hxt");
        typeofid.setValue(user.getIdtype());

        Attribute documentid= new Attribute();
        documentid.setAttribute("fXHMMrKgEWk");
        documentid.setValue(user.getIdnum());

        Attribute dob2= new Attribute();
        dob2.setAttribute("mAWcalQYYyk");
        dob2.setValue(user.getDob());


        Attribute surname =new Attribute();
        surname.setAttribute("aW66s2QSosT");
        surname.setValue(user.getFamilyname());
        System.out.println("family name"+ user.getFamilyname());


        Attribute othername =new Attribute();
        othername.setAttribute("TfdH5KvFmMy");
        othername.setValue(user.getOthername());
        System.out.println("other name"+ user.getOthername());


        Attribute gender =new Attribute();
        gender.setAttribute("CklPZdOd6H1");
        gender.setValue(user.getSex());
        System.out.println("sex"+ user.getSex());




        Attribute clientStatus= new Attribute();
        clientStatus.setAttribute("Kmh0uBf0GI5");
        clientStatus.setValue("lk");


        List<Enrollment> enrollments= new ArrayList<>();




        attributes.add(typeofid);
        attributes.add(documentid);
        attributes.add(dob2);
        attributes.add(clientStatus);
        attributes.add(gender);
        attributes.add(surname);
        attributes.add(othername);





        trackedEntityInstance.setAttributes(attributes);

        trackedEntityInstance.setEnrollments(enrollments);





        RestTemplate restTemplate = restTemplateBuilder.basicAuthentication(dhisusernam,dhispassword).build();
        URI uri = new URI(url2);


        System.out.println(url2);

      restTemplate.put(uri, trackedEntityInstance);





        // This method returns filter function which will log request data




    }



        @GetMapping(value = "/getAccess")
    public ResponseEntity<StatusMessage> getwards(@RequestParam(name = "phone", required = true) String phone, HttpSession session) {

        TrackedEntityInstance recordDetails = (TrackedEntityInstance) session.getAttribute("verifiedrecord");
        System.out.println("thisphone"+ phone);
        System.out.println(recordDetails.getDHISPhoneNumber());
            System.out.println(recordDetails.getDHISEMAIL());

        if(phone.equalsIgnoreCase(recordDetails.getDHISPhoneNumber()) || phone.equalsIgnoreCase(recordDetails.getDHISEMAIL())){

            session.setAttribute("accessgranted", "true");
            session.setAttribute("firstresult",recordDetails);



            return ResponseEntity.ok(new StatusMessage("success", "Edit Access Granted "));



        }

        System.out.println("wrong");
            session.setAttribute("accessgranted", "false");
       return ResponseEntity.ok(new StatusMessage("error", "Edit Access Denied, phone number does not match!"));

    }


    @RequestMapping(value = "/healthfacility", method = RequestMethod.GET)
    public String showHealthfacilityverification(Model model,  @RequestParam (required = true) String id){


        String barcode= "http://verification.vaccination.gov.ng/health-facility?id="+id;

        if(!healthCenterRepo.existsByOrganizationuit(id)){
            return "InvalidFaclity";

        }
        Healthcenter hc= healthCenterRepo.findByOrganizationuit(id);

        model.addAttribute("healthcenter", hc);



        model.addAttribute("barcode", "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + barcode);

        return "healthfacility";
    }


    @RequestMapping(value = "/rating", method = RequestMethod.GET)
    public String saveFacilityRating(Model model,  @RequestParam (required = true) String id, @RequestParam  (required = false) String show){



        if(!healthCenterRepo.existsByOrganizationuit(id)){
            return "InvalidFaclity";

        }
        Healthcenter hc= healthCenterRepo.findByOrganizationuit(id);

        model.addAttribute("rating", new Rating(hc.getState(), hc.getOrganizationuit(), hc.getHealthCenter()));
        model.addAttribute("healthcenter", hc);

       model.addAttribute("saved", show);

        System.out.println("ghg"+show);


        return "rating";
    }


    @RequestMapping(value = "/rate", method = RequestMethod.POST)
    public String homePage(Model model, @ModelAttribute("rating") RatingDTO rating, HttpServletRequest request) throws ParseException, IOException {

        System.out.println(rating.getComment());
        System.out.println("start"+rating.getStars());

        Rating myRating= new Rating(rating.getState(), rating.getFacilityid(), rating.getFacilityname());
        myRating.setComment(rating.getComment());
        myRating.setEmail(rating.getEmail());


        if(rating.getStars()==""){
            myRating.setStars(0);
        }else{
            myRating.setStars(Float.parseFloat(rating.getStars()));
        }

        ratingRepo.save(myRating);
        model.addAttribute("saved", true);
        return "redirect:/rating?id=" +rating.getFacilityid()+"&show=true";
    }





        @RequestMapping(value = "/faq", method = RequestMethod.GET)
    public String showFaq(Model model){
        model.addAttribute("verification", new VerificationEntity());
        return "faq";
    }

    @RequestMapping(value = "/verification", method = RequestMethod.POST)
    public ResponseEntity<StatusMessage> doverify(VerificationEntity verificationEntity, Model model){


                System.out.println("hey"+ verificationEntity.getVerificationID());
        return  ResponseEntity.ok(new StatusMessage("success", "Great"));

    }




    @RequestMapping(value = "/verify", method = RequestMethod.GET)
    public String homePage(Model model, @RequestParam (required = false) String id, @RequestParam(required = false) String verificationID, HttpServletRequest request, HttpSession session) throws ParseException, IOException {

        List<TrackedEntityInstance> tracker = null;
        List<Event> events;

        TrackedEntityInstance firstresult = null;

        List<Vaccination> myVaccinations = new ArrayList<>() ;
        List<Contact> contacts= new ArrayList<>();

        if (id!=null){

//THIS IS FOR SEARCH BY  QRCODE
            // Regex to check string is alphanumeric or not.
            String regex = "^(?=.*[a-zA-Z])(?=.*[0-9])[A-Za-z0-9]{8,}$";



            // Compile the ReGex
            Pattern p = Pattern.compile(regex);

            Matcher m = p.matcher(id);

            if(m.matches()){


                String hs=id.substring(6);

                long number = Long.parseLong(hs);

                if (number>0){
                    events = getAllEvents(id);
                    tracker = getAllEntities(events);
                }else{
                    //bad string
                    System.out.println("badd!! "+request.getRemoteAddr() +"-" +id);
                }




            }else{
                //bad string
                System.out.println("badd!! "+request.getRemoteAddr() +"-" +id);
                tracker= Collections.emptyList();
            }



            if (!tracker.isEmpty()) {
                firstresult= tracker.get(0);

                System.out.println("first "+firstresult.getFirstDosePhase());
                System.out.println("second "+ firstresult.getSecondDosePhase());

                model.addAttribute("verifiedrecord", firstresult);

                System.out.println(firstresult.getFirstDoseDate());

                String barcode = tracker.get(0).getQRCode();
                if (!barcode.contains("http")) {
                    model.addAttribute("barcode", "https://via.placeholder.com/200x200.png?text=QR+Not+Linked");
                } else {
                    model.addAttribute("barcode", "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + barcode);

                }


                System.out.println("vactype"+ firstresult.getVaccationtype());
                String batch = null;
                String batch2= null;

                System.out.println("hmm"+firstresult.getVaccinnatedFirstDose());






                if(firstresult.getVaccinnatedFirstDose().equalsIgnoreCase("true")&& firstresult.getFirstDosePhase()!=0){



                        batch=firstresult.getfirstdosebatchno();

                    myVaccinations.add(new Vaccination(ordinal(1), firstresult.getFirstDoseVaccineType(), firstresult.getFirstDose(), batch));
                }else if(firstresult.getVaccinatedSecondDose().equalsIgnoreCase("true") && firstresult.getFirstDosePhase()==0){
                    myVaccinations.add(new Vaccination(ordinal(1), "-", "-", "-"));
                }


                if(firstresult.getVaccinatedSecondDose().equalsIgnoreCase("true") && firstresult.getSecondDosePhase()!=0){

                       batch2=firstresult.getseconddosebatchno();



                    myVaccinations.add(new Vaccination(ordinal(2),firstresult.getSecondDoseVaccineType(), firstresult.getSecondDoseDate(), batch2));

                }else if(firstresult.getVaccinatedSecondDose().equalsIgnoreCase("true") && firstresult.getSecondDosePhase()==0){
                    myVaccinations.add(new Vaccination(ordinal(2), "-", "-", "-"));
                }


                if(firstresult.getVaccinatedBooster().equalsIgnoreCase("true")){




                    batch2=firstresult.getboosterBatch();

                    myVaccinations.add(new Vaccination("Booster",firstresult.getBoosterDoseVaccineType(), firstresult.getboosterdatee(), batch2));

                }else if(firstresult.getVaccinatedSecondDose().equalsIgnoreCase("true") && firstresult.getSecondDosePhase()==0){
                    myVaccinations.add(new Vaccination(ordinal(2), "-", "-", "-"));
                }









                session.setAttribute("covid", myVaccinations);




                model.addAttribute("covid", myVaccinations);





                tracker.remove(0);
                Page<TrackedEntityInstance> pagedEntities = entityService.findPaginated(PageRequest.of(1 - 1, 2), tracker);

                int totalPages = pagedEntities.getTotalPages();
                if (totalPages > 0) {
                    List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                            .boxed()
                            .collect(Collectors.toList());
                    model.addAttribute("pageNumbers", pageNumbers);
                }



                session.setAttribute("verifiedrecord", firstresult);


                model.addAttribute("ver", new EditDTO(firstresult.getTrackedEntityInstance(), firstresult.getIdtypee(), firstresult.getVaccinationid(), firstresult.getDocumentId(),firstresult.getDOB(), firstresult.getPhonenumber(), firstresult.getGender(), firstresult.getFamilyName(), firstresult.getOtherNames(), firstresult.getDHISEMAIL(), barcode));

                model.addAttribute("pagedEntities", pagedEntities);
            }
        }

        else{

            //THIS IS FOR SEARCH BY VACCINATIONID

            List<TrackedEntity> tracked = verification(verificationID);

             tracker= tracked.get(0).getTrackedEntityInstances();
            if (!tracker.isEmpty()) {

               firstresult= tracker.get(0);

                System.out.println("first "+firstresult.getFirstDosePhase());
                System.out.println("second "+ firstresult.getSecondDosePhase());


                System.out.println("first center "+firstresult.getVaccinnatedFirstDoseCenter());
                System.out.println("second center"+ firstresult.getVaccinatedSecondDoseCenter());

                String firststatecode=firstresult.getVaccinnatedFirstDoseCenter().substring(0,2);
                String secondstatecode =firstresult.getVaccinatedSecondDoseCenter().substring(0,2);

                System.out.println("bae"+ firststatecode);

                System.out.println("vactype"+ firstresult.getVaccationtype());
                String batch = null;
                String batch2= null;


                System.out.println("hmm"+firstresult.getVaccinnatedFirstDose());
                if(firstresult.getVaccinnatedFirstDose().equalsIgnoreCase("true")&& firstresult.getFirstDosePhase()!=0){


                    System.out.println(firststatecode);
                    System.out.println("vaccinetype"+ firstresult.getFirstDoseVaccineType());
                    System.out.println("vaccinetype2" + firstresult.getSecondDoseVaccineType());
                    System.out.println(firstresult.getFirstDosePhase());





               //    VaccineDistribution firstdist= vaccinedistrepo.findByStateCodeAndVaccinetypeAndPhase(firststatecode, firstresult.getVaccationtype(), firstresult.getFirstDosePhase());


                        batch=firstresult.getfirstdosebatchno();

                    myVaccinations.add(new Vaccination(ordinal(1), firstresult.getFirstDoseVaccineType(), firstresult.getFirstDose(), batch));
                }else if(firstresult.getVaccinatedSecondDose().equalsIgnoreCase("true") && firstresult.getFirstDosePhase()==0){
                    myVaccinations.add(new Vaccination(ordinal(1), "-", "-", "-"));
                }

                if(firstresult.getVaccinatedSecondDose().equalsIgnoreCase("true") && firstresult.getSecondDosePhase()!=0){
            //        VaccineDistribution seconddist= vaccinedistrepo.findByStateCodeAndVaccinetypeAndPhase(secondstatecode, firstresult.getVaccationtype(), firstresult.getSecondDosePhase());


                        batch2=firstresult.getseconddosebatchno();


                    myVaccinations.add(new Vaccination(ordinal(2),firstresult.getSecondDoseVaccineType(), firstresult.getSecondDoseDate(), batch2));

                }else if(firstresult.getVaccinatedSecondDose().equalsIgnoreCase("true") && firstresult.getSecondDosePhase()==0){
                    myVaccinations.add(new Vaccination(ordinal(2), "-", "-", "-"));
                }

                if(firstresult.getVaccinatedBooster().equalsIgnoreCase("true")){



                    batch2=firstresult.getboosterBatch();

                    myVaccinations.add(new Vaccination("Booster",firstresult.getBoosterDoseVaccineType(), firstresult.getboosterdatee(), batch2));

                }else if(firstresult.getVaccinatedSecondDose().equalsIgnoreCase("true") && firstresult.getSecondDosePhase()==0){
                    myVaccinations.add(new Vaccination(ordinal(2), "-", "-", "-"));
                }











                model.addAttribute("covid", myVaccinations);
                session.setAttribute("covid", myVaccinations);
                model.addAttribute("verifiedrecord", firstresult);
                System.out.println("hey"+firstresult.getTrackedEntityInstance());
                System.out.println("oky"+ firstresult.getFamilyName());

                System.out.println("idtype"+ firstresult.getIdType2());

               firstresult.setVaccinationid(firstresult.getVaccinnationID());

                session.setAttribute("verifiedrecord", firstresult);





                String barcode = firstresult.getQRCode();
                if (!barcode.contains("http")) {
                    model.addAttribute("barcode", "https://via.placeholder.com/200x200.png?text=QR+Not+Linked");
                } else {
                    model.addAttribute("barcode", "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + barcode);

                }

                model.addAttribute("ver", new EditDTO(firstresult.getTrackedEntityInstance(), firstresult.getIdType2(), firstresult.getVaccinationid(), firstresult.getDocumentId(),firstresult.getDOB(), firstresult.getPhonenumber(), firstresult.getGender(), firstresult.getFamilyName(), firstresult.getOtherNames(), firstresult.getDHISEMAIL(), barcode));

                tracker.remove(0);
                Page<TrackedEntityInstance> pagedEntities = entityService.findPaginated(PageRequest.of(1 - 1, 2), tracker);

                int totalPages = pagedEntities.getTotalPages();
                if (totalPages > 0) {
                    List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                            .boxed()
                            .collect(Collectors.toList());
                    model.addAttribute("pageNumbers", pageNumbers);
                }



                model.addAttribute("pagedEntities", pagedEntities);
            }
        }




        Validation validateInfo = null;
        Boolean validatorconfirmed = null;
        if(firstresult!=null){
            validateInfo= checkValidation(firstresult.getQRCode());

                System.out.println(firstresult.toString());

                 contacts = contactCache.getContactPerson(firstresult.getEnrollments().get(0).getOrgUnitName().substring(0, 2));

                 Date statefinish;
                    if(validateInfo!=null){
                        String datestring=validateInfo.getSubmissiontime();
                        String statedate=  contacts.get(0).getValidationDate();
                        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
                        DateFormat inputFormat2 = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);


                        if(datestring!=null){
                            Date submissiondate = inputFormat.parse(datestring);
                            statefinish= inputFormat2.parse(statedate);


                            if(submissiondate.after(statefinish)){
                                model.addAttribute("validationvalid", "true");
                            }else{
                                model.addAttribute("validationvalid", "false");
                                validateInfo=null;
                            }





                        }
                    }else{


                    }













        }


        


        String image="https://via.placeholder.com/200x200.png?text=Validation+Pending";
        String text="";
        if(validateInfo!=null){

            // String watermarkkedimg="https://textoverimage.moesif.com/image?image_url="+image+"&text="+text+"&x_align=center&y_align=bottom&text_size=16&text_color=000000ff"+".jpg";

          validateInfo.getImage();
         //   String watermarkkedimg="https://neutrinoapi.net/image-watermark?image-url="+ image+"&watermark-url=https://res.cloudinary.com/nphcdaict/image/upload/c_scale,w_200/v1633926858/watermark_mvfiyj.png&api-key=YqXLmTK9RGaMBX56EDyYTMwKbjbEiOCSmmGbdiYdeAcxofh6&user-id=leksyde&opacity=30";



           // System.out.println("image" +watermarkkedimg);


            if(firstresult.getVaccinatedSecondDose().equalsIgnoreCase("true")){

                model.addAttribute("reddot", true);
            }else if(firstresult.getVaccinnatedFirstDose().equalsIgnoreCase("true") && firstresult.getVaccationtype()=="Johnson" ){
                model.addAttribute("reddot", true);
            }
            else{
                model.addAttribute("reddot", false);
            }
            byte[] byteArray=null; //need to initialize it


            model.addAttribute("covid", myVaccinations);

            boolean found=false;

            if(validateInfo!=null){
                for (Vaccination vac: myVaccinations){
                    System.out.println("datte"+ vac.getVaccineDate());

                    DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);

                    DateFormat inputFormat2 = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

                    System.out.println("end"+validateInfo.getEnd());
                    Date vald= inputFormat2.parse(vac.getVaccineDate());
                    Date da= inputFormat.parse(validateInfo.getEnd());



                    DateTimeComparator dateTimeComparator = DateTimeComparator.getDateOnlyInstance();
                    int retVal = dateTimeComparator.compare(da, vald);

                    if(retVal == 0){
                        System.out.println("these are same date");
                        vac.setValidated(true);
                        found=true;
                    }




                }
            }

            if(found!=true){
                validateInfo=null;

            }else{
                model.addAttribute("verifiedimg", "data:image/jpeg;base64,"+validateInfo.getImage());
            }


            model.addAttribute("validator",validatorconfirmed );

        }



        model.addAttribute("displaydot",false);

if(firstresult!=null)
{

    if(firstresult.displaydot(contacts.get(0).getValidationDate())){
        model.addAttribute("displaydot",true);
    }

    if(firstresult.displaydotfirst(contacts.get(0).getValidationDate())){
        model.addAttribute("displaydot",true);
    }
    if(firstresult.displaydotbooster(contacts.get(0).getValidationDate())){
        model.addAttribute("displaydot",true);
    }


}







        model.addAttribute("validatationinfo", validateInfo);


        model.addAttribute("verification", new VerificationEntity());

        return "verification";
    }





    public boolean displaydote(String statedate, String vaccinedate) throws ParseException {

        DateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);





        Date vaccinedatee = inputFormat.parse(vaccinedate);

        Date statetrainingdate = parseDate(statedate);




            if(vaccinedatee.after(statetrainingdate)){
                System.out.println("Authentication Phase");
                return true;
            }

            else{
                System.out.println("Your Safe");
                return false;
            }

        }

    public static Date parseDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException e) {
            return null;
        }
    }
    private Validation checkValidation(String QRcode) {
        System.out.println("trying to get image");
        Validation validation;

        List<Validation> validationlist= validationRepo.getValidation(QRcode);




        if(!validationlist.isEmpty()){
            validationlist.sort(Comparator.comparing(Validation::getId).reversed());
        return validationlist.get(0);
        }



     return null;


    }


    String stripstring(String deviceid)
    {
        System.out.println("Device ID "+ deviceid);

        System.out.println("sub "+ deviceid.substring(8));
        return deviceid.substring(8);
    }









    public static String ordinal(int i) {
        String[] suffixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return i + "th";
            default:
                return i + suffixes[i % 10];

        }
    }

    private List<TrackedEntityInstance> getAllEntities(List<Event> events) {

        List<TrackedEntityInstance> listofpple = new ArrayList<>();


        for (int i = 0; i < events.size(); i++) {
            try {


                webClient = WebClient.builder()
                        .defaultHeaders(header -> header.setBasicAuth(dhisusernam, dhispassword))
                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .baseUrl(dhisurl)
                        .build();
                // String url = "https://jmeter.e4eweb.space/dhis2/api/trackedEntityInstances.json?ou=s5DPBsdoE8b&program=gWuxRU2yJ1x&ouMode=CAPTURE&filter=izttywqePh2:EQ:NG-RJ89430232GV&fields=*";


                String url2 = dhisurl+"/dhis2/api/trackedEntityInstances/" + events.get(i).getTrackedEntityInstance() + ".json?ou=s5DPBsdoE8b&program=gWuxRU2yJ1x&ouMode=CAPTURE&fields=*";


                // create auth credentials


                System.out.println(url2);






                String response =  webClient.get().uri(url2)
                        .retrieve().bodyToMono(String.class).block();


                // get JSON response



                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(response);


                // Start by checking if this is a list -> the order is important here:
                if (rootNode instanceof ArrayNode) {
                    // Read the json as a list:
                    TrackedEntity[] objects = mapper.readValue(rootNode.toString(), TrackedEntity[].class);

                } else if (rootNode instanceof JsonNode) {
                    // Read the json as a single object:
                    TrackedEntityInstance object = mapper.readValue(rootNode.toString(), TrackedEntityInstance.class);

                    listofpple.add(object);




                } else {

                }


            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        return listofpple;
    }


    public List<Event> getAllEvents(String id) {

        List<Event> events = new ArrayList<>();


        try {


            webClient = WebClient.builder()
                    .defaultHeaders(header -> header.setBasicAuth(dhisusernam, dhispassword))
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .baseUrl(dhisurl)
                    .build();

            String url2 = dhisurl+"/dhis2/api/events.json?program=gWuxRU2yJ1x&filter=LavUrktwH5D:Like:" + id  +"&page=1&pageSize=2";


            System.out.println(url2);

            String response =  webClient.get().uri(url2)
                    .retrieve().bodyToMono(String.class).block();


            System.out.println(response);


            // get JSON response




            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);

            // Start by checking if this is a list -> the order is important here:
            if (rootNode instanceof ArrayNode) {
                // Read the json as a list:
                Events[] objects = mapper.readValue(rootNode.toString(), Events[].class);


            } else if (rootNode instanceof JsonNode) {

                Events object = mapper.readValue(rootNode.toString(), Events.class);

                System.out.println(object.getEvents().isEmpty());
                 events=object.getEvents();








                return events;
            }


        }
        catch (Exception e) {
            e.printStackTrace();

                throw new RuntimeException();

        }


        return events;
    }


    public List<TrackedEntity> verification(String verificationid){





        List<TrackedEntity> tracker = new ArrayList<>();
        try {
            // request url

            webClient = WebClient.builder()
                    .defaultHeaders(header -> header.setBasicAuth(dhisusernam, dhispassword))
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .baseUrl(dhisurl)
                    .build();

            String url = dhisurl+"/dhis2/api/trackedEntityInstances.json?ou=s5DPBsdoE8b&program=gWuxRU2yJ1x&ouMode=CAPTURE&fields=*&filter=izttywqePh2:EQ:" + verificationid;


            System.out.println(url);




            // make a request


            String response =  webClient.get().uri(url)
                    .retrieve().bodyToMono(String.class).block();

            // get JSON response


            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);


            // Start by checking if this is a list -> the order is important here:
            if (rootNode instanceof ArrayNode) {
                // Read the json as a list:
                TrackedEntityInstance[] objects = mapper.readValue(rootNode.toString(), TrackedEntityInstance[].class);


            } else if (rootNode instanceof JsonNode) {
                // Read the json as a single object:

                TrackedEntity object = mapper.readValue(rootNode.toString(), TrackedEntity.class);






                tracker.add(object);


                return tracker;


            }


        } catch (Exception e) {
            e.printStackTrace();


        }

        return tracker;

    }


}




