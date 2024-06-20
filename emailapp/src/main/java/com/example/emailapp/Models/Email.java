package com.example.emailapp.Models;

//database model representing an email table row in the database
public record Email(String id, String content, String sender, String recipient, long sent, boolean starred) {}