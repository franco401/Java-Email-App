import 'bootstrap/dist/css/bootstrap.css';
import { useEffect } from 'react';

export default function RegisterPage() {
    document.title = "Register Page";

    let errorDisplayStyle = "display: grid; color: red; text-align: center";
    let decentMessageStyle = "display: grid; color: #d6e02b; text-align: center";
    let goodMessageStyle = "display: grid; color: green; text-align: center";

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

        try {
            let response = await fetch("http://localhost:8080/register", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(inputFields)
            });
    
            response.json().then((data) => {
                console.log(data);
                if (data['email'] === '') {
                    configDisplayMessage("This email is already taken", errorDisplayStyle);
                } else {
                    configDisplayMessage("Successfully registered account", goodMessageStyle);
                    window.location.href = "/login";
                }
            });
        } catch {
            //display when user can't connect to the server
            configDisplayMessage("Couldn't connect to the server", errorDisplayStyle);
        }
    }

    return (
        <>
            <h1 style={{'textAlign': 'center'}}>Register Page</h1>
            <form onSubmit={register}>
                <div className="mb-3">
                    <label htmlFor="exampleInputEmail1" className="form-label">Email address</label>
                    <input title="username@mail.com" pattern="[A-Za-z0-9]+@mail.com" required type="email" className="form-control" id="email" aria-describedby="emailHelp"></input>
                </div>
                <div className="mb-3">
                    <label htmlFor="exampleInputPassword1" className="form-label">Password</label>
                    <input onInput={checkPasswordLength} required type="password" className="form-control" id="password"></input>
                </div>
                <button id="registerButton" type="submit" className="btn btn-primary">Register</button>
            </form>
            <span>Already have an account? Login <a href="/login">here</a></span>

            <br></br>
            <br></br>
            <h3 id="displayMessage"></h3>
        </>
    )
}