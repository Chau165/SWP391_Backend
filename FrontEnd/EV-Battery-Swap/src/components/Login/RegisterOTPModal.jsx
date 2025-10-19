import React, { useState, useEffect } from 'react';
import './LoginModal.css';

const RegisterOTPModal = ({ isOpen, onClose, email, registrationData, onVerifySuccess }) => {
  const [otp, setOtp] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [countdown, setCountdown] = useState(0);
  const [otpSent, setOtpSent] = useState(false);

  // Gửi OTP khi modal mở
  useEffect(() => {
    if (isOpen && email && !otpSent) {
      sendOTP();
    }
  }, [isOpen, email]);

  // Countdown timer
  useEffect(() => {
    if (countdown > 0) {
      const timer = setTimeout(() => setCountdown(countdown - 1), 1000);
      return () => clearTimeout(timer);
    }
  }, [countdown]);

  const sendOTP = async () => {
    setLoading(true);
    setError('');
    
    try {
      const response = await fetch('http://localhost:8080/webAPI3/api/send-registration-otp', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email })
      });

      const data = await response.json();

      if (response.ok) {
        setOtpSent(true);
        setCountdown(60); // 60 giây countdown
        alert('Mã OTP đã được gửi đến email của bạn!');
      } else {
        setError(data.error || 'Không thể gửi OTP. Vui lòng thử lại.');
      }
    } catch (error) {
      console.error('Send OTP error:', error);
      setError('Không thể kết nối đến server. Vui lòng thử lại sau.');
    } finally {
      setLoading(false);
    }
  };

  const handleResendOTP = () => {
    if (countdown === 0) {
      setOtp('');
      setOtpSent(false);
      sendOTP();
    }
  };

  const handleVerifyOTP = async (e) => {
    e.preventDefault();
    
    if (!otp || otp.length !== 6) {
      setError('Vui lòng nhập mã OTP gồm 6 chữ số');
      return;
    }

    setLoading(true);
    setError('');

    try {
      // Bước 1: Xác thực OTP
      const verifyResponse = await fetch('http://localhost:8080/webAPI3/api/verify-registration-otp', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, otp })
      });

      const verifyData = await verifyResponse.json();

      if (verifyResponse.ok) {
        // Bước 2: OTP đúng, giờ tạo tài khoản
        const registerResponse = await fetch('http://localhost:8080/webAPI3/api/register', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(registrationData)
        });

        const registerData = await registerResponse.json();

        if (registerResponse.ok) {
          alert('Đăng ký thành công! Vui lòng đăng nhập.');
          onVerifySuccess();
          onClose();
        } else {
          setError(registerData.error || 'Đăng ký thất bại. Vui lòng thử lại.');
        }
      } else {
        setError(verifyData.error || 'Mã OTP không chính xác. Vui lòng thử lại.');
      }
    } catch (error) {
      console.error('Verify OTP error:', error);
      setError('Không thể kết nối đến server. Vui lòng thử lại sau.');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-content" style={{ maxWidth: '450px' }}>
        <button className="close-button" onClick={onClose} disabled={loading}>
          ×
        </button>
        
        <h2 className="modal-title">Xác thực Email</h2>
        <p className="modal-subtitle">
          Mã OTP đã được gửi đến email: <strong>{email}</strong>
        </p>

        <form onSubmit={handleVerifyOTP} className="login-form">
          <div className="form-group">
            <label htmlFor="otp">Nhập mã OTP (6 chữ số)</label>
            <input
              type="text"
              id="otp"
              placeholder="Nhập mã OTP"
              maxLength="6"
              pattern="[0-9]{6}"
              required
              value={otp}
              onChange={(e) => {
                setOtp(e.target.value);
                setError('');
              }}
              disabled={loading}
              style={{
                fontSize: '20px',
                textAlign: 'center',
                letterSpacing: '8px'
              }}
            />
          </div>

          {error && (
            <div style={{
              color: '#d32f2f',
              backgroundColor: '#ffebee',
              padding: '10px',
              borderRadius: '5px',
              marginBottom: '15px',
              fontSize: '14px'
            }}>
              {error}
            </div>
          )}

          <button 
            type="submit" 
            className="login-button" 
            disabled={loading || !otpSent}
          >
            {loading ? 'Đang xác thực...' : 'Xác thực OTP'}
          </button>

          <div style={{ 
            marginTop: '15px', 
            textAlign: 'center',
            fontSize: '14px'
          }}>
            {countdown > 0 ? (
              <p style={{ color: '#666' }}>
                Gửi lại mã sau {countdown} giây
              </p>
            ) : (
              <button
                type="button"
                onClick={handleResendOTP}
                disabled={loading}
                style={{
                  background: 'none',
                  border: 'none',
                  color: '#1976d2',
                  cursor: 'pointer',
                  textDecoration: 'underline',
                  fontSize: '14px'
                }}
              >
                Gửi lại mã OTP
              </button>
            )}
          </div>
        </form>
      </div>
    </div>
  );
};

export default RegisterOTPModal;
