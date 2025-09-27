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

      {/* DỰ ÁN PIN TIÊU BIỂU */}
      <section className="project-section">
        <h2>The most advanced energy platform for Electric Vehicle</h2>
        <h3>DỰ ÁN PIN TIÊU BIỂU</h3>

        <div className="project-cards">
          <div className="project">
            <img src="/img-bat.jpg" alt="Pin xe điện" />
            <div className="text">
              <h3>PIN CÔNG NGHỆ CAO DÀNH CHO XE ĐIỆN</h3>
              <p>
                Pin hoàn chỉnh với hệ thống an toàn, đáp ứng các tiêu chuẩn
                khắt khe. Ứng dụng cho xe máy điện, xe tải điện, xe buýt điện,
                với tuổi thọ cao và hiệu suất ổn định.
              </p>
            </div>
          </div>

          <div className="project">
            <img src="/img-project-energy.jpg" alt="Pin năng lượng" />
            <div className="text">
              <h3>PIN CHO ỨNG DỤNG NĂNG LƯỢNG</h3>
              <p>
                Giải pháp pin lưu trữ năng lượng, UPS, hệ thống điện mặt trời,
                đảm bảo độ ổn định và tuổi thọ cao, đáp ứng tiêu chuẩn quốc tế.
              </p>
            </div>
          </div>

          <div className="project">
            <img src="/img-project-custom.jpg" alt="Pin đối tác" />
            <div className="text">
              <h3>PIN THEO NHU CẦU ĐỐI TÁC</h3>
              <p>
                Pin được thiết kế riêng theo nhu cầu từng đối tác,
                từ dung lượng, kiểu dáng đến công nghệ cell, đảm bảo
                tính tối ưu cho từng ứng dụng.
              </p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}

export default Battery;
