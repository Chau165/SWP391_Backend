import React from 'react';
import './BatteryPin.css';

export default function BatteryPin() {
  return (
    <div className="battery-pin-page">
      {/* Main Container */}
      <div className="gogoro-container">
        {/* Left Side - Text Content */}
        <div className="text-content">
          <h1>Always quick. Always ready.</h1>
          <p>GoStation Sites make swapping batteries a breeze. Way cleaner than gas. Infinitely faster than charging. Full batteries are ready when you are. No waiting. No fumes. No fuss.</p>
        </div>

        {/* Right Side - Battery Station */}
        <div className="station-container">
          {/* Battery Grid */}
          <div className="battery-grid">
            {[...Array(18)].map((_, index) => (
              <div key={index} className="battery-slot">
                <div className="battery-inner"></div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}