import { useState, useEffect } from 'react';
import { useUser } from '../../contexts/UserContext';
import './UserProfile.css';

/**
 * Component hiển thị và chỉnh sửa thông tin profile của user
 * Dành cho tất cả các role: Driver, Admin, Staff, User
 */
export default function UserProfile({ onClose }) {
  const { user } = useUser();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    fullName: '',
    phone: '',
    avatarUrl: ''
  });

  // Load profile khi component mount
  useEffect(() => {
    if (user && user.id) {
      loadProfile();
    }
  }, [user]);

  /**
   * Load profile từ API
   */
  const loadProfile = async () => {
    try {
      setLoading(true);
      setError(null);

      const response = await fetch(`http://localhost:8080/webAPI/api/profile?userId=${user.id}`);
      const data = await response.json();

      if (data.success && data.data) {
        setProfile(data.data);
        setFormData({
          fullName: data.data.fullName || '',
          phone: data.data.phone || '',
          avatarUrl: data.data.avatarUrl || ''
        });
      } else {
        setError(data.message || 'Không thể tải thông tin profile');
      }
    } catch (err) {
      console.error('Error loading profile:', err);
      setError('Lỗi kết nối đến server');
    } finally {
      setLoading(false);
    }
  };

  /**
   * Xử lý thay đổi input
   */
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  /**
   * Lưu thông tin profile
   */
  const handleSave = async () => {
    try {
      setLoading(true);
      setError(null);

      const response = await fetch('http://localhost:8080/webAPI/api/profile/update', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          userId: user.id,
          fullName: formData.fullName,
          phone: formData.phone,
          avatarUrl: formData.avatarUrl
        })
      });

      const data = await response.json();

      if (data.success) {
        alert('Cập nhật profile thành công!');
        setIsEditing(false);
        loadProfile(); // Reload profile
      } else {
        setError(data.message || 'Không thể cập nhật profile');
      }
    } catch (err) {
      console.error('Error updating profile:', err);
      setError('Lỗi kết nối đến server');
    } finally {
      setLoading(false);
    }
  };

  /**
   * Format date
   */
  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('vi-VN');
  };

  if (loading && !profile) {
    return (
      <div className="profile-modal-overlay" onClick={onClose}>
        <div className="profile-modal" onClick={(e) => e.stopPropagation()}>
          <div className="loading">Đang tải...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="profile-modal-overlay" onClick={onClose}>
      <div className="profile-modal" onClick={(e) => e.stopPropagation()}>
        <div className="profile-header">
          <h2>Thông Tin Profile</h2>
          <button className="close-btn" onClick={onClose}>×</button>
        </div>

        {error && (
          <div className="error-message">{error}</div>
        )}

        <div className="profile-content">
          {/* Avatar */}
          <div className="profile-avatar-section">
            <div className="profile-avatar">
              {profile?.avatarUrl ? (
                <img src={profile.avatarUrl} alt="Avatar" />
              ) : (
                <div className="avatar-placeholder">
                  {profile?.fullName?.charAt(0).toUpperCase() || user?.email?.charAt(0).toUpperCase()}
                </div>
              )}
            </div>
            {isEditing && (
              <div className="form-group">
                <label>URL Avatar:</label>
                <input
                  type="text"
                  name="avatarUrl"
                  value={formData.avatarUrl}
                  onChange={handleInputChange}
                  placeholder="Nhập URL ảnh avatar"
                />
              </div>
            )}
          </div>

          {/* Thông tin cơ bản */}
          <div className="profile-info">
            <div className="info-group">
              <label>Họ và Tên:</label>
              {isEditing ? (
                <input
                  type="text"
                  name="fullName"
                  value={formData.fullName}
                  onChange={handleInputChange}
                  placeholder="Nhập họ tên"
                />
              ) : (
                <span>{profile?.fullName || 'Chưa cập nhật'}</span>
              )}
            </div>

            <div className="info-group">
              <label>Email:</label>
              <span>{profile?.email || user?.email}</span>
            </div>

            <div className="info-group">
              <label>Số điện thoại:</label>
              {isEditing ? (
                <input
                  type="text"
                  name="phone"
                  value={formData.phone}
                  onChange={handleInputChange}
                  placeholder="Nhập số điện thoại"
                />
              ) : (
                <span>{profile?.phone || 'Chưa cập nhật'}</span>
              )}
            </div>

            <div className="info-group">
              <label>Vai trò:</label>
              <span className={`role-badge role-${profile?.role?.toLowerCase()}`}>
                {profile?.role || user?.role}
              </span>
            </div>

            {/* Thông tin gói pin (nếu có) */}
            {profile?.currentPackageId && (
              <div className="package-info">
                <h3>Thông Tin Gói Pin</h3>
                <div className="info-group">
                  <label>Tên gói:</label>
                  <span>{profile.packageName}</span>
                </div>
                <div className="info-group">
                  <label>Ngày bắt đầu:</label>
                  <span>{formatDate(profile.packageStartDate)}</span>
                </div>
                <div className="info-group">
                  <label>Ngày kết thúc:</label>
                  <span>{formatDate(profile.packageEndDate)}</span>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Buttons */}
        <div className="profile-actions">
          {isEditing ? (
            <>
              <button 
                className="btn btn-save" 
                onClick={handleSave}
                disabled={loading}
              >
                {loading ? 'Đang lưu...' : 'Lưu'}
              </button>
              <button 
                className="btn btn-cancel" 
                onClick={() => {
                  setIsEditing(false);
                  setFormData({
                    fullName: profile?.fullName || '',
                    phone: profile?.phone || '',
                    avatarUrl: profile?.avatarUrl || ''
                  });
                }}
                disabled={loading}
              >
                Hủy
              </button>
            </>
          ) : (
            <button 
              className="btn btn-edit" 
              onClick={() => setIsEditing(true)}
            >
              Chỉnh sửa
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
