package com.example.emailapp.Controllers;

import com.example.emailapp.Database;
import com.example.emailapp.Security;

//import records for database tables
import com.example.emailapp.Records.User;
import com.example.emailapp.Records.UserForm;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//used to read url parameters such as /greeting?name="Adam"
import org.springframework.web.bind.annotation.RequestParam;

//used to create api endpoints for controller
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
//used to allow different request origins
import org.springframework.web.bind.annotation.CrossOrigin;

//used for password hashing
import org.apache.commons.codec.digest.DigestUtils;

//used to execute db queries
import java.sql.*;
import java.util.ArrayList;

@CrossOrigin(origins = "http://127.0.0.1:5173/")
@RestController
public class UserController {
    @GetMapping("/users")
    public ArrayList<User> getUsers() {
        Connection conn = Database.connect();
        String query = "select * from \"Users\"";
        ArrayList<User> users = new ArrayList<User>();

        //try-with-resources automatically closes the ps variable
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();

            //read returned data from query in result set
            while (rs.next()) {
                /*
                 * create a user object for each row returned from 
                 * the database query and add it to the array list
                 */
                User user = new User(rs.getString("id"), rs.getString("email"), rs.getString("password"));
                users.add(user);
            }
            //close connection once finished with db query
            conn.close();
        } catch (SQLException e) {
            System.out.println("Query error: " + e);
        }
        return users;
    }

    @PostMapping("/register")
    public User register(@RequestBody UserForm userForm) {
        //connect to database and read post request data mapped into userForm
        Connection conn = Database.connect();
        String query = "insert into \"Users\" values (?, ?, ?)";
        String id = Security.createBase64ID(userForm.email);
        User user = new User(id, userForm.email, DigestUtils.sha256Hex(userForm.password));

        //try-with-resources automatically closes the ps variable
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            //set query parameters and then execute
            ps.setString(1, id);
            ps.setString(2, user.email());
            ps.setString(3, user.password());
            ps.executeQuery();
            
            //close connection once finished with db query
            conn.close();
        } catch (SQLException e) {
            System.out.println("Query error: " + e);
        }
        return user;
    }

    @PostMapping("/login")
    public User login(@RequestBody UserForm userForm) {
        //connect to database and read post request data mapped into userForm
        Connection conn = Database.connect();
        String query = "select id, email from \"Users\" where email = ? and password = ?";
        
        //store data returned from database query for returning User object below
        String id = "";
        String email = "";

        //try-with-resources automatically closes the ps variable
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            //set query parameters and then execute
            ps.setString(1, userForm.email);
            ps.setString(2, DigestUtils.sha256Hex(userForm.password));
            ResultSet rs = ps.executeQuery();
            
            //read returned data from query in result set
            while (rs.next()) {
                id = rs.getString("id"); 
                email = rs.getString("email"); 
            }

            //close connection once finished with db query
            conn.close();
        } catch (SQLException e) {
            System.out.println("Query error: " + e);
        }
        /*
         * create a user object for the row in
         * the database that matches the entered
         * username and password
         */
        return new User(id, email, "");
    }
}