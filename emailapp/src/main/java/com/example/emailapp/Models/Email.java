package com.example.emailapp.Models;

//database model representing an email table row in the database
public record Email(String id, String content, String sender, String receiver, long sent, boolean starred) {}