package com.example.emailapp.Models;

//maps a post request for replying to emails
public class ReplyForm {
    public String recipient;
    public String subject;
    public String content;
    public String sender;
    public String fileAttatchments;
    public String email_id_to_reply;
}