import 'bootstrap/dist/css/bootstrap.css';
import { useEffect } from 'react';

export default function RegisterPage() {
    document.title = "Register Page";

    let errorDisplayStyle = "display: grid; color: red; text-align: center";
    let decentMessageStyle = "display: grid; color: #d6e02b; text-align: center";
    let goodMessageStyle = "display: grid; color: green; text-align: center";

    let center = {
        'margin': '0',
        'position': 'absolute',
        'top': '40%',
        'left': '50%',
        'MsTransform': 'translate(-50%, -50%)',
        'transform': 'translate(-50%, -50%)',
        
    };

    let spanStyleObj = {
        'margin': '0',
        'position': 'absolute',
        'top': '70%',
        'left': '51%',
        'MsTransform': 'translate(-50%, -50%)',
        'transform': 'translate(-50%, -50%)'
    };

    useEffect(() => {
        document.getElementById("registerButton").disabled = true;
    })

    function configDisplayMessage(message, style) {
        document.getElementById("displayMessage").innerText = message;
        document.getElementById("displayMessage").style = style;
    }

    function checkPasswordLength(e) {
        e.preventDefault();
        if (e.target.value.length < 8) {
            configDisplayMessage("Password length is too short", errorDisplayStyle);
            document.getElementById("registerButton").disabled = true;
        } else if (e.target.value.length >= 8 && e.target.value.length < 12) {
            configDisplayMessage("Password length is decent", decentMessageStyle);
            document.getElementById("registerButton").disabled = true;
        } else {
            configDisplayMessage("Password length is good", goodMessageStyle);
            document.getElementById("registerButton").disabled = false;
        }
    }

    async function register(e) {
        e.preventDefault();
        let email = e.target.email.value;
        let password = e.target.password.value;
        let inputFields = {
            "email": email, 
            "password": password
        };

        let registerStatusCode = 0;

        try {
            let response = await fetch("http://localhost:8080/register", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(inputFields)
            }).then(function(response) {
                registerStatusCode = response.status;
                return response.json();
            });
    
            if (response['email'].length > 0) {
                configDisplayMessage("Successfully registered account", goodMessageStyle);
                window.location.href = "/login";
            }
            
        } catch {
            switch (registerStatusCode) {
                case 0:
                    //display when the connection refuses
                    configDisplayMessage("Connection refused. You may not be connected to the internet or the server is down.", errorDisplayStyle);
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
                    configDisplayMessage("A server error has occurred. It could be that this email address is already taken.", errorDisplayStyle);
                    break;
            }
        }
    }

    return (
        <div>
            <h1 style={{'textAlign': 'center', 'position': 'absolute', 'top': '8%', 'left': '37%'}}>Create Your Account</h1>
            <form style={center} onSubmit={register}>
                <div style={{'width': '400px'}} className="mb-3">
                    <input placeholder='Email address' title="username@mail.com" pattern="[A-Za-z0-9]+@mail.com" required type="email" className="form-control" id="email" aria-describedby="emailHelp"></input>
                </div>
                <br></br>
                <div className="mb-3">
                    <input placeholder='Password' onInput={checkPasswordLength} required type="password" className="form-control" id="password"></input>
                </div>
                <br></br>
                <button id="registerButton" type="submit" className="btn btn-primary">Register</button>
            </form>
            <span style={spanStyleObj}>Already have an account? <a href="/login">Login</a></span>

            <br></br>
            <br></br>
            <h3 id="displayMessage"></h3>
        </div>
    )
}