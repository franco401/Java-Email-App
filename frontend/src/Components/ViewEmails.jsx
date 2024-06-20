import { useEffect, useState } from "react";

export default function ViewEmails() {
    let [emails, setEmails] = useState([]);
    let user = JSON.parse(localStorage.getItem("emailAddress"));
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
                <th>{email.content}</th>
                <th>{email.sender}</th>
                <th>{time}</th>
            </tr>
        )
    }

    async function sendEmail(e) {
        e.preventDefault();
        let recipient = e.target.recipient.value;
        let content = e.target.content.value;
        let inputFields = {
            "recipient": recipient, 
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
            alert(`You have ${unfinishedFields} empty inputs`)
        } else {
            let response = await fetch("http://localhost:8080/sendemail", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(inputFields)
            })
            response.json().then((data) => {
                if (data['recipient'] == '') {
                    alert("This recipient doesn't exist");
                } else {
                    alert("Email sent successfully");
                }
            });
        }

    }

    function EmailForm() {
        return (
            <form onSubmit={sendEmail}>
                <input id='recipient' placeholder="Recipient"></input>
                <br></br>
                <textarea id='content' placeholder="Content"></textarea>
                <br></br>
                <button>Send Email</button>
            </form>
        )
    }

    return (
        <div>
            <h3>Welcome back, {user['email']}!</h3>
            <button onClick={logOut}>Log Out</button>
            <table>
                <tr>
                    <th></th>
                    <th>Content</th>
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
            <EmailForm/>
        </div>
    )
}