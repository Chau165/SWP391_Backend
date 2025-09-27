import React from 'react';
import './Battery.css';


// Đường dẫn này hoạt động vì img-battery.jpg nằm trong thư mục public
const heroImagePath = "/img-battery.jpg"; 

export default function Battery() {
  return (
    <div className="battery-page">
      
      {/* 1. HERO SECTION */}
      <section className="hero-section">
        <img 
          src={heroImagePath} 
          alt="Trạm đổi pin EV Battery Swap" 
          className="hero-image"
        />
        {/* LOẠI BỎ CLASS container để dễ dàng căn chỉnh Full-width */}
        <div className="hero-overlay"> 
            <h1>Công Nghệ Pin & Trao Đổi Điện Năng Tương Lai</h1>

        </div>
      </section>

      {/* 2. OVERVIEW SECTION: Giới thiệu chung */}
      <section className="overview-section container">
        <h2>Sức Mạnh Của Pin Lithium-Ion Thông Minh</h2>
        <div className="content-grid">
            <p>Các gói pin của chúng tôi được thiết kế để tối ưu hóa hiệu suất và tuổi thọ. Với công nghệ làm mát tiên tiến và hệ thống quản lý pin (BMS) AI, chúng tôi đảm bảo mỗi lần trao đổi đều mang lại trải nghiệm tốt nhất.</p>
            <ul>
                <li>Hiệu suất cao</li>
                <li>Thời gian sạc/đổi cực nhanh</li>
                <li>Độ bền và An toàn đã được kiểm chứng</li>
            </ul>
        </div>
      </section>

      {/* 3. COMPATIBILITY SECTION: Các dòng xe tương thích */}
      <section className="compatibility-section">
        <div className="container">
            <h2>Mẫu Xe Tương Thích</h2>
            <p>Hệ thống pin của chúng tôi hỗ trợ nhiều dòng xe điện phổ biến, từ xe tay ga đến các mẫu xe bốn bánh đô thị.</p>
        </div>
      </section>

      {/* 4. CALL TO ACTION SECTION: Kêu gọi hành động */}
      <section className="cta-section">
        <div className="container">
            <h3>Bạn đã sẵn sàng chuyển sang năng lượng điện?</h3>
            <a href="/goshare" className="cta-button">Tìm hiểu về Chính sách của chúng tôi</a>
        </div>
      </section>

    </div>
  );
}