import { useEffect, useState } from "react";
import { Link } from 'react-router-dom';
import logo from "../../assets/react.svg";
import "./Header.css";

// CHỈNH SỬA: Nhận prop onLoginClick từ App.jsx
export default function Header({ onLoginClick }) { 
  const [scrolled, setScrolled] = useState(false);
  const [hovered, setHovered] = useState(false);

  // LOGIC CUỘN CHUỘT: Thêm class 'scrolled' khi cuộn quá 20px
  useEffect(() => {
    const onScroll = () => {
      if (window.scrollY > 20) {
        setScrolled(true);
      } else {
        setScrolled(false);
      }
    };
    window.addEventListener("scroll", onScroll);
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  return (
    <header
      className={`site-header ${scrolled ? "scrolled" : hovered ? "hovered" : ""}`}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
    >
      <div className="header-inner">
        <Link className="brand" to="/" aria-label="home">
          <img src={logo} alt="Logo" className="brand-logo" />
          <span className="brand-title">EV Battery Swap</span>
        </Link>

        <nav className="main-nav" aria-label="Primary">
          <Link to="/" className="nav-link">Home</Link>
          <Link to="/vehicles" className="nav-link">Battery Electric</Link>
          <Link to="/goshare" className="nav-link">Polices</Link>
          <Link to="/news" className="nav-link">News</Link>
          <Link to="/about" className="nav-link">About Us</Link>
        </nav>
        <div className="actions">
            <a 
                href="#" 
                className="cta login" 
                onClick={(e) => { 
                    e.preventDefault(); 
                    onLoginClick();
                }}
            >
                Login
            </a>
        </div>
      </div>
    </header>
  );
}