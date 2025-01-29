package com.example.emailapp.Controllers;

import com.example.emailapp.Database;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//used to create api endpoints for controller
import org.springframework.web.bind.annotation.RestController;

//used to allow different request origins
import org.springframework.web.bind.annotation.CrossOrigin;

//used to execute db queries
import java.sql.*;

import java.util.ArrayList;
import java.util.Base64;

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

@CrossOrigin(origins = "http://127.0.0.1:5173/")

@RestController
public class EmailController {
    @PostMapping("/emailsreceived")
    public ArrayList<Email> emailsReceived(@RequestBody GetEmailsForm getEmailsForm) {
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
                System.out.println("Query error line 60: " + e);
            }
            return emails;
        }
        //return an empty Email ArrayList if conn is null
        return new ArrayList<Email>();
    }

    //remove this soon
    @GetMapping("/allemails")
    public ArrayList<Email> allEmails() {
        Connection conn = Database.connect();

        ArrayList<Email> emails = new ArrayList<Email>();

        if (conn != null) {
            String query = "select * from emails";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    //create an Email object for each row returned from the database query and add it to the array list
                    Email email = new Email(rs.getString("id"), rs.getString("sender"), rs.getString("recipient"), rs.getString("subject"), Security.decrypt(rs.getString("content")), rs.getLong("sent"), rs.getBoolean("starred"), rs.getString("file_attatchments"), rs.getString("email_id_to_reply"));
                    emails.add(email);
                }
                //close connection and result set once finished with db query
                conn.close();
                rs.close();
            } catch (Exception e) {
                System.out.println(e);
            }
            return emails;
        }
        return new ArrayList<Email>();
    }

    @PostMapping("/repliesreceived")
    public ArrayList<Email> repliesReceived(@RequestBody GetRepliesForm getRepliesForm) {
        Connection conn = Database.connect();

        if (conn != null) {
            String query = "select id, sender, recipient, subject, content, sent, starred, file_attatchments, email_id_to_reply from emails where email_id_to_reply = ?";
            ArrayList<Email> emails = new ArrayList<Email>();

            //try-with-resources automatically closes the ps variable
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, getRepliesForm.email_id_to_reply);
                //ps.setString(2, getRepliesForm.email_id_to_reply);
                
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
                System.out.println("Query error line 119: " + e);
            }
            return emails;
        }
        //return an empty Email ArrayList if conn is null
        return new ArrayList<Email>();
    }

    //will be used by sendEmail to see if a user exists
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
                System.out.println("Query error line 97: " + e);
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
    public Email sendEmail(@RequestBody EmailForm emailForm) {
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
                //return an empty Email object if the email couldn't be inserted
                System.out.println("Query error line 171: " + e);
                return new Email("", "", "", "", null, 0, false, "", "");
            }
            if (recipientsFound > 0) {
                //return an Email object if inserted successfully with one of the existing recipients
                return new Email("", "", emailForm.sender, emailForm.recipients[existingUserIndex], "", System.currentTimeMillis(), false, "", "");
            }
            //return an empty Email object if none of the recipients exist
            return new Email("", "", "", "", null, 0, false, "", "");
        }
        //return an empty Email object if conn is null
        return new Email("", "", "", "", null, 0, false, "", "");
    }

    @PostMapping("/sendreply")
    public Email sendReply(@RequestBody ReplyForm replyForm) {
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
                System.out.println("Query error line 171: " + e);
                return new Email("", "", "", "", null, 0, false, "", "");
            }
            //return an empty Email object if none of the recipients exist
            return new Email("", "", "", "", null, 0, false, "", "");
        }
        //return an empty Email object if conn is null
        return new Email("", "", "", "", null, 0, false, "", "");
    }

    @PostMapping("/staremail")
    public Email starEmail(@RequestBody StarEmailForm starEmailForm) {
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
                //return null if the email couldn't be starred
                System.out.println("Query error line 205: " + e);
                return null;
            }
            return new Email("", "", "", "", "", 0, starEmailForm.starred, "", "");
        }
        //return an empty Email object if conn is null
        return new Email("", "", "", "", null, 0, false, "", "");
    }

    @PostMapping("/filteremails")
    public ArrayList<Email> filterEmails(@RequestBody FilterEmailForm filterEmailForm) {
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
                System.out.println("Query error line 250: " + e);
                return null;
            }
            return emails;
        }
        //return an empty Email ArrayList if conn is null
        return new ArrayList<Email>();
    }
}
