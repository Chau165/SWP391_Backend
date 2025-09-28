import React from "react";
import "./Battery.css";

function Battery() {
  return (
    <div className="battery-page">
      {/* HERO SECTION */}
      <div className="hero">
        <img
          src="/img-battery.jpg"
          alt="Battery Swap"
          className="hero-image"
        />
        <div className="hero-content">
          <h1>EV Battery Solutions</h1>
        </div>
      </div>

      {/* GIỚI THIỆU VỀ GIẢI PHÁP */}
      <section className="intro-section">
        <h2>ABOUT THE SOLUTION</h2>
        <div className="intro-cards">
          <div className="card">
            <img src="/img-battery3.jpg" alt="Pin cho xe điện" />
            <h3> ELECTRIC MOBILITY SOLUTION</h3>
            <p>
            This represents an advanced mobility model that completely eliminates the need for traditional battery charging.
            </p>
          </div>
          <div className="card">
            <img src="/img-battery4.jpg" alt="Pin năng lượng" />
            <h3>ENERGY STORAGE AND SWAPPING SYSTEM</h3>
            <p>
                 SwappingStation—an automated energy storage cabinet that functions as an energy vending machine.
            </p>
          </div>
          <div className="card">
            <img src="/img-battery2.jpg" alt="Pin đối tác" />
            <h3>DESIGN AND BATTERY TECHNOLOGY DETAILS</h3>
            <p>
                The close-up image of the batteries (or battery slots) highlights the standardized, sleek, and durable product design.
            </p>
          </div>
        </div>
      </section>


    {/* PROJECT SECTION - BỐ CỤC ẢNH 50% - TEXT 50% */}
    <section className="project-section-single">
      <h2>THE MOST ADVANCED ENERGY PLATFORM FOR ELECTRIC VEHICLE</h2>

      <div className="project-card-gogoro">

        {/* THẺ DỰ ÁN 1: ẢNH TRÁI - TEXT PHẢI (Giống Gogoro Smart Batteries) */}
        <div className="card-item">
          <div className="card-image-wrapper">
            <img src="img-network-feature-1.jpg" alt="Smart Battery Swapping" /> 
          </div>
          <div className="card-text-content">
            <h4>SMART BATTERIES</h4>
            <h3>Refuel in seconds.</h3> 
            <p>The wait is over. Refuel in seconds. The fastest, cleanest way to power your ride. Swaps in seconds. Compact and easy to use. Just swap and go and you're back on the road without skipping a beat.</p>
          </div>
        </div>

        {/* THẺ DỰ ÁN 2: ẢNH TRÁI - TEXT PHẢI */}
        <div className="card-item">
          <div className="card-image-wrapper">
            <img src="/img-network-feature-2.jpg" alt="Energy Storage Application" /> 
          </div>
          <div className="card-text-content">
            <h4>PIN CHO ỨNG DỤNG NĂNG LƯỢNG</h4>
            <h3>Powering Tomorrow.</h3>
            <p>Giải pháp pin lưu trữ năng lượng, UPS, hệ thống điện mặt trời, đảm bảo độ ổn định và tuổi thọ cao, đáp ứng tiêu chuẩn quốc tế. Tối ưu hóa hiệu suất và giảm chi phí vận hành.</p>
          </div>
        </div>

        {/* THẺ DỰ ÁN 3: ẢNH TRÁI - TEXT PHẢI */}
        <div className="card-item">
          <div className="card-image-wrapper">
            <img src="/img-network-feature-3.jpg" alt="Design and Technology" /> 
          </div>
          <div className="card-text-content">
            <h4>THIẾT KẾ VÀ CÔNG NGHỆ PIN</h4>
            <h3>Standardized and Durable.</h3>
            <p>Pin được thiết kế riêng theo nhu cầu từng đối tác, sử dụng dung lượng, kiểu dáng đến công nghệ cell, đảm bảo tính tối ưu cho từng ứng dụng và đáp ứng các tiêu chuẩn khắt khe về an toàn.</p>
          </div>
        </div>

      </div>
    </section>

    </div>
  );
}

export default Battery;