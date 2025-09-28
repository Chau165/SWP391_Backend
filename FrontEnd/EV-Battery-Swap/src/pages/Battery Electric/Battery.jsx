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
              <h4>CONVENIENT EV B.SWAPSTATION SITES</h4>
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
              <h4>EV BATTERY SWAPPING APP</h4>
              <h3>Power with a tap, payments in a snap.</h3>
              <p>Connect to the entire Gogoro Network in real time. Ready for a swap? Find fresh batteries near you. Planning a trip? Map GoStation Sites on your route. Everything is here. Riding plans. Cash-free billing. Riding stats. Swapping tips. Network updates. 24/7 service. Gas never had an app.</p>
            </div>
          </div>
        </div>
      </section>

      {/* TẠI SAO CHỌN GIẢI PHÁP CỦA CHÚNG TÔI */}
      <section className="intro-section">
        <h2>WHY CHOOSE OUR SOLUTION?</h2>
        <div className="intro-cards-sumary">
          <div className="card-sumary">
            <img src="/img-batte.jpg" alt="Pin cho xe điện" />
          </div>
          {/*Setting chữ*/}
          <div className="sumary-text-chung">
            <div className="sumary-text-1">
              <h3 class="text">CONVENIENCE</h3>
              <p>
                GoStation Sites use less space than a parking spot and are quick and easy to install in a wide range of sites. A single location can serve hundreds of swaps a day without waiting.
              </p>
            </div>
          
          <div className="sumary-text-1">
            <div className="sumary-text-1">
              <h3 class="text">Easy to Operate</h3>
              <p>
                Weather proof. Tamper proof. Low maintenance. 24hr monitoring, remote updates and automatic safety measures deliver 99% uptime.
              </p>
            </div>
</div>
          
            <div className="sumary-text-1">
            <h3 class="text">Built to Last</h3>
            <p>
              Rugged design, future-proof technology and continual updates. Engineered for reliability in the most demanding urban environments. Swap after swap, year after year.
            </p>
          </div>
        
        
        </div>
        </div>
      </section>
    </div>
  );
}

export default Battery;