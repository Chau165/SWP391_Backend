// src/App.jsx

import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Header from './components/Header/Header';
import Footer from './components/Footer/Footer';
import Home from './pages/Home/Home';
import Battery from './pages/Battery Electric/Battery';
import BatteryPin from './pages/Battery Electric/BatteryPin'; // Thêm import
import AboutUs from './pages/About Us/AboutUs';
import LoginModal from './components/Login/LoginModal';
import './App.css';

function App() {
  const [isLoginOpen, setIsLoginOpen] = useState(false);

  return (
    <Router>
      <div className="App">
        <Header onLoginClick={() => setIsLoginOpen(true)} />
        
        <main>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/battery" element={<Battery />} />
            <Route path="/battery-pin" element={<BatteryPin />} /> {/* Thêm route mới */}
            <Route path="/about" element={<AboutUs />} />
          </Routes>
        </main>

        <Footer />
        
        <LoginModal 
          isOpen={isLoginOpen} 
          onClose={() => setIsLoginOpen(false)} 
        />
      </div>
    </Router>
  );
}

export default App;