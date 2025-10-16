import React, { useRef, useEffect, useState } from 'react';
import { useUser } from '../../contexts/UserContext';
import './LoginModal.css';


/**
 * Component LoginModal
 * @param {boolean} isOpen - Trạng thái hiển thị modal
 * @param {function} onClose - Hàm đóng modal
 */
export default function LoginModal({ isOpen, onClose }) {
  const { login, isLoading } = useUser();
  
  // Add register form toggle and state
  const [isRegister, setIsRegister] = useState(false);
  const [registerData, setRegisterData] = useState({
    name: '',
    email: '',
    password: '',
    confirm: ''
  });
  const handleRegisterChange = (e) => {
    setRegisterData({ ...registerData, [e.target.id.replace('reg-', '')]: e.target.value });
  };
  const handleRegisterSubmit = (e) => {
    e.preventDefault();
    // TODO: Add register API logic here
    alert('Đăng ký thành công!');
    setIsRegister(false);
  };
  const modalRef = useRef();

  //State để lưu trữ Email và Mật khẩu
  const [formData, setFormData] = useState({
    email: '',
    password: '',
  });

  //State để quản lý lỗi
  const [error, setError] = useState(null);
  
  // Hàm xử lý thay đổi input
  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.id]: e.target.value });
  };

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

  if (!isOpen) return null;

  // Hàm gọi API Đăng nhập
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null); // Xóa lỗi cũ

    const result = await login(formData.email, formData.password);
    
    if (result.success) {
      // Đăng nhập thành công
      onClose(); // Đóng modal
      // Reset form
      setFormData({ email: '', password: '' });
    } else {
      // Hiển thị lỗi
      setError(result.message);
    }
  };


  return (
    <div className="modal-backdrop">
      <div className="login-modal" ref={modalRef}>
        <button className="close-btn" onClick={onClose} aria-label="Đóng">
          &times;
        </button>
        {!isRegister && (
          <>
            <h2 className="modal-title">Đăng nhập</h2>
            <p className="modal-subtitle">Chào mừng trở lại. Vui lòng nhập thông tin của bạn.</p>
            <form className="login-form" onSubmit={handleSubmit}>
              {/* ...existing login form code... */}
              <div className="form-group">
                <label htmlFor="email">Email</label>
                <input 
                  type="email" 
                  id="email" 
                  placeholder="Nhập email của bạn" 
                  required 
                  value={formData.email}
                  onChange={handleChange}
                />
              </div>
              <div className="form-group">
                <label htmlFor="password">Mật khẩu</label>
                <input 
                  type="password" 
                  id="password" 
                  placeholder="Nhập mật khẩu" 
                  required 
                  value={formData.password}
                  onChange={handleChange}
                />
              </div>
              {error && <p className="error-message">{error}</p>}
              <button type="submit" className="login-button" disabled={isLoading}>
                {isLoading ? 'Đang đăng nhập...' : 'Đăng nhập'}
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
                  value={registerData.name}
                  onChange={handleRegisterChange}
                />
              </div>
              <div className="form-group">
                <label htmlFor="reg-email">Email</label>
                <input 
                  type="email" 
                  id="reg-email" 
                  placeholder="Nhập email" 
                  required 
                  value={registerData.email}
                  onChange={handleRegisterChange}
                />
              </div>
              <div className="form-group">
                <label htmlFor="reg-password">Mật khẩu</label>
                <input 
                  type="password" 
                  id="reg-password" 
                  placeholder="Nhập mật khẩu" 
                  required 
                  value={registerData.password}
                  onChange={handleRegisterChange}
                />
              </div>
              <div className="form-group">
                <label htmlFor="reg-confirm">Xác nhận mật khẩu</label>
                <input 
                  type="password" 
                  id="reg-confirm" 
                  placeholder="Nhập lại mật khẩu" 
                  required 
                  value={registerData.confirm}
                  onChange={handleRegisterChange}
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