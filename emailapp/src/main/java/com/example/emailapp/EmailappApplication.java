package com.example.emailapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.*;

@SpringBootApplication
public class EmailappApplication {
	public static void main(String[] args) {
		
		SpringApplication.run(EmailappApplication.class, args);
		
		String createUsersTable = "create table if not exists users ("
									+ "id text primary key not null,"
									+ " email text not null,"
									+ " password text not null);";
		
		String createEmailsTable = "create table if not exists emails ("
									+ "id text primary key not null,"
									+ " sender text not null,"
									+ " recipient text not null,"
									+ " subject text not null,"
									+ " content text not null,"
									+ " sent int8 not null,"
									+ " starred bool,"
									+ " file_attatchments text not null,"
									+ " email_id_to_reply text,"
									+ " foreign key (sender) references users(email) on delete cascade,"
									+ " foreign key (recipient) references users(email) on delete cascade);";

		/*
		* tries connecting to sqlite database and will create it
		* for the first time if it doesn't exist and then create
		* the Users table and Email table if they don't exist
		* and finally return a connection object
		*/
		try (Connection conn = Database.connect()) {
			if (conn != null) {
				Statement statement = conn.createStatement();
				statement.execute(createUsersTable);
				statement.execute(createEmailsTable);
			}
		} catch (SQLException e) {
			System.out.println("Connection error line 45: " + e);
		}
	}

}
