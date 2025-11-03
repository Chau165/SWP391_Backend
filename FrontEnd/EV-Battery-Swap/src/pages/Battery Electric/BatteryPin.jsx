import React, { useState } from 'react';
import './BatteryPin.css';

export default function BatteryPin() {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedBatteryIndex, setSelectedBatteryIndex] = useState(null);

  const handleBatteryClick = (index) => {
    setSelectedBatteryIndex(index);
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setSelectedBatteryIndex(null);
  };

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
              <div 
                key={index} 
                className="battery-slot"
                onClick={() => handleBatteryClick(index)}
                style={{'--i': index}}
              >
                <div className="battery-inner"></div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Battery Modal */}
      {isModalOpen && (
        <div className="battery-modal-backdrop" onClick={closeModal}>
          <div className="battery-modal" onClick={(e) => e.stopPropagation()}>
            <button className="modal-close-btn" onClick={closeModal}>
              ×
            </button>
            
            <div className="modal-header">
              <h2>Gogoro Battery #{selectedBatteryIndex + 1}</h2>
              <span className="battery-status">Ready to Swap</span>
            </div>

            <div className="modal-content">
              {/* Battery Image */}
              <div className="battery-image-container">
                <img 
                  src="/ping.jpg" 
                  alt={`Gogoro Battery ${selectedBatteryIndex + 1}`}
                  className="battery-image"
                />
                <div className="battery-glow-effect"></div>
              </div>

              {/* Battery Info */}
              <div className="battery-info">
                <div className="info-row">
                  <span className="info-label">Capacity:</span>
                  <span className="info-value">100%</span>
                </div>
                <div className="info-row">
                  <span className="info-label">Voltage:</span>
                  <span className="info-value">50.4V</span>
                </div>
                <div className="info-row">
                  <span className="info-label">Temperature:</span>
                  <span className="info-value">28°C</span>
                </div>
                <div className="info-row">
                  <span className="info-label">Cycles:</span>
                  <span className="info-value">247</span>
                </div>
                <div className="info-row">
                  <span className="info-label">Health:</span>
                  <span className="info-value health-good">Excellent</span>
                </div>
              </div>
            </div>

            <div className="modal-actions">
              <button className="swap-button">
                <span className="button-icon">⚡</span>
                Swap Battery
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}