package com.example.emailapp.Records;

/*
 * maps a post request containing "content",
 * "sender", and "receiver" as the keys into an
 * object for a post request in the controller
 */
public record EmailPostData(String content, String sender, String receiver) {}
