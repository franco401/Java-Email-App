import 'bootstrap/dist/css/bootstrap.css';

export default function LoginPage() {
    document.title = "Login Page";

    let errorDisplayStyle = "display: grid; color: red; text-align: center";
    let goodMessageStyle = "display: grid; color: green; text-align: center";

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
        
        try {
            let response = await fetch("http://localhost:8080/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(inputFields)
            })
            
            response.json().then((data) => {
                if (data['email'] === '') {
                    configDisplayMessage("The email and password entered were invalid", errorDisplayStyle);
                } else {
                    configDisplayMessage("Successfully logged in", goodMessageStyle);
                    localStorage.setItem("emailAddress", JSON.stringify(data));
                    window.location.href = "/emails";
                }
            });
        } catch {
            //display when user can't connect to the server
            configDisplayMessage("Couldn't connect to the server", errorDisplayStyle);
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