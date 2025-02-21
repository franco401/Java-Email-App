import 'bootstrap/dist/css/bootstrap.css';
import { useEffect } from 'react';

export default function LoginPage() {
    document.title = "Login Page";

    let errorDisplayStyle = "margin: 0; position: absolute; top: 80%; left: 30%; display: grid; color: blue; text-align: center";
    let goodMessageStyle = "margin: 0; position: absolute; top: 80%; left: 30%; display: grid; color: green; text-align: center";

    let connectionErrorStyle = "margin: 0; position: absolute; top: 80%; left: 20%; display: grid; color: red; text-align: center";

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
        'left': '50%',
        'MsTransform': 'translate(-50%, -50%)',
        'transform': 'translate(-50%, -50%)'
    };

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
                    configDisplayMessage("Connection refused. You may not be connected to the internet or the server is down.", connectionErrorStyle);
                    break;
                case 204:
                    configDisplayMessage("The email and password entered were invalid", connectionErrorStyle);
                    break;
                case 403:
                    //display when user is blocked from making more requests
                    configDisplayMessage("You are currently blocked from making any requests", connectionErrorStyle);
                    break;
                case 429:
                    //display when user has done too many requests
                    configDisplayMessage("You have made too many requests. You will be temporarily blocked from making requests for 1 minute.", connectionErrorStyle);
                    break;
                case 500:
                    //display when user can't connect to the server
                    configDisplayMessage("A server error has occurred", connectionErrorStyle);
                    break;
            }
        }
    }

    return (
        <div>
            <h1 style={{'textAlign': 'center', 'margin': 'auto', 'position': 'absolute', 'top': '8%', 'left': '39%'}}>Login to Your Account</h1>
            <form style={center} onSubmit={login}>
                <div style={{'width': '400px'}} className="mb-3">
                    <input placeholder='Email address' title="username@mail.com" pattern="[A-Za-z0-9]+@mail.com" required type="email" className="form-control" id="email" aria-describedby="emailHelp"></input>
                </div>
                <br></br>
                <div className="mb-3">
                    <input placeholder='Password' required type="password" className="form-control" id="password"></input>
                </div>
                <br></br>
                <button style={{'width': '100px'}} type="submit" className="btn btn-primary">Login</button>
            </form>
            
            <div>
                <span style={spanStyleObj}>Don't have an account? <a href="/register">Sign up</a></span>
            </div>
            
            <br></br>
            <br></br>
            <h3 id="displayMessage"></h3>
        </div>
    )
}