export default function LoginPage() {
    async function login(e) {
        e.preventDefault();
        let email = e.target.email.value;
        let password = e.target.password.value;
        let inputFields = {
            "email": email, 
            "password": password
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

        if (unfinishedFields > 0) {
            alert(`You have ${unfinishedFields} empty inputs`)
        } else {
            try {
                let response = await fetch("http://localhost:8080/login", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify(inputFields)
                });
                console.log(response);
            } catch {
                alert("Something went wrong.")
            }
        }
    }

    return (
        <>
            <h1>Login Page</h1>
            <form onSubmit={login}>
                <div className="mb-3">
                    <label htmlFor="exampleInputEmail1" className="form-label">Email address</label>
                    <input type="email" className="form-control" id="email" aria-describedby="emailHelp"></input>
                </div>
                <div className="mb-3">
                    <label htmlFor="exampleInputPassword1" className="form-label">Password</label>
                    <input type="password" className="form-control" id="password"></input>
                </div>
                <button type="submit" className="btn btn-primary">Login</button>
            </form>
        </>
    )
}