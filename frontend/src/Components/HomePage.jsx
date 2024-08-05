import 'bootstrap/dist/css/bootstrap.css';

export default function HomePage() {
    return (
        <>
            <h1>Home Page</h1>
            <div className="mb-3"></div>
            <a href="/login">Login</a>
            <br></br>
            <a href="/register">Register</a>
            <br></br>
        </>
    )
}