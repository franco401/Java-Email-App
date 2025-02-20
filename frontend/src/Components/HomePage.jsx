import 'bootstrap/dist/css/bootstrap.css';

export default function HomePage() {
    document.title = "Home Page";

    
    let center = {
        'margin': '0',
        'position': 'absolute',
        'top': '45%',
        'left': '35%',
        'MsTransform': 'translate(-50%, -50%)',
        'transform': 'translate(-50%, -50%)'
    };

    let flexStyleObj = {
        'display': 'flex', 
        'justifyContent': 'center', 
        'gap': '10%',
        'backgroundColor': '#40db8b',
        'height': '40%'
    };

    let footerStyleObj = {
        'display': 'flex', 
        'justifyContent': 'center', 
        'gap': '10%',
        'backgroundColor': '#40db8b',
        'height': '50px'
    };

    let imgStyle = {
        'width': '200px', 
        'height': '200px',
        'margin': '0',
        'position': 'absolute',
        'top': '45%',
        'left': '82%',
        'MsTransform': 'translate(-50%, -50%)',
        'transform': 'translate(-50%, -50%)'
    };

    let bodyStyleObj = {
        'position': 'relative',
        'height': '85vh'
    };

    return (
        <div>
            <div id="header" style={flexStyleObj}>
                <img style={{'height': '50px', 'weight': '50px'}} src="email_logo.png"></img>
                <a style={{'color': 'white'}} href='/'>Home</a>
                <a style={{'color': 'white'}} href='/register'>Register</a>
                <a style={{'color': 'white'}} href='/login'>Login</a>
            </div>

            <div style={bodyStyleObj} id="body">
                <h2 style={center}>
                    This full-stack Email application was made using Java with Spring Boot in the backend
                    and JavaScript with React in the frontend.
                </h2>
                <img style={imgStyle} src="email_logo.png"></img>
            
            </div>

            <div id="footer" style={footerStyleObj}>
                <a style={{'color': 'white'}} href="https://github.com/franco401">My Github</a>
                <a style={{'color': 'white'}} href="https://www.linkedin.com/in/franco-jimenez2002/">My LinkedIn</a>
            </div>
        </div>
    )
}