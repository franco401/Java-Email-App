package com.example.emailapp.Controllers;

import com.example.emailapp.Database;
import com.example.emailapp.EmailappApplication;
import com.example.emailapp.Models.User;
import com.example.emailapp.Models.UserForm;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//used to read url parameters such as /greeting?name="Adam"
import org.springframework.web.bind.annotation.RequestParam;

//used to create api endpoints for controller
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

//used to allow different request origins
import org.springframework.web.bind.annotation.CrossOrigin;
//used for password hashing
import org.apache.commons.codec.digest.DigestUtils;

//used to execute db queries
import java.sql.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

@CrossOrigin(origins = "http://127.0.0.1:5173/")
@RestController
public class UserController {
    //stores amount of requests for each user
    HashMap<String, AtomicInteger> requestsPerUser = new HashMap<String, AtomicInteger>();
    
    //stores the timestamp of when each user is allowed to make a request after being blocked
    HashMap<String, Long> blockedUsers = new HashMap<String, Long>();
    
    /*
     * the amount of requests for a user, as an AtomicInteger object,
     * great for atomically incrementing to avoid incorrect values
     * as each request will increment this value in its own thread
     */
    AtomicInteger requests = new AtomicInteger(0);
    
    //request limit for all users 
    int requestLimit = 5;

    //amount of minutes users have to wait until they get unblocked
    int minutes = 1;

    //checks if a user has reached the request limit
    public boolean requestLimitReached(String user) {
        //add new user if not already added
        requestsPerUser.putIfAbsent(user, new AtomicInteger(0));
        
        //increment amount of requests for a user
        requests = requestsPerUser.get(user);
        requests.incrementAndGet();

        return requests.get() > requestLimit;
    }

    //remove user from blockedUsers hashmap and set user's request count to 1
    public void unblockUser(String user) {
        //remove user from blockedUser hashmap
        blockedUsers.remove(user);

        //set user's request count to 1
        requestsPerUser.remove(user);
        requestsPerUser.putIfAbsent(user, new AtomicInteger(1));
    }

    //call this only AFTER a user reaches the request limit
    public void blockUser(String user) {
        //make a future timestamp 1 minute from now that a blocked user has to wait until to make further requests
        long waitTime = System.currentTimeMillis() + (1000 * 60 * 1);

        //add new user if not already added
        blockedUsers.putIfAbsent(user, waitTime);
    }

    //handles rate limiting logic to prevent users from making too many requests
    public int rateLimit(String user) {
        //the blocked user's timestamp of when they can do a request again, can be null if it's not in blockedUsers 
        Object userTimeStamp = blockedUsers.get(user);

        //basic rate limiting logic below, checks if user has reached the request limit and other cases below
        if (requestLimitReached(user)) {
            //check if the user is in the blockedUser hashmap, if not block them
            if (userTimeStamp != null) {
                //check the current time of this request, if false then the user is still blocked
                if (System.currentTimeMillis() > (long)userTimeStamp) {
                    //unblock this user, then proceed with connecting to the database and do the request further below
                    unblockUser(user);
                } else {
                    //user is still blocked
                    return HttpStatus.FORBIDDEN.value();
                }
            } else {
                blockUser(user);
                return HttpStatus.TOO_MANY_REQUESTS.value();
            }
        }
        return 200;
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody UserForm userForm) {
        //get status code from rateLimit check and handle it below
        int statusCode = rateLimit(userForm.email);

        switch (statusCode) {
            case 403:
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);    
            case 429:
                return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        }
        
        //connect to database and read post request data mapped into userForm
        Connection conn = Database.connect();
    
        if (conn != null) {
            String query = "insert into users values (?, ?, ?)";

            //create Base64 id using current time in milliseconds and the user's email
            String id = Base64.getEncoder().encodeToString(userForm.email.getBytes());

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
                //return a 500 status code if an error occurred
                System.out.println("Query error at line " + EmailappApplication.getLineNumber() + " :" + e);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);                
            }
            //return user's email if registration was successful
            return new ResponseEntity<>(new User("", userForm.email, ""), HttpStatus.OK);
        }
        //return a 500 status code if the server can't connect to the database
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody UserForm userForm) {
        //get status code from rateLimit check and handle it below
        int statusCode = rateLimit(userForm.email);

        switch (statusCode) {
            case 403:
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);    
            case 429:
                return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        }
        
        //connect to database and read post request data mapped into userForm
        Connection conn = Database.connect();
        
        if (conn != null) {
            String query = "select email from users where email = ? and password = ?";
            
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
                //return a 500 status code if an error occurred
                System.out.println("Query error at line " + EmailappApplication.getLineNumber() + " :" + e);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);                
            }
            
            //return user's email if logging in was successful (non-empty sql result)
            if (email.length() > 0) {
                return new ResponseEntity<>(new User("", email, ""), HttpStatus.OK);
            } else {
                //return 204 if the sql result is an email
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
        }
        //return a 500 status code if the server can't connect to the database
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/deleteaccount")
    public ResponseEntity<Object> deleteAccount(@RequestParam(value = "email") String email) {
        Connection conn = Database.connect();
        
        if (conn != null) {
            String query = "delete from users where email = ?";

            //try-with-resources automatically closes the ps variable
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                //set query parameters and then execute
                ps.setString(1, email);
                ps.executeUpdate();
                
                //close connection once finished with db query
                conn.close();
            } catch (SQLException e) {
                //return a 500 status code if an error occurred
                System.out.println("Query error at line " + EmailappApplication.getLineNumber() + " :" + e);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);                
            }
            return new ResponseEntity<>(new User("", email, ""), HttpStatus.OK);
        }
        //return a 500 status code if the server can't connect to the database
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/updatepassword")
    public ResponseEntity<Object> updatePassword(@RequestBody UserForm userForm) {
        //connect to database and read post request data mapped into userForm
        Connection conn = Database.connect();
        
        if (conn != null) {
            String query = "update users set password = ? where email = ?";

            //try-with-resources automatically closes the ps variable
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                //set query parameters and then execute
                ps.setString(1, DigestUtils.sha256Hex(userForm.password));
                ps.setString(2, userForm.email);
                ps.executeUpdate();
                
                //close connection once finished with db query
                conn.close();
            } catch (SQLException e) {
                //return a 500 status code if an error occurred
                System.out.println("Query error at line " + EmailappApplication.getLineNumber() + " :" + e);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); 
            }
            //return the user's email if the password update was successful
            return new ResponseEntity<>(new User("", userForm.email, ""), HttpStatus.OK);
        }
        //return a 500 status code if the server can't connect to the database
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}