package com.example.emailapp.Controllers;

import com.example.emailapp.Database;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//used to create api endpoints for controller
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//used to allow different request origins
import org.springframework.web.bind.annotation.CrossOrigin;

//used to execute db queries
import java.sql.*;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

//custom class with functions for encrypting and decrypting data
import com.example.emailapp.Security;

import com.example.emailapp.Models.Email;
import com.example.emailapp.Models.User;

import com.example.emailapp.Models.GetEmailsForm;
import com.example.emailapp.Models.GetRepliesForm;
import com.example.emailapp.Models.EmailForm;
import com.example.emailapp.Models.ReplyForm;
import com.example.emailapp.Models.FilterEmailForm;
import com.example.emailapp.Models.StarEmailForm;

import com.example.emailapp.EmailappApplication;

@CrossOrigin(origins = "http://127.0.0.1:5173/")

@RestController
public class EmailController {
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
    
    //request limit for all users, small for testing purposes 
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
        long waitTime = System.currentTimeMillis() + (1000 * 10 * 1);

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

    @PostMapping("/emailsreceived")
    public ResponseEntity<Object> emailsReceived(@RequestBody GetEmailsForm getEmailsForm) {        
        //get status code from rateLimit check and handle it below
        int statusCode = rateLimit(getEmailsForm.recipient);

        switch (statusCode) {
            case 403:
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);    
            case 429:
                return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        }

        Connection conn = Database.connect();

