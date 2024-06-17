package com.example.emailapp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

//used to read url parameters such as /greeting?name="Adam"
import org.springframework.web.bind.annotation.RequestParam;

//used to create api endpoints for controller
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
public class EmailController {

    /*
     * get all emails a specific user received
     * using the url pattern /emails?receiver=[username]
     */
    @GetMapping("/emails")
    public ArrayList<Email> EmailsReceived(@RequestParam(value = "receiver") String receiver) {
        
        ArrayList<Email> emails = new ArrayList<Email>();
        emails.add(new Email("15N0QINR23", "hi", "user2@mail.com", receiver, 1518648939830L, false));
        emails.add(new Email("2rJ40912J4AQ", "hello?", "user2@mail.com", receiver, 1528648939830L, false));
        
        return emails;
    }
}
