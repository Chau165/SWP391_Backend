import React, { useState, useRef, useEffect } from 'react';
import './LoginModal.css';

/**
 * ForgotPasswordModal - Component xử lý quên mật khẩu
 * Flow: Nhập Email → Nhập OTP → Đổi mật khẩu mới → Quay về Login
 */
export default function ForgotPasswordModal({ isOpen, onClose, onSuccess }) {
  const modalRef = useRef();
  const [step, setStep] = useState(1); // 1: Email, 2: OTP, 3: New Password
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState(null);
  const [message, setMessage] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  const API_BASE_URL = 'http://localhost:8080/webAPI';

  // Reset khi đóng modal
  useEffect(() => {
    if (!isOpen) {
      setStep(1);
      setEmail('');
      setOtp('');
      setNewPassword('');
      setConfirmPassword('');
      setError(null);
      setMessage(null);
    }
  }, [isOpen]);

  // Đóng modal khi click backdrop
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

  // STEP 1: Gửi OTP qua email
  const handleSendOtp = async (e) => {
    e.preventDefault();
    setError(null);
    setMessage(null);
    setIsLoading(true);

    try {
      const response = await fetch(`${API_BASE_URL}/api/forgot-password`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email })
      });

      const data = await response.json();

      if (data.status === 'success') {
        setMessage(data.message);
        setTimeout(() => {
          setStep(2); // Chuyển sang bước nhập OTP
          setMessage(null);
        }, 1500);
      } else {
        setError(data.message);
      }
    } catch (err) {
      setError('Không thể kết nối đến server. Vui lòng thử lại.');
      console.error('Send OTP error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  // STEP 2: Xác thực OTP
  const handleVerifyOtp = async (e) => {
    e.preventDefault();
    setError(null);
    setMessage(null);
    setIsLoading(true);

    try {
      const response = await fetch(`${API_BASE_URL}/api/verify-otp`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, otp })
      });

      const data = await response.json();

      if (data.status === 'success') {
        setMessage('OTP hợp lệ! Vui lòng nhập mật khẩu mới.');
        setTimeout(() => {
          setStep(3); // Chuyển sang bước đổi mật khẩu
          setMessage(null);
        }, 1000);
      } else {
        setError(data.message);
      }
    } catch (err) {
      setError('Không thể kết nối đến server. Vui lòng thử lại.');
      console.error('Verify OTP error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  // STEP 3: Đổi mật khẩu mới
  const handleResetPassword = async (e) => {
    e.preventDefault();
    setError(null);
    setMessage(null);

    // Kiểm tra mật khẩu trùng khớp
    if (newPassword !== confirmPassword) {
      setError('Mật khẩu xác nhận không khớp');
      return;
    }

    if (newPassword.length < 6) {
      setError('Mật khẩu phải có ít nhất 6 ký tự');
      return;
    }

    setIsLoading(true);

    try {
      const response = await fetch(`${API_BASE_URL}/api/reset-password`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, otp, newPassword })
      });

      const data = await response.json();

      if (data.status === 'success') {
        setMessage('Đổi mật khẩu thành công! Đang chuyển về trang đăng nhập...');
        setTimeout(() => {
          onSuccess(); // Quay về trang login
          onClose();
        }, 2000);
      } else {
        setError(data.message);
      }
    } catch (err) {
      setError('Không thể kết nối đến server. Vui lòng thử lại.');
      console.error('Reset password error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="modal-backdrop">
      <div className="login-modal" ref={modalRef}>
        <button className="close-btn" onClick={onClose} aria-label="Đóng">
          &times;
        </button>

        {/* STEP 1: Nhập Email */}
        {step === 1 && (
          <>
            <h2 className="modal-title">🔐 Quên mật khẩu?</h2>
            <p className="modal-subtitle">Nhập email của bạn để nhận mã OTP</p>
            <form className="login-form" onSubmit={handleSendOtp}>
              <div className="form-group">
                <label htmlFor="forgot-email">Email</label>
                <input
                  type="email"
                  id="forgot-email"
                  placeholder="Nhập email đã đăng ký"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                />
              </div>
              {error && <p className="error-message">{error}</p>}
              {message && <p className="success-message">{message}</p>}
              <button type="submit" className="login-button" disabled={isLoading}>
                {isLoading ? 'Đang gửi...' : 'Gửi mã OTP'}
              </button>
              <p className="signup-link">
                Nhớ mật khẩu?{' '}
                <a href="#" onClick={(e) => { e.preventDefault(); onClose(); }}>
                  Đăng nhập
                </a>
              </p>
            </form>
          </>
        )}

        {/* STEP 2: Nhập OTP */}
        {step === 2 && (
          <>
            <h2 className="modal-title">✉️ Xác thực OTP</h2>
            <p className="modal-subtitle">
              Mã OTP đã được gửi đến <strong>{email}</strong>
            </p>
            <form className="login-form" onSubmit={handleVerifyOtp}>
              <div className="form-group">
                <label htmlFor="otp-input">Mã OTP (6 chữ số)</label>
                <input
                  type="text"
                  id="otp-input"
                  placeholder="Nhập mã OTP"
                  required
                  maxLength="6"
                  pattern="\d{6}"
                  value={otp}
                  onChange={(e) => setOtp(e.target.value.replace(/\D/g, ''))}
                  style={{ fontSize: '24px', textAlign: 'center', letterSpacing: '8px' }}
                />
              </div>
              {error && <p className="error-message">{error}</p>}
              {message && <p className="success-message">{message}</p>}
              <button type="submit" className="login-button" disabled={isLoading}>
                {isLoading ? 'Đang xác thực...' : 'Xác thực OTP'}
              </button>
              <p className="signup-link">
                Không nhận được mã?{' '}
                <a href="#" onClick={(e) => { e.preventDefault(); setStep(1); setOtp(''); }}>
                  Gửi lại
                </a>
              </p>
            </form>
          </>
        )}

        {/* STEP 3: Đổi mật khẩu mới */}
        {step === 3 && (
          <>
            <h2 className="modal-title">🔑 Đặt mật khẩu mới</h2>
            <p className="modal-subtitle">Nhập mật khẩu mới cho tài khoản của bạn</p>
            <form className="login-form" onSubmit={handleResetPassword}>
              <div className="form-group">
                <label htmlFor="new-password">Mật khẩu mới</label>
                <input
                  type="password"
                  id="new-password"
                  placeholder="Nhập mật khẩu mới"
                  required
                  minLength="6"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                />
              </div>
              <div className="form-group">
                <label htmlFor="confirm-password">Xác nhận mật khẩu</label>
                <input
                  type="password"
                  id="confirm-password"
                  placeholder="Nhập lại mật khẩu mới"
                  required
                  minLength="6"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                />
              </div>
              {error && <p className="error-message">{error}</p>}
              {message && <p className="success-message">{message}</p>}
              <button type="submit" className="login-button" disabled={isLoading}>
                {isLoading ? 'Đang cập nhật...' : 'Đổi mật khẩu'}
              </button>
            </form>
          </>
        )}
      </div>
    </div>
  );
}
