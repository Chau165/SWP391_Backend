import React from 'react';
import './BatteryPin.css'; // Đảm bảo import đúng tên file CSS

export default function BatteryPin() {
  return (
    <div className="battery-pin-page">
      {/* Hero Section */}
      <section className="hero">
        <img 
          src="/img-battery2.jpg" 
          alt="Battery Pin Hero" 
          className="hero-image"
        />
        <div className="hero-content">
          <h1>Battery Pin Technology</h1>
        </div>
      </section>

      {/* Content sections */}
      <section className="intro-section">
        <h2>Công nghệ Pin Tiên tiến</h2>
        <div className="intro-cards">
          <div className="card">
            <img src="/img-battery3.jpg" alt="Pin công nghệ cao" />
            <h3>Pin Lithium Ion</h3>
            <p>Công nghệ pin tiên tiến với tuổi thọ cao và khả năng sạc nhanh.</p>
          </div>
          <div className="card">
            <img src="/img-battery4.jpg" alt="Hệ thống quản lý pin" />
            <h3>Hệ thống BMS</h3>
            <p>Quản lý pin thông minh đảm bảo an toàn và hiệu suất tối ưu.</p>
          </div>
          <div className="card">
            <img src="/img-battery1.jpg" alt="Sạc nhanh" />
            <h3>Sạc Nhanh</h3>
            <p>Công nghệ sạc nhanh giúp tiết kiệm thời gian và tăng hiệu quả.</p>
          </div>
        </div>
      </section>

      {/* Video section */}
      <section className="video-section">
        <div className="video-inner">
          <video 
            className="responsive-video" 
            controls 
            poster="/img-battery.jpg"
          >
            <source src="/promo1.mp4" type="video/mp4" />
            Trình duyệt của bạn không hỗ trợ video.
          </video>
        </div>
      </section>
    </div>
  );
}