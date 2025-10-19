import React, { useState, useRef, useEffect } from 'react';
import './LoginModal.css';

/**
 * RegistrationOtpModal - Component xử lý xác thực OTP khi đăng ký
 * Flow: Nhập Email → Nhận OTP → Nhập OTP → Xác thực thành công → Cho phép đăng ký
 */
export default function RegistrationOtpModal({ 
  isOpen, 
  onClose, 
  onOtpVerified,
  registrationEmail 
}) {
  const modalRef = useRef();
  const [step, setStep] = useState(1); // 1: Email, 2: OTP
  const [email, setEmail] = useState(registrationEmail || '');
  const [otp, setOtp] = useState('');
  const [error, setError] = useState(null);
  const [message, setMessage] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  const API_BASE_URL = 'http://localhost:8080/webAPI3';

  // Cập nhật email từ form đăng ký
  useEffect(() => {
    if (registrationEmail) {
      setEmail(registrationEmail);
    }
  }, [registrationEmail]);

  // Reset khi đóng modal
  useEffect(() => {
    if (!isOpen) {
      setStep(1);
      setOtp('');
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
      const response = await fetch(`${API_BASE_URL}/api/send-registration-otp`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email })
      });

      const data = await response.json();

      if (data.status === 'success') {
        setMessage(data.message || 'Mã OTP đã được gửi đến email của bạn!');
        setTimeout(() => {
          setStep(2); // Chuyển sang bước nhập OTP
          setMessage(null);
        }, 1500);
      } else {
        setError(data.message || 'Không thể gửi mã OTP. Vui lòng thử lại.');
      }
    } catch (err) {
      setError('Không thể kết nối đến server. Vui lòng thử lại.');
      console.error('Send Registration OTP error:', err);
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
      const response = await fetch(`${API_BASE_URL}/api/verify-registration-otp`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, otp })
      });

      const data = await response.json();

      if (data.status === 'success') {
        setMessage('Xác thực thành công! Đang xử lý đăng ký...');
        setTimeout(() => {
          onOtpVerified(email, otp); // Callback với email và OTP đã verify
          onClose();
        }, 1000);
      } else {
        setError(data.message || 'Mã OTP không hợp lệ hoặc đã hết hạn.');
      }
    } catch (err) {
      setError('Không thể kết nối đến server. Vui lòng thử lại.');
      console.error('Verify Registration OTP error:', err);
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

        {/* STEP 1: Nhập Email để nhận OTP */}
        {step === 1 && (
          <>
            <h2 className="modal-title">📧 Xác thực Email</h2>
            <p className="modal-subtitle">Nhập email đã đăng ký để nhận mã OTP:</p>
            <form className="login-form" onSubmit={handleSendOtp}>
              <div className="form-group">
                <label htmlFor="registration-email">Email</label>
                <input
                  type="email"
                  id="registration-email"
                  placeholder="Nhập email của bạn"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  disabled={!!registrationEmail} // Nếu có email từ form đăng ký thì disable
                />
              </div>
              {error && <p className="error-message">{error}</p>}
              {message && <p className="success-message">{message}</p>}
              <button type="submit" className="login-button" disabled={isLoading}>
                {isLoading ? 'Đang gửi...' : 'Gửi mã OTP'}
              </button>
              <p className="signup-link">
                <a href="#" onClick={(e) => { e.preventDefault(); onClose(); }}>
                  Đóng
                </a>
              </p>
            </form>
          </>
        )}

        {/* STEP 2: Nhập OTP để xác thực */}
        {step === 2 && (
          <>
            <h2 className="modal-title">🔐 Xác thực OTP</h2>
            <p className="modal-subtitle">
              Nhập mã OTP đã được gửi đến <strong>{email}</strong>
            </p>
            <form className="login-form" onSubmit={handleVerifyOtp}>
              <div className="form-group">
                <label htmlFor="registration-otp">Mã OTP</label>
                <input
                  type="text"
                  id="registration-otp"
                  placeholder="Nhập 6 chữ số"
                  required
                  maxLength="6"
                  value={otp}
                  onChange={(e) => setOtp(e.target.value.replace(/\D/g, ''))}
                  autoComplete="off"
                />
              </div>
              {error && <p className="error-message">{error}</p>}
              {message && <p className="success-message">{message}</p>}
              <button type="submit" className="login-button" disabled={isLoading}>
                {isLoading ? 'Đang xác thực...' : 'Xác thực'}
              </button>
              <p className="signup-link">
                Không nhận được mã?{' '}
                <a 
                  href="#" 
                  onClick={(e) => { 
                    e.preventDefault(); 
                    setStep(1); 
                    setOtp(''); 
                    setError(null); 
                  }}
                >
                  Gửi lại
                </a>
              </p>
            </form>
          </>
        )}
      </div>
    </div>
  );
}
