import 'bootstrap/dist/css/bootstrap.css';

import { useEffect } from "react";


export default function AccountSettingsPage() {
    document.title = "Account Settings";

    //load user object from localStorage
    let user = JSON.parse(localStorage.getItem("emailAddress"));

    let errorDisplayStyle = "display: grid; color: red; text-align: center";

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
        }
    }, []);

    function configDisplayMessage(message, style) {
        document.getElementById("displayMessage").innerText = message;
        document.getElementById("displayMessage").style = style;
    }

    function logOut() {
        localStorage.clear();
        window.location.href = "/login";
    }

    async function deleteAccount() {
        let response = await fetch(`http://localhost:8080/deleteaccount?email=${user["email"]}`);
        response.json().then((data) => {
            console.log(data);
            if (data['email'] === '') {
                alert("This account doesn't exist.");
            } else {
                alert("Successfully deleted your account.");
                logOut();
            }
        });
    }

    async function updatePassword(e) {
        e.preventDefault();
        let email = e.target.email.value;
        let password = e.target.password.value;
        let inputFields = {
            "email": email, 
            "password": password
        };

        let updateStatusCode = 0;

        try {
            let response = await fetch("http://localhost:8080/updatepassword", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(inputFields)
            }).then(function(response) {
                updateStatusCode = response.status;
                return response.status;
            });
            
            if (response['email'] === '') {
                alert("Couldn't update your password.");
            } else {
                alert("Successfully updated your password.");
                logOut();
            }
        } catch {
            switch (updateStatusCode) {
                case 0:
                    //display when the connection refuses
                    configDisplayMessage("Connection refused. You may not be connected to the internet or the server is down.", errorDisplayStyle);
                    break;
                case 500:
                    //display when user can't connect to the server
                    configDisplayMessage("A server error has occurred.", errorDisplayStyle);
                    break;
            }
        }
    }

    return (
        <div>
            <h1 style={{'textAlign': 'center'}}>Account Settings</h1>
            <hr></hr>
            <h3 id="displayMessage"></h3>
            <br></br>
            <br></br>
            <h3>Update Password</h3>
            <form onSubmit={updatePassword}>
                <div className="mb-3">
                    <input value={user["email"]} readOnly type="email" className="form-control" id="email"></input>
                </div>
                <div className="mb-3">
                    <label htmlFor="exampleInputPassword1" className="form-label">Update Password</label>
                    <input required type="password" className="form-control" id="password"></input>
                </div>
                <button type="submit" className="btn btn-primary">Update</button>
            </form>
            <hr></hr>

            <h3>Delete My Account</h3>
            <br></br>
            <p>If you want to delete your account, simply press the button below and you will be returned to the home page afterwards.</p>
            <button onClick={deleteAccount} className="btn btn-danger">Delete My Account</button>
            <hr></hr>
            
            <h3>Log Out</h3>
            <button className="btn btn-primary" onClick={logOut}>Log Out</button>
            <hr></hr>

        </div>
    )
}