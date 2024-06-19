package com.example.emailapp.Models;

//database model representing a user table row in the database
public record User(String id, String email, String password) {}