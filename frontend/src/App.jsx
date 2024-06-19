import { BrowserRouter, Routes, Route } from 'react-router-dom';

import RegisterPage from './Components/RegisterPage';
import LoginPage from './Components/LoginPage';
import HomePage from './Components/HomePage';
import ViewEmails from './Components/ViewEmails';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path='' element={<HomePage/>}/>
        <Route path='/register' element={<RegisterPage/>}/>
        <Route path='/login' element={<LoginPage/>}/>
        <Route path='/emails' element={<ViewEmails/>}/>
      </Routes>
    </BrowserRouter>
  )
}

export default App
