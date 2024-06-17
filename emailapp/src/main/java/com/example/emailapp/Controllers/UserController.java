package com.example.emailapp.Controllers;

import com.example.emailapp.Database;
import com.example.emailapp.Records.Email;
//import records for database tables
import com.example.emailapp.Records.User;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

//used to read url parameters such as /greeting?name="Adam"
import org.springframework.web.bind.annotation.RequestParam;

//used to create api endpoints for controller
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.sql.*;

@RestController
public class UserController {
    @GetMapping("/users")

    //@RequestParam(value = "email") String email

    public ArrayList<User> GetUsers() {
        Connection conn = Database.connect();
        String query = "select * from \"Users\"";
        ArrayList<User> users = new ArrayList<User>();

        //try-with-resources automatically closes the ps variable
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                /*
                 * create a user object for each row returned from 
                 * the database query and add it to the array list
                 */
                User user = new User(rs.getString("id"), rs.getString("email"), rs.getString("password"));
                users.add(user);
            }
        } catch (SQLException e) {
            System.out.println("Query error: " + e);
        }

        return users;
    }
}
