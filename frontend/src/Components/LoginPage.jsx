import 'bootstrap/dist/css/bootstrap.css';
import { useEffect } from 'react';

export default function LoginPage() {
    document.title = "Login Page";

    let errorDisplayStyle = "display: grid; color: red; text-align: center";
    let goodMessageStyle = "display: grid; color: green; text-align: center";

    //load user object from localStorage
    let user = JSON.parse(localStorage.getItem("emailAddress"));

    useEffect(() => {
        //if the user is already logged in, take them back to the emails page
        if (user !== null) {
            window.location.href = "/emails";
        }
    }, []);

    function configDisplayMessage(message, style) {
        document.getElementById("displayMessage").innerText = message;
        document.getElementById("displayMessage").style = style;
    }

    async function login(e) {
        e.preventDefault();
        let email = e.target.email.value;
        let password = e.target.password.value;
        let inputFields = {
            "email": email, 
            "password": password
        };
        
        let loginStatusCode = 0;

        try {
            let response = await fetch("http://localhost:8080/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(inputFields)
            }).then(function(response) {
                loginStatusCode = response.status;
                return response.json();
            });
            
            if (response['email'].length > 0) {
                configDisplayMessage("Successfully logged in", goodMessageStyle);
                localStorage.setItem("emailAddress", JSON.stringify(response));
                window.location.href = "/emails";
            }
        } catch {
            switch (loginStatusCode) {
                case 0:
                    //display when the connection refuses
                    configDisplayMessage("Connection refused. You may not be connected to the internet or the server is down.", errorDisplayStyle);
                    break;
                case 204:
                    configDisplayMessage("The email and password entered were invalid", errorDisplayStyle);
                    break;
                case 403:
                    //display when user is blocked from making more requests
                    configDisplayMessage("You are currently blocked from making any requests", errorDisplayStyle);
                    break;
                case 429:
                    //display when user has done too many requests
                    configDisplayMessage("You have made too many requests. You will be temporarily blocked from making requests for 1 minute.", errorDisplayStyle);
                    break;
                case 500:
                    //display when user can't connect to the server
                    configDisplayMessage("A server error has occurred", errorDisplayStyle);
                    break;
            }
        }
    }

    return (
        <>
            <h1 style={{'textAlign': 'center'}}>Login Page</h1>
            <form onSubmit={login}>
                <div className="mb-3">
                    <label htmlFor="exampleInputEmail1" className="form-label">Email address</label>
                    <input title="username@mail.com" pattern="[A-Za-z0-9]+@mail.com" required type="email" className="form-control" id="email" aria-describedby="emailHelp"></input>
                </div>
                <div className="mb-3">
                    <label htmlFor="exampleInputPassword1" className="form-label">Password</label>
                    <input required type="password" className="form-control" id="password"></input>
                </div>
                <button type="submit" className="btn btn-primary">Login</button>
            </form>
            <span>Don't have an account? Create one <a href="/register">here</a></span>
            
            <br></br>
            <br></br>
            <h3 id="displayMessage"></h3>
        </>
    )
}