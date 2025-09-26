import { useEffect, useState } from "react";
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
      // Class sẽ là 'scrolled' nếu cuộn, hoặc 'hovered' nếu di chuột
      className={`site-header ${scrolled ? "scrolled" : hovered ? "hovered" : ""}`}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
    >
      <div className="header-inner">
        <a className="brand" href="/" aria-label="home">
          <img src={logo} alt="Logo" className="brand-logo" />
        </a>

        <nav className="main-nav" aria-label="Primary">
          <a href="/" className="nav-link">Smart Energy</a>
          <a href="/vehicles" className="nav-link">Smart Vehicles</a>
          <a href="/goshare" className="nav-link">GoShare</a>
          <a href="/news" className="nav-link">News</a>
          <a href="/about" className="nav-link">About Us</a>
        </nav>
        
        {/* BỔ SUNG: Phần tử chứa nút Login (đã được định kiểu trong CSS) */}
        <div className="actions">
            <a 
                href="#" 
                className="cta login" 
                onClick={(e) => { 
                    e.preventDefault(); 
                    onLoginClick(); // Gọi hàm mở Modal
                }}
            >
                Login
            </a>
        </div>
        {/* KẾT THÚC BỔ SUNG */}
      </div>
    </header>
  );
}