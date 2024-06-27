import { useEffect, useState } from "react";

//used for file uploading
import axios from "axios";

export default function ViewEmails() {
    let [emails, setEmails] = useState([]);
    let user = JSON.parse(localStorage.getItem("emailAddress"));

    //might be used later
    let [starImage, setStarImage] = useState("greyStar.jpg");

    useEffect(() => {
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
    }, [])

    //get all emails the logged in user received
    async function getEmails(user) {
        fetch(`http://localhost:8080/emails?recipient=${user['email']}`)
        .then((response) => response.json()).then((json) => setEmails(json));
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
        if (hours > 12) hours-=12;
        let time = day + " " + hours + ":" + minutes + ":" + seconds + " " + meridiem;  

        //let yearsAgo = new Date(Date.now()).getFullYear - new Date(email.sent).getFullYear();
        return time;
    }

    function Email({email}) {
        let time = calculateTime(email.sent);
        return (
            <tr>
                <th><img style={{'width': '16px', 'height': '16px'}} src={starImage}></img></th>
                <th>{email.subject}</th>
                <th>{email.sender}</th>
                <th>{time}</th>
            </tr>
        )
    }

    //creates a viewable list of recipients for sending an email
    let recipients = [];
    function addRecipient() {
        let recipient = document.getElementById('recipients').value;
        recipients.push(recipient);
        if (recipients.length == 1) {
            //add this only once
            document.getElementById('recipientList').innerText = "Recipients:\n";
        }
        document.getElementById('recipientList').innerText += recipient + "\n";
    }

    async function sendEmail(e) {
        e.preventDefault();
        let subject = e.target.subject.value;
        let content = e.target.content.value;
        let inputFields = {
            "recipients": recipients,
            "subject": subject, 
            "content": content
        };

        /* 
         * loop through each input field and check if any
         * are empty and highlight them red
         */
        
        let unfinishedFields = 0;
        for (let field in inputFields) {
            if (inputFields[field] == "") {
                document.getElementById(field).style.borderColor = "red";
                unfinishedFields++;
            } else {
                document.getElementById(field).style.borderColor = "";
            }
        }
        inputFields["sender"] = user['email'];
        if (unfinishedFields > 0) {
            alert(`You have ${unfinishedFields} empty inputs`);
            if (recipients.length == 0) {
                alert("Please enter at least 1 recipient");
            }
        } else {
            let response = await fetch("http://localhost:8080/sendemail", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(inputFields)
            })

            /* 
            * if the returned email object's
            * recipient isn't empty, that means
            * the email was sent successfully, otherwise
            * it wasn't
            */
            response.json().then((data) => {
                if (data['recipient'] == '') {
                    alert("This recipient doesn't exist");
                } else {
                    alert("Email sent successfully");
                    window.location.reload();
                }
            });
        }

    }

    function EmailForm() {
        return (
            <form onSubmit={sendEmail}>
                <input id='recipients' placeholder="Recipient"></input>
                <br></br>
                <input id='subject' placeholder="Subject"></input>
                <br></br>
                <textarea id='content' placeholder="Content"></textarea>
                <br></br>
                <button>Send Email</button>
            </form>
        )
    }

    async function uploadFiles(e) {
        e.preventDefault();

        //get files
        let files = document.getElementById("fileUpload").files;
        
        //create form data for POST request
        let formData = new FormData();
        try {
            /* 
             * append all files to form data so they can
             * be uploaded all at once
             */
            for (let i = 0; i < files.length; i++) {
                formData.append("file", files[i], files[i].name);
            }
        } catch {
            alert("No file was selected.");
        }
        await axios.post("http://localhost:8080/uploadfiles", formData);
    }

    function FileUploadForm() {
        return (
            <form method="POST" encType="multipart/form-data" onSubmit={uploadFiles}>
                <input type="file" name="file" id="fileUpload" multiple></input>
                <input type="submit" value="Upload"></input>
            </form>
        )
    }


    function EmailTable() {
        return (
            <table>
                <tr>
                    <th></th>
                    <th>Subject</th>
                    <th>Sender</th>
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

    return (
        <div>
            {/* returns a welcome message only if the user object is not null */}
            {user!==null ? <h3>Welcome back, {user['email']}!</h3> : <h3></h3>}
            <button onClick={logOut}>Log Out</button>
            <br></br>
            
            <p>File upload form:</p>
            <FileUploadForm/>
            <hr></hr>
            
            <br></br>

            <div id='recipientList'></div>
            <button onClick={addRecipient}>Add Recipient</button>
            <EmailForm/>
            
            <br></br>
            <EmailTable/>
            <br></br>
        </div>
    )
}