# Java Email Web App
A full stack, email web application using Java in the backend with Spring Boot and JavaScript in the frontend with React.

## Installation Guide (Windows)

1. Download and install Docker Desktop if you don't have it installed: https://docs.docker.com/desktop/install/windows-install/

2. Download the source code or clone it with the command prompt or powershell using:
```
git clone http://github.com/franco401/Java-Email-App.git
```

3. Run install.ps1 to install the web application
4. Run uninstall.ps1 once you're done with the project

## Installation Guide (Linux)
1. Download and install Docker by running the following command in a terminal window if you don't have it installed: 
````
sudo apt install docker.io
````

2. Download the source code or clone it with the terminal using:
```
git clone http://github.com/franco401/Java-Email-App.git
```

3. Run install.sh to install the web application
4. Run uninstall.sh once you're done with the project


# Access the application at http://127.0.0.1:5173/ once everything is setup (install.ps1 and install.sh will open this link for you but it's here incase it's needed)

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
* Added rate limiting to Spring Boot server using two hashmaps