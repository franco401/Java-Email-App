# Java-Email-App
A full stack, email application using Java in the backend with Spring Boot and JavaScript in the frontend with React.

# Features
* Users can make an account
* Users can update their password
* Users can delete their account
* Users can send an email to mulitple recipients
* Users can view a table of emails they received including starred ones as well or starred only
* Users can view the emails they sent to others
* Users can attatch multiple files (with a total limit of 8MB) when sending an email
* Users can download the attatched files from emails they received
* The content of sent emails are encrypted when uploaded to the backend and decrypted when viewed in the frontend
* Users can reply to each other's emails

# Versions Used
* Spring Boot 3.3.0
* Java 22

# Spring Boot Dependencies Used
* spring-boot-starter-web
* lombok
* spring-boot-starter-test
* commons-codec (Version 1.17)
* sqlite-jdbc (Version 3.46.0.1)

# React Dependencies Used
* react (Version 18.2.0)
* react-router-dom (Version 6.23.1)
* axios (1.7.4)
* bootstrap (Version 5.3.3)

## Installation Guide (Windows)

1. Download Docker Desktop: https://docs.docker.com/desktop/install/windows-install/
2. Download the source code and have Docker Desktop open
3. Go to the "emailapp" folder, open command prompt and enter:
```
docker build -t spring-app .
```
4. Go to the "frontend" folder, open command prompt and enter:
```
docker build -t react-app .
```
5. (Optional) Check to see if the images were made by opening command prompt and entering:
```
docker images
```
6. To start the Spring Boot server, open command prompt and enter:
```
docker run -d -p 127.0.0.1:8080:8080 spring-app
```
7. To start the React app, open command prompt and enter:
```
docker run -d -p 127.0.0.1:5173:5173 react-app
```
8. The project should now be accessible at: http://127.0.0.1:5173/