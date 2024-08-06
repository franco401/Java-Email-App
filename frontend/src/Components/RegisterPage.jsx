import 'bootstrap/dist/css/bootstrap.css';

export default function RegisterPage() {
    async function register(e) {
        e.preventDefault();
        let email = e.target.email.value;
        let password = e.target.password.value;
        let inputFields = {
            "email": email, 
            "password": password
        };

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
                alert("The username and/or password are already taken.");
            } else {
                alert("Successfully registered account");
                window.location.href = "/login";
            }
        });
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
                    <input required type="password" className="form-control" id="password"></input>
                </div>
                <button type="submit" className="btn btn-primary">Register</button>
            </form>
            <span>Already have an account? Login <a href="/login">here</a></span>
        </>
    )
}