import React, { useRef, useEffect, useState } from 'react';
import './LoginModal.css';

/**
 * Component LoginModal
 * @param {boolean} isOpen - Trạng thái hiển thị modal
 * @param {function} onClose - Hàm đóng modal
 */
export default function LoginModal({ isOpen, onClose }) {
  const [isRegister, setIsRegister] = useState(false);
  const modalRef = useRef();

  // Logic đóng modal khi click ra ngoài (backdrop)
  useEffect(() => {
    if (!isOpen) return;

    const handleClickOutside = (event) => {
      if (modalRef.current && !modalRef.current.contains(event.target)) {
        if (!isRegister) {
          onClose();
        }
        // If isRegister, do nothing (disable closing by outside click)
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isOpen, onClose, isRegister]);

  if (!isOpen) return null; // Không hiển thị gì nếu modal đóng


  const handleLoginSubmit = (e) => {
    e.preventDefault();
    // Logic đăng nhập sẽ ở đây
    alert('Đăng nhập thành công! (Mock)');
    onClose();
  };

  const handleRegisterSubmit = (e) => {
    e.preventDefault();
    // Logic đăng ký sẽ ở đây
    alert('Đăng ký thành công! (Mock)');
    onClose();
  };

  return (
    <div className="modal-backdrop">
      <div className="login-modal" ref={modalRef}>
        <button className="close-btn" onClick={onClose} aria-label="Đóng">
          &times;
        </button>
        {/* Login Form */}
        {!isRegister && (
          <>
            <h2 className="modal-title">Đăng nhập</h2>
            <p className="modal-subtitle">Chào mừng trở lại. Vui lòng nhập thông tin của bạn.</p>
            <form className="login-form" onSubmit={handleLoginSubmit}>
              <div className="form-group">
                <label htmlFor="email">Email</label>
                <input 
                  type="email" 
                  id="email" 
                  placeholder="Nhập email của bạn" 
                  required 
                />
              </div>
              <div className="form-group">
                <label htmlFor="password">Mật khẩu</label>
                <input 
                  type="password" 
                  id="password" 
                  placeholder="Nhập mật khẩu" 
                  required 
                />
              </div>
              <button type="submit" className="login-button">
                Đăng nhập
              </button>
              <div className="form-options">
                <a href="#" className="forgot-password">Quên mật khẩu?</a>
              </div>
            </form>
            <p className="signup-link">
              Chưa có tài khoản?{' '}
              <a href="#" onClick={e => { e.preventDefault(); setIsRegister(true); }}>Đăng ký ngay</a>
            </p>
          </>
        )}
        {/* Register Form */}
        {isRegister && (
          <>
            <h2 className="modal-title">Đăng ký</h2>
            <p className="modal-subtitle">Tạo tài khoản mới để sử dụng dịch vụ.</p>
            <form className="login-form" onSubmit={handleRegisterSubmit}>
              <div className="form-group">
                <label htmlFor="reg-name">Họ và tên</label>
                <input 
                  type="text" 
                  id="reg-name" 
                  placeholder="Nhập họ và tên" 
                  required 
                />
              </div>
              <div className="form-group">
                <label htmlFor="reg-email">Email</label>
                <input 
                  type="email" 
                  id="reg-email" 
                  placeholder="Nhập email" 
                  required 
                />
              </div>
              <div className="form-group">
                <label htmlFor="reg-password">Mật khẩu</label>
                <input 
                  type="password" 
                  id="reg-password" 
                  placeholder="Nhập mật khẩu" 
                  required 
                />
              </div>
              <div className="form-group">
                <label htmlFor="reg-confirm">Xác nhận mật khẩu</label>
                <input 
                  type="password" 
                  id="reg-confirm" 
                  placeholder="Nhập lại mật khẩu" 
                  required 
                />
              </div>
              <button type="submit" className="login-button">
                Đăng ký
              </button>
            </form>
            <p className="signup-link">
              Đã có tài khoản?{' '}
              <a href="#" onClick={e => { e.preventDefault(); setIsRegister(false); }}>Đăng nhập</a>
            </p>
          </>
        )}
      </div>
    </div>
  );
}