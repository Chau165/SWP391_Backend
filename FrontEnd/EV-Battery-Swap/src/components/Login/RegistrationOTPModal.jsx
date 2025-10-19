import React, { useState, useRef, useEffect } from 'react';
import './LoginModal.css';

/**
 * RegistrationOTPModal - Xác thực email khi đăng ký
 * Flow: Bước 1 (Nhập Email → Gửi OTP) → Bước 2 (Nhập OTP → Tạo tài khoản)
 */
const RegistrationOTPModal = ({ 
  isOpen, 
  onClose, 
  initialEmail,
  registrationData,
  onVerifySuccess 
}) => {
  const modalRef = useRef();
  const [step, setStep] = useState(1); // 1: Nhập Email, 2: Nhập OTP
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const API_BASE_URL = 'http://localhost:8080/webAPI3';

  // Reset khi đóng modal hoặc khi mở với email mới
  useEffect(() => {
    if (isOpen) {
      setStep(1);
      setEmail(initialEmail || '');
      setOtp('');
      setError('');
      setMessage('');
    }
  }, [isOpen, initialEmail]);

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

  // BƯỚC 1: Gửi OTP qua email
  const handleSendOTP = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');
    setIsLoading(true);

    try {
      const response = await fetch(`${API_BASE_URL}/api/send-registration-otp`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email })
      });

      const data = await response.json();

      if (response.ok) {
        setMessage('Mã OTP đã được gửi tới email của bạn!');
        setTimeout(() => {
          setStep(2); // Chuyển sang bước nhập OTP
          setMessage('');
        }, 1500);
      } else {
        setError(data.error || 'Không thể gửi OTP. Vui lòng thử lại.');
      }
    } catch (error) {
      console.error('Send OTP error:', error);
      setError('Không thể kết nối đến server. Vui lòng thử lại sau.');
    } finally {
      setIsLoading(false);
    }
  };

  // BƯỚC 2: Xác thực OTP và tạo tài khoản
  const handleVerifyOTP = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');

    if (!otp.trim()) {
      setError('Vui lòng nhập mã OTP');
      return;
    }

    setIsLoading(true);

    try {
      // Step 1: Verify OTP
      const verifyResponse = await fetch(`${API_BASE_URL}/api/verify-registration-otp`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, otp: otp.trim() })
      });

      const verifyData = await verifyResponse.json();

      if (!verifyResponse.ok) {
        setError(verifyData.error || 'Mã OTP không đúng. Vui lòng thử lại.');
        return;
      }

      // Step 2: OTP đúng, tạo tài khoản
      const registerResponse = await fetch(`${API_BASE_URL}/api/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(registrationData)
      });

      const registerData = await registerResponse.json();

      if (registerResponse.ok) {
        setMessage('Đăng ký thành công! Vui lòng đăng nhập.');
        setTimeout(() => {
          onVerifySuccess();
          onClose();
        }, 1500);
      } else {
        setError(registerData.error || 'Đăng ký thất bại. Vui lòng thử lại.');
      }

    } catch (error) {
      console.error('Verification error:', error);
      setError('Không thể kết nối đến server. Vui lòng thử lại sau.');
    } finally {
      setIsLoading(false);
    }
  };

  // RENDER
  return (
    <div className="modal-backdrop">
      <div className="login-modal" ref={modalRef}>
        <button className="close-btn" onClick={onClose}>
          &times;
        </button>

        {/* BƯỚC 1: Nhập Email để gửi OTP */}
        {step === 1 && (
          <>
            <h2 className="modal-title">Xác thực Email</h2>
            <p className="modal-subtitle">Nhập Email để nhận mã OTP</p>

            <form className="login-form" onSubmit={handleSendOTP}>
              <div className="form-group">
                <label htmlFor="reg-otp-email">Email</label>
                <input
                  type="email"
                  id="reg-otp-email"
                  placeholder="Nhập địa chỉ email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  disabled={isLoading}
                  autoFocus
                />
              </div>

              {error && <div className="error-message">{error}</div>}
              {message && <div className="success-message">{message}</div>}

              <button 
                type="submit" 
                className="login-button" 
                disabled={isLoading}
              >
                {isLoading ? 'Đang gửi...' : 'Gửi OTP'}
              </button>

              <button 
                type="button" 
                className="secondary-button" 
                onClick={onClose}
                disabled={isLoading}
              >
                Đóng
              </button>
            </form>
          </>
        )}

        {/* BƯỚC 2: Nhập OTP để xác thực */}
        {step === 2 && (
          <>
            <h2 className="modal-title">Vui lòng nhập mã OTP</h2>
            <p className="modal-subtitle">
              Mã OTP đã được gửi tới <strong>{email}</strong>
            </p>

            <form className="login-form" onSubmit={handleVerifyOTP}>
              <div className="form-group">
                <label htmlFor="reg-otp-code">OTP</label>
                <input
                  type="text"
                  id="reg-otp-code"
                  placeholder="Nhập mã OTP (6 chữ số)"
                  required
                  maxLength="6"
                  value={otp}
                  onChange={(e) => {
                    const value = e.target.value.replace(/\D/g, '');
                    setOtp(value);
                    setError('');
                  }}
                  disabled={isLoading}
                  autoFocus
                />
              </div>

              {error && <div className="error-message">{error}</div>}
              {message && <div className="success-message">{message}</div>}

              <button 
                type="submit" 
                className="login-button" 
                disabled={isLoading || otp.length !== 6}
              >
                {isLoading ? 'Đang xác thực...' : 'Xác thực OTP'}
              </button>

              <button 
                type="button" 
                className="secondary-button" 
                onClick={() => {
                  setStep(1);
                  setOtp('');
                  setError('');
                  setMessage('');
                }}
                disabled={isLoading}
              >
                Quay lại
              </button>
            </form>
          </>
        )}
      </div>
    </div>
  );
};

export default RegistrationOTPModal;
