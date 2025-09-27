import React, { useState } from 'react';
import Header from './components/Header/Header';
import Footer from './components/Footer/Footer';
import LoginModal from './components/Login/LoginModal';
import Home from './pages/Home/Home';

function App() {
  // STATE MỚI: Quản lý trạng thái mở/đóng Modal
  const [isLoginModalOpen, setIsLoginModalOpen] = useState(false); 

  const handleOpenModal = () => setIsLoginModalOpen(true);
  const handleCloseModal = () => setIsLoginModalOpen(false);

  return (
    <>
      <Header onLoginClick={handleOpenModal} />
      <Home />
      <Footer />
      <LoginModal 
        isOpen={isLoginModalOpen} 
        onClose={handleCloseModal} 
      />
    </>
  )
}

export default App