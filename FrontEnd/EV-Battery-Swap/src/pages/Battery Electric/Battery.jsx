import React from 'react';
import './Battery.css';


// Đường dẫn này hoạt động vì img-battery.jpg nằm trong thư mục public
const heroImagePath = "/img-battery.jpg"; 
const overviewImagePath = "/img-battery1.jpg"; 

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
            {/* Dòng chữ nhỏ phía trên (ví dụ: Gogoro Network) */}
            <p className="hero-subheading">EV Battery Swap Network</p> 
            {/* Dòng chữ lớn (ví dụ: Swap & Go in seconds.) */}
            <h1>Swap & Go</h1>
            {/* Dòng chữ nhỏ phía dưới (ví dụ: Electric fuel reimagined.) */}
            <p>Electric fuel reimagined.</p>
        </div>
      </section>

       {/* 2. OVERVIEW SECTION: Bố cục 2 cột (Ảnh trái, Nội dung phải) */}
      <section className="overview-section container">
        {/* Tiêu đề lớn, căn giữa */}
        <h2 className="overview-title">The most advanced energy platform for Electric Vehicle</h2>
        
        <div className="two-column-layout">
            
            {/* CỘT HÌNH ẢNH BÊN TRÁI */}
            <div className="image-col">
                <img 
                    src={overviewImagePath} 
                    alt="Công nghệ Pin" 
                    className="overview-image"
                />
            </div>

            {/* CỘT NỘI DUNG BÊN PHẢI */}
            <div className="text-col">
                <h3>Sức Mạnh Của Pin Lithium-Ion Thông Minh</h3>
                <p>Các gói pin của chúng tôi được thiết kế để tối ưu hóa hiệu suất và tuổi thọ. Với công nghệ làm mát tiên tiến và hệ thống quản lý pin (BMS) AI, chúng tôi đảm bảo mỗi lần trao đổi đều mang lại trải nghiệm tốt nhất.</p>
                
                <p>Chúng tôi cung cấp giải pháp pin thông minh toàn diện với chất lượng quốc tế, giá thành cạnh tranh và dịch vụ hậu mãi xuất sắc. Với nền tảng quản lý pin thông minh duy nhất đạt giải thưởng Sao khuê hạng 5 sao, EV Battery Swap mang đến một giải pháp độc đáo và tối ưu nhất thị trường.</p>
            </div>
            
        </div>
      </section>

      {/* 3. COMPATIBILITY SECTION: Mẫu Xe Tương Thích */}
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
            {/* Vẫn dùng <a>, cần chuyển sang <Link> sau */}
            <a href="/goshare" className="cta-button">Tìm hiểu về Chính sách của chúng tôi</a>
        </div>
      </section>

    </div>
  );
}