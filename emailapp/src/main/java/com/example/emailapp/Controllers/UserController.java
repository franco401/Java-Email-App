package com.example.emailapp.Controllers;

import com.example.emailapp.Database;
import com.example.emailapp.Security;

import com.example.emailapp.Models.User;
import com.example.emailapp.Models.UserForm;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//used to read url parameters such as /greeting?name="Adam"
import org.springframework.web.bind.annotation.RequestParam;

//used to create api endpoints for controller
import org.springframework.web.bind.annotation.RestController;

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
        String query = "select id, email, password from \"Users\"";
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
            //close connection and result set once finished with db query
            conn.close();
            rs.close();
        } catch (SQLException e) {
            System.out.println("Query error line 54: " + e);
        }
        return users;
    }

    @PostMapping("/register")
    public User register(@RequestBody UserForm userForm) {
        //connect to database and read post request data mapped into userForm
        Connection conn = Database.connect();
        String query = "insert into \"Users\" values (?, ?, ?)";
        String id = Security.createBase64ID(userForm.email);

        //try-with-resources automatically closes the ps variable
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            //set query parameters and then execute
            ps.setString(1, id);
            ps.setString(2, userForm.email);
            ps.setString(3, DigestUtils.sha256Hex(userForm.password));
            ps.executeUpdate();
            
            //close connection once finished with db query
            conn.close();
        } catch (SQLException e) {
            //return an empty object if an error occurred
            System.out.println("Query error line 78: " + e);
            return new User("", "", "");
            
        }
        return new User("", userForm.email, "");
    }

    @PostMapping("/login")
    public User login(@RequestBody UserForm userForm) {
        //connect to database and read post request data mapped into userForm
        Connection conn = Database.connect();
        String query = "select email from \"Users\" where email = ? and password = ?";
        
        //store data returned from database query for returning User object below
        String email = "";

        //try-with-resources automatically closes the ps variable
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            //set query parameters and then execute
            ps.setString(1, userForm.email);
            ps.setString(2, DigestUtils.sha256Hex(userForm.password));
            ResultSet rs = ps.executeQuery();
            
            //read returned data from query in result set
            while (rs.next()) {
                email = rs.getString("email"); 
            }

            //close connection once finished with db query
            conn.close();
        } catch (SQLException e) {
            //return an empty object if an error occurred
            System.out.println("Query error line 111: " + e);
            return new User("", "", "");
        }
        /*
         * create a user object for the row in
         * the database that matches the entered
         * username and password
         */
        return new User("", email, "");
    }

    @GetMapping("/deleteaccount")
    public User deleteAccount(@RequestParam(value = "email") String email) {
        Connection conn = Database.connect();
        String query = "delete from \"Users\" where email = ?";

        //try-with-resources automatically closes the ps variable
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            //set query parameters and then execute
            ps.setString(1, email);
            ps.executeUpdate();
            
            //close connection once finished with db query
            conn.close();
        } catch (SQLException e) {
            //return an empty object if an error occurred
            System.out.println("Query error line 137: " + e);
            return new User("", "", "");
        }
        return new User("", email, "");
    }

    @PostMapping("updatepassword")
    public User updatePassword(@RequestBody UserForm userForm) {
        //connect to database and read post request data mapped into userForm
        Connection conn = Database.connect();
        String query = "update \"Users\" set password = ? where email = ?";

        //try-with-resources automatically closes the ps variable
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            //set query parameters and then execute
            ps.setString(1, DigestUtils.sha256Hex(userForm.password));
            ps.setString(2, userForm.email);
            ps.executeUpdate();
            
            //close connection once finished with db query
            conn.close();
        } catch (SQLException e) {
            //return an empty object if an error occurred
            System.out.println("Query error line 160: " + e);
            return new User("", "", ""); 
        }
        return new User("", userForm.email, "");
    }
}