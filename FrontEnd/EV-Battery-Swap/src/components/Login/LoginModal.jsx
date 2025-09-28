import React, { useRef, useEffect, useState } from 'react';
import './LoginModal.css';


/**
 * Component LoginModal
 * @param {boolean} isOpen - Trạng thái hiển thị modal
 * @param {function} onClose - Hàm đóng modal
 */
export default function LoginModal({ isOpen, onClose }) {
  const modalRef = useRef();

  //State để lưu trữ Email và Mật khẩu
  const [formData, setFormData] = useState({
    email: '',
    password: '',
  });

  //State để quản lý lỗi và trạng thái tải
  const [error, setError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

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

  // BỔ SUNG 3: Hàm gọi API Đăng nhập
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null); // Xóa lỗi cũ
    setIsLoading(true); // Bắt đầu tải

    try {
      // ----------------------------------------------------
      // THAY ĐỔI ĐƯỜNG DẪN API VÀ CÁC HEADER CỦA BẠN TẠI ĐÂY
      // ----------------------------------------------------
      const response = await fetch('https://03dafbc27102.ngrok-free.app/webAPI/api/login', {
        method: 'POST',
        headers: {
          "Content-Type": "application/json"
        },
        // THAY THẾ DỮ LIỆU CỨNG BẰNG BIẾN formData
        body: JSON.stringify(formData)
      });

      const data = await response.json();
      console.log(data);
      if (response.ok) {
        // Xử lý thành công: Lưu token, chuyển hướng người dùng
        alert('Đăng nhập thành công! Token: ' + data.token);
        // Ví dụ: localStorage.setItem('token', data.token);
        onClose(); // Đóng modal
      } else {
        // Xử lý thất bại: Hiển thị thông báo lỗi từ API
        setError(data.message || 'Email hoặc mật khẩu không đúng.');
      }
    } catch (err) {
      // Xử lý lỗi kết nối mạng
      setError('Lỗi kết nối mạng. Vui lòng thử lại.');
    } finally {
      setIsLoading(false); // Kết thúc tải
    }
  };


  return (
    <div className="modal-backdrop">
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
              value={formData.email} // Gắn giá trị state
              onChange={handleChange} // Xử lý thay đổi
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Mật khẩu</label>
            <input
              type="password"
              id="password"
              placeholder="Nhập mật khẩu"
              required
              value={formData.password} // Gắn giá trị state
              onChange={handleChange} // Xử lý thay đổi
            />
          </div>

          {/* BỔ SUNG 4: Hiển thị lỗi */}
          {error && <p className="error-message">{error}</p>}

          <div className="form-options">
            <a href="#" className="forgot-password">Quên mật khẩu?</a>
          </div>

          <button type="submit" className="login-button" disabled={isLoading}>
            {isLoading ? 'Đang đăng nhập...' : 'Đăng nhập'}
          </button>
        </form>

        <p className="signup-link">
          Chưa có tài khoản? <a href="#">Đăng ký ngay</a>
        </p>
      </div>
    </div>
  );
}