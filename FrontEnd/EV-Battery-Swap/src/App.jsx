import React, { useState } from 'react'; // Bổ sung import React và useState
// SỬA LỖI: Đường dẫn Header phải là "./components/Header/Header"
import Header from './components/Header/Header';
import Footer from './components/Footer/Footer';
import LoginModal from './components/Login/LoginModal'; // Giữ nguyên

function App() {
  // STATE MỚI: Quản lý trạng thái mở/đóng Modal
  const [isLoginModalOpen, setIsLoginModalOpen] = useState(false); 

  const handleOpenModal = () => setIsLoginModalOpen(true);
  const handleCloseModal = () => setIsLoginModalOpen(false);

  return (
    <>
      {/* TRUYỀN PROP: Truyền hàm mở Modal vào Header */}
      <Header onLoginClick={handleOpenModal} /> 
      
      <main style={{ padding: 0, margin: 0 }}>
        {/* ... (Phần video giữ nguyên) ... */}
        <video
          autoPlay
          loop
          muted
          playsInline
          controls={false}
          style={{
            position: 'relative',
            width: '100%',
            height: '100%',
            objectFit: 'cover',
            display: 'block',
            background: '#000'
          }}
        >
          <source src="/promo.mp4" type="video/mp4" />
        </video>

        <Footer />
      </main>

      {/* RENDER MODAL: Chỉ hiển thị khi state là true */}
      <LoginModal 
        isOpen={isLoginModalOpen} 
        onClose={handleCloseModal} 
      />
    </>
  )
}

export default App