import { useEffect, useState } from "react";

//used for file uploading
import axios from "axios";

import 'bootstrap/dist/css/bootstrap.css';

export default function ViewEmails() {
    document.title = "Your Emails";

    //css style object for some div elements in a flex display
    let flexStyleObj = {
        "display": 'flex', 
        "justifyContent": 'center', 
        "gap": '5%'
    };

    //css style object for the menu when viewing an email's contents
    let viewEmailObj = {
        "display": 'none', 
        "flexDirection": 'column', 
        "alignItems": 'center',
        "position": 'absolute',
        "margin": 'auto',
        "width": '50%',
        "padding": '10px',
        "bottom": '10%',
        "right": '40%',
    };

    //css style object for textarea elements
    let textAreaStyleObj = {
        "resize": 'none', 
        "width": '350px', 
        "height": '200px'
    };

    //css style object to center text for some elements
    let textAlignObj = {'textAlign': "center"}

    //list of emails from database query
    let [emails, setEmails] = useState([]);

    //copy of user emails used for searching
    let [emailsCopy, setEmailsCopy] = useState([]);
    
    //load user object from localStorage
    let user = JSON.parse(localStorage.getItem("emailAddress"));

    //array that will be made from a string that will be separated by a delimiter
    let [fileAttatchments, setFileAttatchments] = useState([]);

    //offset used for sql query when getting a user's list of emails
    let [offset, setOffset] = useState(0);

    //how much do we change the offset with next/prev email buttons below the table
    let offsetChange = 25;

    //used to display what 'page' the user is in when going through many emails
    let [page, setPage] = useState(1);

    //set value of recipient field to send reply to this user
    let [replyRecipeint, setReplyRecipient] = useState("");

    //styles for displayed messages
    let errorDisplayStyle = "display: grid; color: red; text-align: center";
    let goodMessageStyle = "display: grid; color: green; text-align: center";


    useEffect(() => {
        configDisplayMessage("", "display: none");
        /**
         * read the logged in user's email address
         * saved in localStorage and use it to retrieve
         * all emails they received from backend to display
         * in a table, if the localStorage item is null, make
         * them log in first
         */
        if (user === null) {
            window.location.href = "/login";
        } else {
            getEmails(user);
        }
    }, [offset]);
    
    //get a number of the most recent emails the logged in user received
    async function getEmails(user) {
        let getEmailFormData = {
            "recipient": user["email"],
            "offset": offset
        };

        try {
            let data = await fetch("http://localhost:8080/emailsreceived", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(getEmailFormData)
            }).then(response => response.json());

            //remove current error message
            configDisplayMessage("", "display: none");
            
            setEmails(data);
            
            /**
             * create a copy of the emails array in localStorage
             * so emailsCopy can store it without being a reference
             * to it (so emailsCopy doesn't affect the original emails array)
             * so then it can be used for email searching with regex further below
             */
            localStorage.setItem("emails", JSON.stringify(data));
            setEmailsCopy(JSON.parse(localStorage.getItem("emails")));
            localStorage.removeItem("emails");

            //disable View Previous Emails button when on the first set of emails
            if (page == 1) {
                document.getElementById("viewPrevButton").disabled = true;
            } else {
                document.getElementById("viewPrevButton").disabled = false;
            }

            //disable View Next Emails button when the next set of emails is less than 25
            if (data.length < offsetChange) {
                document.getElementById("viewNextButton").disabled = true;
            } else {
                document.getElementById("viewNextButton").disabled = false;
            }
        } catch {
            //display when user can't connect to the server
            configDisplayMessage("Couldn't connect to the server", errorDisplayStyle);
        }
    }
    
    function logOut() {
        localStorage.clear();
        window.location.href = "/login";
    }

    function calculateTime(timestamp) {
        //the year, month, day and time of a unix timestamp
        let date = new Date(timestamp);
        let day = date.getFullYear() + "-" + parseInt(date.getMonth()+1) + "-" + date.getDate();
    
        let meridiem = "AM";

        let hours = date.getHours();
        let minutes = date.getMinutes();
        let seconds = date.getSeconds();

        if (date.getHours() < 10) {
            hours = "0" + date.getHours();
        }
        if (date.getMinutes() < 10) {
            minutes = "0" + date.getMinutes();
        }
        if (date.getSeconds() < 10) {
            seconds = "0" + date.getSeconds();
        }

        if (hours > 11) {
            meridiem = "PM";
        } else {
            meridiem = "AM";
        }
        if (hours > 12) {
            hours -= 12;
        }
        let time = day + " " + hours + ":" + minutes + ":" + seconds + " " + meridiem;  

        //let yearsAgo = new Date(Date.now()).getFullYear - new Date(email.sent).getFullYear();
        return time;
    }

    /**
     * display a gold star only on the email the user
     * clicked if it was a grey one by passing in the
     * email's id to uniquely identify from the rest 
     */
    async function starEmail(emailID) {
        let starEmailForm = {
            "emailID": emailID
        };

        let starSource = document.getElementById(emailID).src;
        if (starSource == 'http://127.0.0.1:5173/greyStar.jpg') {
            starEmailForm["starred"] = true;
            document.getElementById(emailID).src = "goldStar.jpg";
        } else {
            starEmailForm["starred"] = false;
            document.getElementById(emailID).src = "greyStar.jpg";
        }
        await fetch("http://localhost:8080/staremail", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(starEmailForm)
        });
    }

    let restOfPage = document.getElementById("restOfPage");
    
    //list of replies of a given email id
    let [replies, setReplies] = useState([]);
    
    //shows a container showing the currently selected email contents
    function viewEmail(email) {
        //hide the rest of page to better focus on the currently viewing email
        document.getElementById("restOfPage").style = "display: none";
        
        //set previously hidden container with current viewing email visible 
        document.getElementById("viewEmailContainer").style = "display: flex; flex-direction: column; align-items: center;";

        //do the same for the replies
        document.getElementById("viewRepliesContainer").style = "display: flex; flex-direction: column; align-items: center;";

        //set values based on current email
        document.getElementById("currentSender").innerText = "From " + email.sender;
        document.getElementById("currentSubject").innerText = "Subject: " + email.subject;
        document.getElementById("currentContent").innerText = email.content;
        document.getElementById("currentDate").innerText = calculateTime(email.sent);
        document.getElementById("currentRecipient").innerText = "to " + email.recipient;

        //array of filenames split by the | character as a delimiter
        setFileAttatchments(email.fileAttatchments.split("|"));

        /**
         * if replying to own sent email, sent reply recipient to
         * the recipient of the originally sent email
         */
        if (email.sender === user["email"]) {
            setReplyRecipient(email.recipient);
        } else {
            setReplyRecipient(email.sender);
        }

        //add id of email to reply to and see replies for
        localStorage.setItem("email_id_to_reply", email.id);

        //get list of replies of this email
        getReplies();
    }

    async function getReplies() {
        let getEmailFormData = {
            "recipient": user["email"],
            "email_id_to_reply": localStorage.getItem("email_id_to_reply")
        };

        try {
            let response = await fetch("http://localhost:8080/repliesreceived", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(getEmailFormData)
            });

            response.json().then((data) => {
                //remove current error message
                configDisplayMessageForReplies("", "display: none");

                setReplies(data);
            });
        } catch {
            //display when user can't connect to the server
            configDisplayMessageForReplies("Couldn't connect to the server", errorDisplayStyle);
        }
    }

    function closeEmail() {
        //make the rest of the page visible again
        document.getElementById("restOfPage").style = restOfPage.style;

        //hide the email and reply container
        document.getElementById("viewEmailContainer").style = "display: none";
        document.getElementById("viewRepliesContainer").style = "display: none";
    }

    function Email({email}) {
        let time = calculateTime(email.sent);
        
        
        //check if the email was starred to display a grey or gold star
        let starImage = "greyStar.jpg";
        if (email.starred) {
            starImage = "goldStar.jpg";
        }

        return (
            <tr onClick={() => {viewEmail(email)}}>
                <th><img onClick={() => {starEmail(email.id)}} id={email.id} style={{"width": "32px", "height": "32px"}} src={starImage}></img></th>
                {/**
                 * truncate the strings up to a length of 20 
                 * to keep the table viewable
                 */}
                <th>{email.sender.length > 20 ? email.sender.substring(0, 17) + "..." : email.sender}</th>
                <th>{email.recipient.length > 20 ? email.recipient.substring(0, 17) + "..." : email.recipient}</th>
                <th>{email.subject.length > 20 ? email.subject.substring(0, 17) + "..." : email.subject}</th>
                <th>{email.content.length > 20 ? email.content.substring(0, 17) + "..." : email.content}</th>
                <th>{time}</th>
            </tr>
        )
    }

    //creates a viewable list of recipients for sending an email
    let [recipients, setRecipients] = useState([]);
    function addRecipient() {
        let recipient = document.getElementById('recipients').value;
        
        //add a recipient as long as it isn't the user themself
        if (recipient !== user["email"]) {
            //make sure the recipient's name has at least 1 character
            if (recipient.length > 0) {
                setRecipients((recipients) => [...recipients, recipient]);
            } else {
                configDisplayMessage("Please enter at least 1 character", errorDisplayStyle);
            }
        } else {
            configDisplayMessage("You can't add yourself as a recipient", errorDisplayStyle);
        }
        if (recipients.length === 0) {
            //add this only once
            document.getElementById('recipientList').innerText = "Recipients:\n";
        }
    }

    //removes a recipient from the recipients list
    function removeRecipient(recipientToRemove) {
        setRecipients(recipients.filter((recipient) => recipient != recipientToRemove))
    }

    function getFileAttatchmentString() {
        let fileAttatchments = document.getElementById("fileUpload").files;
        let fileAttatchmentString = "";

        for (let i = 0; i < fileAttatchments.length; i++) {
            fileAttatchmentString += fileAttatchments[i].name;
            /**
             * add the delimiter after every filename except the last one
             * this will create a string such as "a|b|c" instead of "a|b|c|"
             */
            if (i != fileAttatchments.length-1) {
                fileAttatchmentString += "|";
            }
        }
        return fileAttatchmentString;
    }

    async function sendEmail(e) {
        e.preventDefault();
        let subject = e.target.subject.value;
        let content = e.target.content.value;
        let inputFields = {
            "recipients": recipients,
            "subject": subject, 
            "content": content, 
        };

        //add sender and file attatchments to POST request
        inputFields["sender"] = user["email"];
        inputFields["fileAttatchments"] = getFileAttatchmentString();

        if (recipients.length === 0) {
            configDisplayMessage("Please click the add recipient button at least once first", errorDisplayStyle);
        } else {
            try {
                let response = await fetch("http://localhost:8080/sendemail", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify(inputFields)
                });

                /* 
                * if the returned email object's
                * recipient isn't empty, upload any files
                * attatched to the email
                */
                response.json().then((data) => {
                    if (data["recipient"] === '') {
                        configDisplayMessage("This recipient doesn't exist", errorDisplayStyle);
                    } else {
                        configDisplayMessage("Email sent successfully!", goodMessageStyle);
                        //upload files if there are any
                        if (inputFields["fileAttatchments"] !== "") {
                            uploadFiles();
                        }
                    }
                });
            } catch {
                //display when user can't connect to the server
                configDisplayMessage("Couldn't connect to the server", errorDisplayStyle);
            }
        }
    }

    function openReplyForm() {
        //display reply and file upload forms
        document.getElementById("replyForm").style = "display: flex; justifyContent: center; gap: 5%";
    }

    async function sendReply(e) {
        e.preventDefault();

        let subject = e.target.replySubject.value;
        let content = e.target.replyContent.value;
        
        let inputFields = {
            "recipient": replyRecipeint,
            "subject": subject, 
            "content": content, 
            "email_id_to_reply": localStorage.getItem("email_id_to_reply")
        };

        //add sender and file attatchments to POST request
        inputFields["sender"] = user["email"];
        inputFields["fileAttatchments"] = getFileAttatchmentString();

        try {
            let response = await fetch("http://localhost:8080/sendreply", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(inputFields)
            });

            /* 
            * if the returned email object's
            * recipient isn't empty, upload any files
            * attatched to the email
            */

            response.json().then((data) => {
                if (data["recipient"] === '') {
                    configDisplayMessageForReplies("This recipient doesn't exist", errorMessageStyle);
                } else {
                    configDisplayMessageForReplies("Reply sent successfully!", goodMessageStyle);
                    
                    //upload files if there are any
                    if (inputFields["fileAttatchments"] !== "") {
                        uploadFiles();
                    }
                }
            });

            //reload page once reply is sent
            window.location.reload();
        } catch {
            //display when user can't connect to the server
            configDisplayMessageForReplies("Couldn't connect to the server", errorDisplayStyle);
        }
    }

    function EmailForm() {
        return (
            <form onSubmit={sendEmail}>
                <input title="username@mail.com" pattern="[A-Za-z0-9]+@mail.com" required type="email" id='recipients' placeholder="Recipient"></input>
                <br></br>
                <input required id='subject' placeholder="Subject"></input>
                <br></br>
                <textarea required style={textAreaStyleObj} id='content' placeholder="Content"></textarea>
                <br></br>
                <button>Send Email</button>
            </form>
        )
    }

    function ReplyForm() {
        return (
            <form onSubmit={sendReply}>
                <input value={replyRecipeint} title="username@mail.com" pattern="[A-Za-z0-9]+@mail.com" required type="email" id='replyRecipient' readOnly></input>
                <br></br>
                <input required id='replySubject' placeholder="Subject"></input>
                <br></br>
                <textarea required style={textAreaStyleObj} id='replyContent' placeholder="Content"></textarea>
                <br></br>
                <button>Send Reply</button>
            </form>
        )
    }

    async function uploadFiles() {
        //get uploaded files from file form
        let files = document.getElementById("fileUpload").files;
        
        //create form data for POST request
        let formData = new FormData();
        try {
            //append each file to form data and upload them one at a time
            for (let i = 0; i < files.length; i++) {
                //check if the current file is larger than 8MB
                if (files[i].size > 8000000) {
                    configDisplayMessage(`Cannot upload this file because it is larger than 8MB:\n ${files[i].name}`, errorDisplayStyle);
                } else {
                    formData.append("file", files[i], files[i].name);
                    await axios.post("http://localhost:8080/uploadfiles", formData);
                }
            }
        } catch {
            configDisplayMessage("No file was selected", errorDisplayStyle);
        }
        configDisplayMessage("File(s) uploaded successfully!", goodMessageStyle);
        window.location.reload();
    }

    function FileUploadForm() {
        return (
            <form method="POST" encType="multipart/form-data" onSubmit={uploadFiles}>
                <input type="file" name="file" id="fileUpload" multiple></input>
                {/* <input type="submit" value="Upload"></input> */}
            </form>
        )
    }

    function EmailTable() {
        return (
            <table className="table">
                <tr>
                    <th></th>
                    <th>Sender</th>
                    <th>Recipient</th>
                    <th>Subject</th>
                    <th>Content</th>
                    <th>Sent</th>
                </tr>

                {
                /**
                 * render a table filled with all emails 
                 * received by the logged in user
                 */
                emails.map((email) => {
                    return <Email email={email}/>
                })}
            </table>
        )
    }

    async function searchEmail(e) {
        e.preventDefault();

        let searchTerm = e.target.searchTerm.value;
        let searchCategory = e.target.searchCategory.value;

        let searchPattern = RegExp(searchTerm);
        let emailsFound = [];

        /**
         * using regular expression, depending on the selected
         * category to search for (subject, content, or sender)
         * the emailsFound array will be filled with email objects
         * that contain the matching pattern with the search term
         * 
         * for example, if the category is subject, and the search term
         * is "Urgent", the emailsFound array will be filled with email
         * objects whose subject contains the string "Urgent"
         */

        switch (searchCategory) {
            case "subject":
                for (let i = 0; i < emailsCopy.length; i++) {
                    if (searchPattern.test(emailsCopy[i].subject.toLowerCase())) {
                        emailsFound.push(emailsCopy[i]);
                    }
                }
                break;
            case "content":
                for (let i = 0; i < emailsCopy.length; i++) {
                    if (searchPattern.test(emailsCopy[i].content.toLowerCase())) {
                        emailsFound.push(emailsCopy[i]);
                    }
                }
                break;
            case "sender":
                for (let i = 0; i < emailsCopy.length; i++) {
                    if (searchPattern.test(emailsCopy[i].sender.toLowerCase())) {
                        emailsFound.push(emailsCopy[i]);
                    }
                }
                break;
        }
        setEmails(emailsFound);
    }

    function SearchEmailForm() {
        return (
            <form onSubmit={searchEmail}>
                <label>Search by</label>
                <select id='searchCategory'>
                    <option value='subject'>Subject</option>
                    <option value='content'>Content</option>
                    <option value='sender'>Sender</option>
                </select>
                <input id='searchTerm'></input>
                <button>Search</button>
            </form>
        )
    }

    function FilterEmailForm() {
        return (
            <form onSubmit={filterEmails}>
                <label>Filter emails by</label>
                <select id='sortBy'>
                    <option value="allReceived">All emails received</option>
                    <option value="allSent">All emails sent</option>
                    <option value="starred">Starred only</option>
                </select>
                <button>Filter</button>
            </form>
        )
    }

    /**
     * sends a post request to get a filtered list
     * of emails based on starred or unstarred emails
     */
    async function filterEmails(e) {
        e.preventDefault();

        let filterEmailForm = {
            "recipient": user["email"],
            "sortBy": e.target.sortBy.value,
        };
            
        if (e.target.sortBy.value == "allReceived") {
            getEmails(user);
        } else {
            try {
                let response = await fetch("http://localhost:8080/filteremails", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify(filterEmailForm)
                });
        
                response.json().then((data) => {
                    //remove current error message
                    configDisplayMessage("", "display: none");
                    
                    setEmails(data);
                    //disable View Next Emails button when the next set of emails is less than 25
                    if (data.length < offsetChange) {
                        document.getElementById("viewNextButton").disabled = true;
                    } else {
                        document.getElementById("viewNextButton").disabled = false;
                    }
                });
            } catch {
                //display when user can't connect to the server
                configDisplayMessage("Couldn't connect to the server", errorDisplayStyle);
            }
        }

    }

    //calls the getEmails function for the next 25 recent emails
    function increaseOffset() {
        setOffset((offset) => offset + offsetChange);
        setPage((page) => page + 1);

        //disable the button until the getEmails function finishes
        document.getElementById("viewNextButton").disabled = true;
    }

    //calls the getEmails function for the previous 25 recent emails
    function decreaseOffset() {
        if (offset >= 2) {
            setOffset((offset) => offset - offsetChange);
        }
        if (page >= 2) {
            setPage((page) => page - 1);
        }

        //disable the button until the getEmails function finishes
        document.getElementById("viewPrevButton").disabled = true;
    }

    function Reply({reply}) {
        return (
            <div>
                <div className="flex-container" style={flexStyleObj}>
                    <h3>Subject: {reply.subject}</h3>
                    <p>From {reply.sender}</p>
                    <p>{calculateTime(reply.sent)}</p>
                    <p>to {reply.recipient}</p>
                </div>
                <p style={{'textAlign': 'center'}}>{reply.content}</p>
                <br></br>
                <div>Download file attatchments:</div>
                {
                    /**
                     * only render the file attatchments if the fileAttatchments array contains
                     * strings of 1 or more file names
                     * 
                     * array of filenames split by the | character as a delimiter
                     */
                    fileAttatchments[0] != '""' ? fileAttatchments.map((filename) => {
                        return (<div><a href={`http://localhost:8080/files/${filename}`}>{filename}</a></div>);
                    }) : null
                }
                <hr></hr>
            </div>
        )
    }

    function configDisplayMessage(message, style) {
        document.getElementById("displayMessage").innerText = message;
        document.getElementById("displayMessage").style = style;
    }

    function configDisplayMessageForReplies(message, style) {
        document.getElementById("displayMessageForReplies").innerText = message;
        document.getElementById("displayMessageForReplies").style = style;
    }

    return (
        <div>
            <div id="restOfPage">
                {/* returns a welcome message only if the user object is not null */}
                {
                    user!==null ? 
                    <div style={flexStyleObj}> 
                        <h3>Welcome back, {user["email"]}!</h3>
                        <a href="/settings"><button className="btn btn-primary">Account Settings</button></a>
                        <button className="btn btn-primary" onClick={logOut}>Log Out</button>
                    </div> : <h3></h3>
                }

                <p style={textAlignObj}>Click on the content of an email to fully view it in a separate menu</p>
                <hr></hr>
                <div className="flex-container" style={flexStyleObj}>            
                    <div>
                        <h5>Search Emails</h5>
                        <SearchEmailForm/>
                    </div>

                    <div>
                        <h5>Filter Emails</h5>
                        <FilterEmailForm/>
                    </div>
                </div>
                
                <hr></hr>
                <h3 style={textAlignObj}>Your Emails</h3>
                <h3 id="displayMessage"></h3>
                
                <EmailTable/>
                
                <div className="flex-container" style={flexStyleObj}>
                    <button id="viewPrevButton" onClick={decreaseOffset}>View Previous Emails</button>
                    <button id="viewNextButton" onClick={increaseOffset}>View Next Emails</button>
                </div>
                <br></br>
                <p style={textAlignObj}>Page {page}</p>

                <hr></hr>

                <div className="flex-container" style={flexStyleObj} id='recipientList'></div>
                    {recipients.map((recipient) => {
                        return (<div className="flex-container" style={flexStyleObj}>{recipient}<button onClick={() => {removeRecipient(recipient)}}>Remove</button></div>)
                    })}

                <div className="flex-container" style={flexStyleObj}>
                    <div>
                        <h5>Send an email</h5>
                        <button onClick={addRecipient}>Add Recipient</button>
                        <EmailForm/>
                    </div>
                    <div>
                        <h5>Add file attatchments to email</h5>
                        <FileUploadForm/>
                    </div>
                </div>
            </div>
            
            <div id='viewEmailContainer' style={viewEmailObj}>
                <button onClick={closeEmail}>Close</button>
                <h3 id="displayMessageForReplies"></h3>
                <div className="flex-container" style={flexStyleObj}>
                    <h3 id='currentSubject'></h3>
                    <p id='currentSender'></p>
                    <p id='currentDate'></p>
                    <p id='currentRecipient'></p>
                </div>
                <p readOnly id='currentContent'></p>
                <div>Download file attatchments:</div>
                {
                    /**
                     * only render the file attatchments if the fileAttatchments array contains
                     * strings of 1 or more file names
                     * 
                     * array of filenames split by the | character as a delimiter
                     */
                    fileAttatchments[0] != '""' ? fileAttatchments.map((filename) => {
                        return (<div><a href={`http://localhost:8080/files/${filename}`}>{filename}</a></div>);
                    }) : null
                }
            </div>
            <hr></hr>

            <div id="viewRepliesContainer" style={viewEmailObj}>
                {
                    replies.map((reply) => {
                        return (
                            <Reply style={viewEmailObj} reply={reply}/>
                        )
                    })
                }
                <button onClick={openReplyForm}>Reply</button>

                <div id="replyForm" className="flex-container" style={{"display": "none"}}>
                    <div>
                        <h5>Send a reply</h5>
                        <ReplyForm/>
                    </div>
                    <div>
                        <h5>Add file attatchments to reply</h5>
                        <FileUploadForm/>
                    </div>
                </div>
            </div>

        </div>
    )
}