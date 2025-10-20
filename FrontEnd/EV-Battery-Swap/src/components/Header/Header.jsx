import { useEffect, useState } from "react";
import logo from "../../assets/react.svg";
import "./Header.css";
import "./ProfileButton.css";
import { Link, useLocation } from 'react-router-dom';
import { useUser } from '../../contexts/UserContext';
import UserProfile from '../UserProfile/UserProfile';

// CHỈNH SỬA: Nhận prop onLoginClick từ App.jsx
export default function Header({ onLoginClick }) { 
  const { user, isLoggedIn, logout } = useUser();
  const [scrolled, setScrolled] = useState(false);
  const [hovered, setHovered] = useState(false);
  const [showBatteryDropdown, setShowBatteryDropdown] = useState(false);
  const [showProfileModal, setShowProfileModal] = useState(false);
  
  const location = useLocation();

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

  // DEBUG: Log user state
  useEffect(() => {
    console.log('=== HEADER DEBUG ===');
    console.log('isLoggedIn:', isLoggedIn);
    console.log('user:', user);
    console.log('==================');
  }, [isLoggedIn, user]);

  const isActive = (path) => location.pathname === path;
  const isBatteryActive = () => location.pathname === '/vehicles' || location.pathname === '/battery-pin';

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
          <span className="brand-title">EV Battery Swapping</span>
        </a>

        <nav className="main-nav" aria-label="Primary">
          <Link 
            to="/" 
            className={`nav-link ${isActive('/') ? 'active' : ''}`}
          >
            Home
          </Link>

          {/* Battery Electric với dropdown */}
          <div 
            className="nav-dropdown"
            onMouseEnter={() => setShowBatteryDropdown(true)}
            onMouseLeave={() => setShowBatteryDropdown(false)}
          >
            <span className={`nav-link dropdown-trigger ${isBatteryActive() ? 'active' : ''}`}>
              Battery Electric
              <svg className="dropdown-arrow" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" />
              </svg>
            </span>
            
            {showBatteryDropdown && (
              <div className="dropdown-menu">
                <Link 
                  to="/battery" 
                  className={`dropdown-item ${isActive('/battery') ? 'active' : ''}`}
                >
                  Battery Swap
                </Link>
                <Link 
                  to="/battery-pin" 
                  className={`dropdown-item ${isActive('/battery-pin') ? 'active' : ''}`}
                >
                  Battery Pin
                </Link>
              </div>
            )}
          </div>

          <Link to="/goshare" className="nav-link">Polices</Link>
          <Link to="/news" className="nav-link">News</Link>
          <Link 
            to="/about" 
            className={`nav-link ${isActive('/about') ? 'active' : ''}`}
          >
            About Us
          </Link>
        </nav>
        
        {/* User Actions */}
        <div className="actions">
          {isLoggedIn ? (
            <div style={{ 
              display: 'flex', 
              alignItems: 'center', 
              gap: '10px',
              flexWrap: 'wrap'
            }}>
              {/* User Info */}
              <span style={{ 
                fontSize: '13px', 
                color: '#fff', 
                fontWeight: '500',
                backgroundColor: 'rgba(255,255,255,0.1)',
                padding: '6px 12px',
                borderRadius: '15px',
                whiteSpace: 'nowrap'
              }}>
                {user?.fullName || user?.email} ({user?.role})
              </span>
              
              {/* Nút Xem Profile */}
              <button
                className="cta view-profile" 
                onClick={(e) => { 
                  e.preventDefault(); 
                  console.log('Profile button clicked!');
                  setShowProfileModal(true);
                }}
                style={{ 
                  backgroundColor: '#28a745',
                  border: 'none',
                  cursor: 'pointer',
                  padding: '10px 20px',
                  borderRadius: '25px',
                  color: '#fff',
                  fontWeight: '600',
                  fontSize: '14px',
                  transition: 'all 0.3s ease'
                }}
              >
                👤 Xem Profile
              </button>
              
              {/* Nút Logout */}
              <button
                className="cta logout" 
                onClick={(e) => { 
                  e.preventDefault(); 
                  logout();
                }}
                style={{ 
                  backgroundColor: '#dc3545',
                  border: 'none',
                  cursor: 'pointer',
                  padding: '10px 20px',
                  borderRadius: '25px',
                  color: '#fff',
                  fontWeight: '600',
                  fontSize: '14px',
                  transition: 'all 0.3s ease'
                }}
              >
                🚪 Logout
              </button>
            </div>
          ) : (
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
          )}
        </div>
      </div>

      {/* Profile Modal */}
      {showProfileModal && (
        <UserProfile onClose={() => setShowProfileModal(false)} />
      )}
    </header>
  );
}