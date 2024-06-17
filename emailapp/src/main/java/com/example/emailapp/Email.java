package com.example.emailapp;

public record Email(String id, String content, String sender, String receiver, long sent, boolean starred) {}