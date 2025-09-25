// src/components/Footer/Footer.jsx
import './Footer.css';

export default function Footer() {
  return (
    <footer className="footer">
      <div className="footer-container">

        {/* Cột logo và social */}
        <div className="footer-col">
          <h2 className="footer-logo">MyBrand</h2>
          <ul className="social-links">
            <li><a href="#">Facebook</a></li>
            <li><a href="#">Youtube</a></li>
            <li><a href="#">Twitter</a></li>
            <li><a href="#">Instagram</a></li>
            <li><a href="#">LinkedIn</a></li>
          </ul>
        </div>

        {/* Smart Energy */}
        <div className="footer-col">
          <h4>Smart Energy</h4>
          <ul>
            <li><a href="#">Swap & Go</a></li>
          </ul>
        </div>

        {/* Smart Vehicles */}
        <div className="footer-col">
          <h4>Smart Vehicles</h4>
          <ul>
            <li><a href="#">Gogoro Pulse</a></li>
            <li><a href="#">Gogoro Delight</a></li>
            <li><a href="#">Gogoro CrossOver</a></li>
          </ul>
        </div>

        {/* About Us */}
        <div className="footer-col">
          <h4>About Us</h4>
          <ul>
            <li><a href="#">Company</a></li>
            <li><a href="#">News</a></li>
            <li><a href="#">Media Center</a></li>
          </ul>
        </div>

        {/* Get Support */}
        <div className="footer-col">
          <h4>Get Support</h4>
          <ul>
            <li><a href="#">Tech Support</a></li>
            <li><a href="#">Contact Us</a></li>
            <li><a href="#">FAQ</a></li>
          </ul>
        </div>
      </div>

      <div className="footer-bottom">
        © 2025 MyBrand. All rights reserved.
      </div>
    </footer>
  );
}
