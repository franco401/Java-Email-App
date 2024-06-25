export default function HomePage() {
    async function readData() {
        fetch("http://localhost:8080/emails?receiver=user8@mail.com")
        .then((response) => response.json()).then((json) => console.log(json));
    }

    return (
        <>
            <h1>Home Page</h1>
            <div className="mb-3"></div>
            <a href="/register">Register</a>
            <br></br>
            <a href="/login">Login</a>
            <br></br>
        </>
    )
}