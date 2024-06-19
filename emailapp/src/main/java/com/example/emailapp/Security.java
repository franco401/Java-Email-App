package com.example.emailapp;

import java.util.Base64;

public class Security {
    //create Base64 id using current time in milliseconds and other data
    public static String createBase64ID(String data) {
        //append current time in milliseconds to string data
        data += System.currentTimeMillis();
        return Base64.getEncoder().encodeToString(data.getBytes());
    }
}
