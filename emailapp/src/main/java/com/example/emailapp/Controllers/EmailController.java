package com.example.emailapp.Controllers;

import com.example.emailapp.Database;

//import records for database tables
import com.example.emailapp.Records.Email;
import com.example.emailapp.Records.EmailPostData;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

//used to read url parameters such as /greeting?name="Adam"
import org.springframework.web.bind.annotation.RequestParam;

//used to create api endpoints for controller
import org.springframework.web.bind.annotation.RestController;

//used to execute db queries
import java.sql.*;

import java.util.ArrayList;
import java.util.Base64;

@RestController
public class EmailController {

    /*
     * get all emails a specific user received
     * using the url pattern /emails?receiver=[username]
     */
    @GetMapping("/emails")
    public ArrayList<Email> EmailsReceived(@RequestParam(value = "receiver") String receiver) {
        Connection conn = Database.connect();
        String query = "select * from \"Emails\" where receiver = ?";
        ArrayList<Email> emails = new ArrayList<Email>();

        //try-with-resources automatically closes the ps variable
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, receiver);
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

    @PostMapping("/sendemail")
    public void SendEmail(EmailPostData epd) {
        Connection conn = Database.connect();
        String query = "insert into \"Emails\" values (?, ?, ?, ?, ?, ?)";
        
        //create Base64 id using current time and other data
        long currentTime = System.currentTimeMillis();
        String data = currentTime + epd.sender();
        String id = Base64.getEncoder().encodeToString(data.getBytes());

        //try-with-resources automatically closes the ps variable
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, id);
            ps.setString(2, epd.content());
            ps.setString(3, epd.sender());
            ps.setString(4, epd.receiver());
            ps.setLong(5, currentTime);
            ps.setBoolean(6, false);

            //insert email to database
            ps.executeQuery();
        } catch (SQLException e) {
            System.out.println("Query error: " + e);
        }
    }
}
