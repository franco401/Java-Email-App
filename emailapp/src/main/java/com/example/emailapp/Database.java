package com.example.emailapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found: " + e);
        }
    }

    //local SQLite database located in project root directory
    private static String url = "jdbc:sqlite:emailapp.db";

    public static Connection connect() {
        try {
            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println("Database connection error line 23: " + e);
            return null;
        }
    }
}
