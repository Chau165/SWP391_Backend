import React from 'react';
import './BatteryPin.css'; // Đảm bảo import đúng tên file CSS

export default function BatteryPin() {
  return (
    <div className="battery-pin-page">
      {/* Hero Section với hình ảnh pin.jpg */}
      <section className="hero1">
        <img 
          src="/pin1.jpg" 
          alt="Battery Pin Technology" 
          className="hero-image"
        />
        <div className="hero1-content">
          <h1>Battery Pin Technology</h1>
        </div>
      </section>

    
    </div>
  );
}