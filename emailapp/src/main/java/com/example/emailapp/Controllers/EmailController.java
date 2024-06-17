package com.example.emailapp.Controllers;

import com.example.emailapp.Database;
//import records for database tables
import com.example.emailapp.Records.*;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

//used to read url parameters such as /greeting?name="Adam"
import org.springframework.web.bind.annotation.RequestParam;

//used to create api endpoints for controller
import org.springframework.web.bind.annotation.RestController;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.sql.*;

@RestController
public class EmailController {

    /*
     * get all emails a specific user received
     * using the url pattern /emails?receiver=[username]
     */
    @GetMapping("/emails")
    public ArrayList<Email> EmailsReceived(@RequestParam(value = "receiver") String receiver) {
        Connection conn = Database.connect();
        String query = "select * from \"Emails\"";

        ArrayList<Email> emails = new ArrayList<Email>();
        //try-with-resources automatically closes the ps variable
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                /*
                 * create an email object for each row returned from 
                 * the database query and add it to the array list
                 */
                Email email = new Email(rs.getString("id"), rs.getString("content"), rs.getString("sender"), rs.getString("receiver"), rs.getLong("sent"), rs.getBoolean("starred"));
                emails.add(email);
            }
        } catch (SQLException e) {
            System.out.println("Query error: " + e);
        }

        return emails;
    }
}
