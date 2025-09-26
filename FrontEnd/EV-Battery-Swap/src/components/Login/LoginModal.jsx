import React, { useRef, useEffect } from 'react';
import './LoginModal.css';

/**
 * Component LoginModal
 * @param {boolean} isOpen - Trạng thái hiển thị modal
 * @param {function} onClose - Hàm đóng modal
 */
export default function LoginModal({ isOpen, onClose }) {
  const modalRef = useRef();

  // Logic đóng modal khi click ra ngoài (backdrop)
  useEffect(() => {
    if (!isOpen) return;

    const handleClickOutside = (event) => {
      if (modalRef.current && !modalRef.current.contains(event.target)) {
        onClose();
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isOpen, onClose]);

  if (!isOpen) return null; // Không hiển thị gì nếu modal đóng

  const handleSubmit = (e) => {
    e.preventDefault();
    // Logic đăng nhập sẽ ở đây
    alert('Đăng nhập thành công! (Mock)');
    onClose();
  };

  return (
    // Backdrop mờ toàn màn hình
    <div className="modal-backdrop">
      {/* Modal chính */}
      <div className="login-modal" ref={modalRef}>
        <button className="close-btn" onClick={onClose} aria-label="Đóng">
          &times;
        </button>
        
        <h2 className="modal-title">Đăng nhập</h2>
        <p className="modal-subtitle">Chào mừng trở lại. Vui lòng nhập thông tin của bạn.</p>

        <form className="login-form" onSubmit={handleSubmit}>
          
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

          <div className="form-options">
            <a href="#" className="forgot-password">Quên mật khẩu?</a>
          </div>

          <button type="submit" className="login-button">
            Đăng nhập
          </button>
        </form>
        
        <p className="signup-link">
            Chưa có tài khoản? <a href="#">Đăng ký ngay</a>
        </p>
      </div>
    </div>
  );
}