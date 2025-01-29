package com.example.emailapp.Models;

//database model representing an email table row in the database
public record Email(String id, String sender, String recipient, String subject, String content, long sent, boolean starred, String fileAttatchments, String email_id_to_reply) {}