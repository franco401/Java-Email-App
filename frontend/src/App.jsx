import { BrowserRouter, Routes, Route } from 'react-router-dom';

import RegisterPage from './Components/RegisterPage';
import LoginPage from './Components/LoginPage';
import HomePage from './Components/HomePage';
import ViewEmails from './Components/ViewEmails';
import AccountSettingsPage from './Components/AccountSettingsPage';

function App() {
  document.title = "Home Page";
  return (
    <BrowserRouter>
      <Routes>
        <Route path="" element={<HomePage/>}/>
        <Route path="/register" element={<RegisterPage/>}/>
        <Route path="/login" element={<LoginPage/>}/>
        <Route path="/emails" element={<ViewEmails/>}/>
        <Route path="/settings" element={<AccountSettingsPage/>}/>
      </Routes>
    </BrowserRouter>
  )
}

export default App
