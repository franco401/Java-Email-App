import 'bootstrap/dist/css/bootstrap.css';

export default function HomePage() {
    document.title = "Home Page";

    let container = {
        'position': 'relative',
        'height': '650px'
    };
    
    let center = {
        'margin': '0',
        'position': 'absolute',
        'top': '50%',
        'left': '50%',
        'MsTransform': 'translate(-50%, -50%)',
        'transform': 'translate(-50%, -50%)'
    };

    let flexStyleObj = {
        'display': 'flex', 
        'justifyContent': 'center', 
        'gap': '5%'
    };

    let imgStyle = {
        'width': '100px', 
        'height': '100px'
    };

    return (
        <div style={container}>
            <br></br>
            <br></br>
            <p style={{'textAlign': 'center'}}>Created with these technologies</p>
            <div style={flexStyleObj}>
                <img style={imgStyle} src="java.svg"></img>
                <img style={imgStyle} src="springBoot.png"></img>
                <img style={imgStyle} src="js.png"></img>
                <img style={imgStyle} src="react.png"></img>
                <img style={imgStyle} src="bootstrap.png"></img>
            </div>
            <div style={center}>
                <h1>Java Email App</h1>
                <a href="/login">Login</a>
                <br></br>
                <a href="/register">Register</a>
            </div>
        </div>
    )
}