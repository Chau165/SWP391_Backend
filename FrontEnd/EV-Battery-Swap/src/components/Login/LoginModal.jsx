import React, { useRef, useEffect, useState } from 'react';
import { useUser } from '../../contexts/UserContext';
import ForgotPasswordModal from './ForgotPasswordModal';
import RegistrationOtpModal from './RegistrationOtpModal';
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
  
  // Add forgot password modal state
  const [isForgotPasswordOpen, setIsForgotPasswordOpen] = useState(false);
  
  // Add registration OTP modal state
  const [isRegistrationOtpOpen, setIsRegistrationOtpOpen] = useState(false);
  const [verifiedEmail, setVerifiedEmail] = useState('');
  const [verifiedOtp, setVerifiedOtp] = useState('');
  
  const [registerData, setRegisterData] = useState({
    fullName: '',
    phone: '',
    email: '',
    password: '',
    confirm: ''
  });
  
  const [registerError, setRegisterError] = useState('');
  const [registerLoading, setRegisterLoading] = useState(false);
  
  const handleRegisterChange = (e) => {
    setRegisterData({ ...registerData, [e.target.id.replace('reg-', '')]: e.target.value });
    setRegisterError(''); // Clear error when user types
  };
  
  const handleRegisterSubmit = async (e) => {
    e.preventDefault();
    setRegisterError('');
    
    // Validate passwords match
    if (registerData.password !== registerData.confirm) {
      setRegisterError('Mật khẩu xác nhận không khớp!');
      return;
    }
    
    // Validate password length
    if (registerData.password.length < 6) {
      setRegisterError('Mật khẩu phải có ít nhất 6 ký tự!');
      return;
    }
    
    // Open OTP modal instead of calling register API directly
    setIsRegistrationOtpOpen(true);
  };
  
  // Handle OTP verification success - then call register API
  const handleOtpVerified = async (email, otp) => {
    setVerifiedEmail(email);
    setVerifiedOtp(otp);
    setRegisterLoading(true);
    
    try {
      const response = await fetch('http://localhost:8080/webAPI3/api/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          fullName: registerData.fullName,
          phone: registerData.phone,
          email: registerData.email,
          password: registerData.password,
          otp: otp // Include verified OTP
        })
      });
      
      const data = await response.json();
      
      if (response.ok) {
        alert('Đăng ký thành công! Vui lòng đăng nhập.');
        setIsRegister(false);
        // Reset form
        setRegisterData({
          fullName: '',
          phone: '',
          email: '',
          password: '',
          confirm: ''
        });
        setVerifiedEmail('');
        setVerifiedOtp('');
      } else {
        setRegisterError(data.error || 'Đăng ký thất bại. Vui lòng thử lại.');
      }
    } catch (error) {
      console.error('Registration error:', error);
      setRegisterError('Không thể kết nối đến server. Vui lòng thử lại sau.');
    } finally {
      setRegisterLoading(false);
    }
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
    <>
      {/* Login Modal */}
      {!isForgotPasswordOpen && (
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
                <a 
                  href="#" 
                  className="forgot-password"
                  onClick={(e) => {
                    e.preventDefault();
                    setIsForgotPasswordOpen(true); // Mở modal quên mật khẩu
                  }}
                >
                  Quên mật khẩu?
                </a>
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
                <label htmlFor="reg-fullName">Họ và tên</label>
                <input 
                  type="text" 
                  id="reg-fullName" 
                  placeholder="Nhập họ và tên" 
                  required 
                  value={registerData.fullName}
                  onChange={handleRegisterChange}
                  disabled={registerLoading}
                />
              </div>
              <div className="form-group">
                <label htmlFor="reg-phone">Số điện thoại</label>
                <input 
                  type="tel" 
                  id="reg-phone" 
                  placeholder="Nhập số điện thoại (VD: 0909123456)" 
                  required 
                  pattern="0[0-9]{9}"
                  value={registerData.phone}
                  onChange={handleRegisterChange}
                  disabled={registerLoading}
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
                  disabled={registerLoading}
                />
              </div>
              <div className="form-group">
                <label htmlFor="reg-password">Mật khẩu</label>
                <input 
                  type="password" 
                  id="reg-password" 
                  placeholder="Nhập mật khẩu (tối thiểu 6 ký tự)" 
                  required 
                  minLength="6"
                  value={registerData.password}
                  onChange={handleRegisterChange}
                  disabled={registerLoading}
                />
              </div>
              <div className="form-group">
                <label htmlFor="reg-confirm">Xác nhận mật khẩu</label>
                <input 
                  type="password" 
                  id="reg-confirm" 
                  placeholder="Nhập lại mật khẩu" 
                  required 
                  minLength="6"
                  value={registerData.confirm}
                  onChange={handleRegisterChange}
                  disabled={registerLoading}
                />
              </div>
              
              {registerError && (
                <div className="error-message" style={{
                  color: '#d32f2f',
                  backgroundColor: '#ffebee',
                  padding: '10px',
                  borderRadius: '5px',
                  marginBottom: '15px',
                  fontSize: '14px'
                }}>
                  {registerError}
                </div>
              )}
              
              <button type="submit" className="login-button" disabled={registerLoading}>
                {registerLoading ? 'Đang xử lý...' : 'Đăng ký'}
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
      )}
      
      {/* Forgot Password Modal */}
      <ForgotPasswordModal
        isOpen={isForgotPasswordOpen}
        onClose={() => {
          setIsForgotPasswordOpen(false);
          // Không cần mở lại LoginModal vì isOpen được quản lý từ parent
        }}
        onSuccess={() => {
          setIsForgotPasswordOpen(false);
          // Quay về LoginModal để user đăng nhập
        }}
      />
      
      {/* Registration OTP Modal */}
      <RegistrationOtpModal
        isOpen={isRegistrationOtpOpen}
        onClose={() => setIsRegistrationOtpOpen(false)}
        onOtpVerified={handleOtpVerified}
        registrationEmail={registerData.email}
      />
    </>
  );
}