        if (conn != null) {
            //get up to the 25 most recent emails sorted by newest to oldest
            String query = "select id, sender, recipient, subject, content, sent, starred, file_attatchments, email_id_to_reply from emails where recipient = ? and email_id_to_reply is null order by sent desc limit 25 offset ?";
            ArrayList<Email> emails = new ArrayList<Email>();

            //try-with-resources automatically closes the ps variable
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, getEmailsForm.recipient);
                ps.setInt(2, getEmailsForm.offset);
                
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    //create an Email object for each row returned from the database query and add it to the array list
                    Email email = new Email(rs.getString("id"), rs.getString("sender"), rs.getString("recipient"), rs.getString("subject"), Security.decrypt(rs.getString("content")), rs.getLong("sent"), rs.getBoolean("starred"), rs.getString("file_attatchments"), rs.getString("email_id_to_reply"));
                    emails.add(email);
                }
                //close connection and result set once finished with db query
                conn.close();
                rs.close();
            } catch (SQLException e) {
                System.out.println("Query error at line " + EmailappApplication.getLineNumber() + " :" + e);
            }
            //return emails from query
            return new ResponseEntity<>(emails, HttpStatus.OK);
        }
        //return a 500 status code if the server can't connect to the database
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/repliesreceived")
    public ResponseEntity<Object> repliesReceived(@RequestBody GetRepliesForm getRepliesForm) {
        //get status code from rateLimit check and handle it below
        int statusCode = rateLimit(getRepliesForm.recipient);

        switch (statusCode) {
            case 403:
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);    
            case 429:
                return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        }
        
        Connection conn = Database.connect();

        if (conn != null) {
            String query = "select id, sender, recipient, subject, content, sent, starred, file_attatchments, email_id_to_reply from emails where email_id_to_reply = ?";
            ArrayList<Email> emails = new ArrayList<Email>();

            //try-with-resources automatically closes the ps variable
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, getRepliesForm.email_id_to_reply);
                
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    //create an Email object for each row returned from the database query and add it to the array list
                    Email email = new Email(rs.getString("id"), rs.getString("sender"), rs.getString("recipient"), rs.getString("subject"), Security.decrypt(rs.getString("content")), rs.getLong("sent"), rs.getBoolean("starred"), rs.getString("file_attatchments"), rs.getString("email_id_to_reply"));
                    emails.add(email);
                }
                //close connection and result set once finished with db query
                conn.close();
                rs.close();
            } catch (SQLException e) {
                System.out.println("Query error at line " + EmailappApplication.getLineNumber() + " :" + e);
            }
            //return emails from query
            return new ResponseEntity<>(emails, HttpStatus.OK);
        }
        //return a 500 status code if the server can't connect to the database
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    //remove this soon, will be used by sendEmail to see if a user exists
    public User getUser(String recipient) {
        Connection conn = Database.connect();
        
        if (conn != null) {
            String query = "select email from users where email = ?";

            //email of the recipient if we can find it in database
            String email = "";

            //used for returning a User object or null further below
            boolean recipientFound = false;

            //try-with-resources automatically closes the ps variable
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                //set query parameters and then execute
                ps.setString(1, recipient);
                ResultSet rs = ps.executeQuery();

                //read returned data from query in result set
                while (rs.next()) {                
                    email = rs.getString("email");
                    recipientFound = true;
                }
                //close connection and result set once finished with db query
                conn.close();
                rs.close();
            } catch (SQLException e) {
                //return null if a user doesn't exist
                System.out.println("Query error at line " + EmailappApplication.getLineNumber() + " :" + e);
                return null;
            }

            if (recipientFound) {
                //return a User object with an existing email if found
                return new User("", email, "");
            }
            return null;
        }
        //return null if conn is null
        return null;
    }
    
    @PostMapping("/sendemail")
    public ResponseEntity<Object> sendEmail(@RequestBody EmailForm emailForm) {
        //get status code from rateLimit check and handle it below
        int statusCode = rateLimit(emailForm.sender);

        switch (statusCode) {
            case 403:
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);    
            case 429:
                return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        }
        
        Connection conn = Database.connect();
        
        if (conn != null) {
            String query = "insert into emails values (?, ?, ?, ?, ?, ?, ?, ?, null)";

            String id = "";

            //used further below to determine if an empty email object is returned or not
            int recipientsFound = 0;

            /* 
            * index of the recipients array from the emailForm object
            * that is used further below for returning an email object
            * including one of the recipients that were found
            */
            int existingUserIndex = 0;

            /*
            * loop through each recipient if more than one were entered
            * and try to insert email for each recipient that exists 
            * in the database
            */

            //try-with-resources automatically closes the ps variable
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                for (int i = 0; i < emailForm.recipients.length; i++) {
                    if (getUser(emailForm.recipients[i]) != null) {
                        //create Base64 id using current time and the list of the recipients for the email being sent
                        id = Base64.getEncoder().encodeToString((emailForm.recipients[i] + System.currentTimeMillis()).getBytes());

                        //set query parameters
                        ps.setString(1, id);
                        ps.setString(2, emailForm.sender);
                        ps.setString(3, emailForm.recipients[i]);
                        ps.setString(4, emailForm.subject);
                        ps.setString(5, Security.encrypt(emailForm.content));
                        ps.setLong(6, System.currentTimeMillis());
                        ps.setBoolean(7, false);
                        ps.setString(8, emailForm.fileAttatchments);
                        
                        //add this current insert query to a batch for bulk insert later below
                        ps.addBatch();

                        //append query string for each recipient found
                        query += ",(?, ?, ?, ?, ?, ?, ?, ?, null)";

                        recipientsFound++;
                        existingUserIndex = i;
                    }
                }
                //insert all email(s) to database at once
                ps.executeBatch();

                //close connection and result set once finished with db query
                conn.close();
            } catch (SQLException e) {
                //return a 500 status code if the email couldn't be inserted
                System.out.println("Query error at line " + EmailappApplication.getLineNumber() + " :" + e);
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            if (recipientsFound > 0) {
                //return an Email object if inserted successfully with one of the existing recipients
                return new ResponseEntity<>(new Email("", "", emailForm.sender, emailForm.recipients[existingUserIndex], "", System.currentTimeMillis(), false, "", ""), HttpStatus.OK);
            }
            //return a 204 status code if none of the recipients doesn't exist
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        //return a 500 status code if the server can't connect to the database
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/sendreply")
    public ResponseEntity<Object> sendReply(@RequestBody ReplyForm replyForm) {
        //get status code from rateLimit check and handle it below
        int statusCode = rateLimit(replyForm.sender);

        switch (statusCode) {
            case 403:
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);    
            case 429:
                return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        }
        
        Connection conn = Database.connect();
        
        if (conn != null) {
            String query = "insert into emails values (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            String id = "";

            //try-with-resources automatically closes the ps variable
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                if (getUser(replyForm.recipient) != null) {
                    //create Base64 id using current time and the list of the recipients for the email being sent
                    id = Base64.getEncoder().encodeToString((replyForm.recipient + System.currentTimeMillis()).getBytes());

                    //set query parameters
                    ps.setString(1, id);
                    ps.setString(2, replyForm.sender);
                    ps.setString(3, replyForm.recipient);
                    ps.setString(4, replyForm.subject);
                    ps.setString(5, Security.encrypt(replyForm.content));
                    ps.setLong(6, System.currentTimeMillis());
                    ps.setBoolean(7, false);
                    ps.setString(8, replyForm.fileAttatchments);
                    ps.setString(9, replyForm.email_id_to_reply);
                    
                    //add this current insert query to a batch for bulk insert later below
                    ps.addBatch();

                    //append query string for each recipient found
                    query += ",(?, ?, ?, ?, ?, ?, ?, ?, ?)";
                }

                //insert all email(s) to database at once
                ps.executeBatch();

                //close connection and result set once finished with db query
                conn.close();
            } catch (SQLException e) {
                //return an empty Email object if the email couldn't be inserted
                System.out.println("Query error at line " + EmailappApplication.getLineNumber() + " :" + e);
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            //return a 204 status code if none of the recipients doesn't exist
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        //return a 500 status code if the server couldn't connect to the server
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/staremail")
    public ResponseEntity<Object> starEmail(@RequestBody StarEmailForm starEmailForm) {
        //get status code from rateLimit check and handle it below
        int statusCode = rateLimit(starEmailForm.user);

        switch (statusCode) {
            case 403:
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);    
            case 429:
                return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        }
        
        Connection conn = Database.connect();
        
        if (conn != null) {
            String query = "update emails set starred = ? where id = ?";

            //try-with-resources automatically closes the ps variable
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                //set query parameters and then execute
                ps.setBoolean(1, starEmailForm.starred);
                ps.setString(2, starEmailForm.emailID);
                
                //execute query
                ps.executeUpdate();

                //close connection and result set once finished with db query
                conn.close();
            } catch (SQLException e) {
                //return a 500 status code if the email couldn't be inserted
                System.out.println("Query error at line " + EmailappApplication.getLineNumber() + " :" + e);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            //return email object's starred state
            return new ResponseEntity<>(new Email("", "", "", "", "", 0, starEmailForm.starred, "", ""), HttpStatus.OK);
        }
        //return a 500 status code if the server couldn't connect to the server
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/filteremails")
    public ResponseEntity<Object> filterEmails(@RequestBody FilterEmailForm filterEmailForm) {
        //get status code from rateLimit check and handle it below
        int statusCode = rateLimit(filterEmailForm.recipient);

        switch (statusCode) {
            case 403:
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);    
            case 429:
                return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        }
        
        Connection conn = Database.connect();
        
        if (conn != null) {
            String query = "";
            
            //switch statement to determine which query to perform depending on what the user wants to filter their emails by
            switch (filterEmailForm.sortBy) {
                case "starred":
                    query = "select id, sender, recipient, subject, content, sent, starred, file_attatchments from emails where recipient = ? and starred = true order by sent desc";
                    break;
                case "allSent":
                    query = "select id, sender, recipient, subject, content, sent, starred, file_attatchments from emails where sender = ? order by sent desc";
                    break;
            }
            
            ArrayList<Email> emails = new ArrayList<Email>();

            //try-with-resources automatically closes the ps variable
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                //set query parameters and then execute
                ps.setString(1, filterEmailForm.recipient);
                
                //execute query
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    //create an email object for each row returned from the database query and add it to the array list
                    Email email = new Email(rs.getString("id"), rs.getString("sender"), rs.getString("recipient"), rs.getString("subject"), Security.decrypt(rs.getString("content")), rs.getLong("sent"), rs.getBoolean("starred"), rs.getString("file_attatchments"), "");
                    emails.add(email);
                }
                //close connection and result set once finished with db query
                conn.close();
                rs.close();
            } catch (SQLException e) {
                //return null if the query couldn't execute
                System.out.println("Query error at line " + EmailappApplication.getLineNumber() + " :" + e);
                return null;
            }
            return new ResponseEntity<>(emails, HttpStatus.OK);
        }
        //return a 500 status code if the server couldn't connect to the server
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
