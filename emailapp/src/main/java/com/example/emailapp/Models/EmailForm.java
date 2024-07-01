package com.example.emailapp.Models;

//maps a post request for sending emails
public class EmailForm {
    public String[] recipients;
    public String subject;
    public String content;
    public String sender;
    public String fileAttatchments;
}