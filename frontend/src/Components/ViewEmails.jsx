import { useEffect, useState } from "react";

export default function ViewEmails() {
    let [emails, setEmails] = useState([]);

    useEffect(() => {
        /**
         * read the logged in user's email address
         * saved in localStorage and use it to retrieve
         * all emails they received from backend to display
         * in a table, if the localStorage item is null, make
         * them log in first
         */
        let user = JSON.parse(localStorage.getItem("emailAddress"));
        if (user === null) {
            window.location.href = "/login";
        } else {
            getEmails(user);
        }
    }, [])

    //get all emails the logged in user received
    async function getEmails(user) {
        fetch(`http://localhost:8080/emails?receiver=${user['email']}`)
        .then((response) => response.json()).then((json) => setEmails(json));
    }
    
    function logOut() {
        localStorage.clear();
        window.location.href = "/login";
    }

    function Email({email}) {
        return (
            <tr>
                <th>{email.content}</th>
                <th>{email.sender}</th>
                <th>{email.sent}</th>
            </tr>
        )
    }

    return (
        <div>
            <button onClick={logOut}>Log Out</button>
            <table>
                <tr>
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
        </div>
    )
}