import React from 'react';
import { useUser } from '../../contexts/UserContext';
import CommentForm from '../Comment/CommentForm';
import AdminControls from '../Admin/AdminControls';
import DebugInfo from '../Debug/DebugInfo';

export default function UserDashboard() {
  const { user, isLoggedIn, isAdmin, isDriver, isStaff, logout } = useUser();

  if (!isLoggedIn) {
    return null;
  }

  return (
    <div style={{
      maxWidth: '1000px',
      margin: '20px auto',
      padding: '20px'
    }}>
      {/* User Info */}
      <div style={{
        backgroundColor: '#e3f2fd',
        padding: '15px',
        borderRadius: '8px',
        marginBottom: '20px',
        border: '1px solid #2196f3'
      }}>
        <h3 style={{ margin: '0 0 10px 0', color: '#1976d2' }}>
          Đã đăng nhập: {user?.fullName || user?.email}
        </h3>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <strong>Role:</strong> {user?.role} | 
            <strong> ID:</strong> {user?.id} | 
            <strong> Email:</strong> {user?.email}
          </div>
          <button
            onClick={logout}
            style={{
              backgroundColor: '#dc3545',
              color: 'white',
              padding: '8px 16px',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '14px'
            }}
          >
            Đăng xuất
          </button>
        </div>
      </div>

      {/* Debug Info - Always show for debugging */}
      <DebugInfo />

      {/* Role-based Content */}
      {isAdmin && (
        <div>
          <AdminControls />
        </div>
      )}

      {isDriver && (
        <div>
          <CommentForm />
        </div>
      )}

      {isStaff && (
        <div style={{
          padding: '20px',
          backgroundColor: '#fff3cd',
          borderRadius: '8px',
          border: '1px solid #ffeaa7',
          textAlign: 'center'
        }}>
          <h3 style={{ color: '#856404', margin: '0 0 10px 0' }}>
            Chào mừng, {user?.fullName}!
          </h3>
          <p style={{ color: '#856404', margin: '0' }}>
            Bạn đang đăng nhập với vai trò <strong>Staff</strong>. 
            Hiện tại chưa có chức năng dành riêng cho Staff.
          </p>
        </div>
      )}
    </div>
  );
}
