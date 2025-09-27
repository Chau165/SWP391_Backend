import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Header from './components/Header/Header';
import Footer from './components/Footer/Footer';
import LoginModal from './components/Login/LoginModal';
import Home from './pages/Home/Home';
import AboutUs from './pages/About Us/AboutUs';

function App() {
  // STATE MỚI: Quản lý trạng thái mở/đóng Modal
  const [isLoginModalOpen, setIsLoginModalOpen] = useState(false); 

  const handleOpenModal = () => setIsLoginModalOpen(true);
  const handleCloseModal = () => setIsLoginModalOpen(false);

  return (
    <Router>
      <Header onLoginClick={handleOpenModal} />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/about" element={<AboutUs />} />
        {/* Add more routes here as needed */}
      </Routes>
      <Footer />
      <LoginModal 
        isOpen={isLoginModalOpen} 
        onClose={handleCloseModal} 
      />
    </Router>
  )
}

export default App