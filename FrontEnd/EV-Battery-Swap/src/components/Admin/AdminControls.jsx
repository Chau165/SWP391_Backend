import React, { useState } from 'react';
import { useUser } from '../../contexts/UserContext';

export default function AdminControls() {
  const { user, isAdmin } = useUser();
  const [comments, setComments] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [status, setStatus] = useState('');

  const fetchCommentsForAdmin = async () => {
    setIsLoading(true);
    setStatus('');
    
    try {
  const response = await fetch('http://localhost:8080/TestWebAPI/api/comment', {
        method: 'GET',
        credentials: 'include'
      });

      if (response.status === 401) {
        setStatus('Vui lòng đăng nhập.');
        return;
      }

      if (response.status === 403) {
        setStatus('Chỉ Admin được xem nhận xét. Bạn không có quyền.');
        return;
      }

      if (!response.ok) {
        setStatus('Lỗi: HTTP ' + response.status);
        return;
      }

      const data = await response.json();
      
      if (Array.isArray(data)) {
        setComments(data);
        setStatus(`Đã tải ${data.length} nhận xét.`);
      } else {
        setStatus('Dữ liệu không hợp lệ.');
      }
    } catch (error) {
      console.error('Error fetching comments:', error);
      setStatus('Lỗi kết nối mạng.');
    } finally {
      setIsLoading(false);
    }
  };

  if (!isAdmin) {
    return null;
  }

  return (
    <div style={{
      maxWidth: '800px',
      margin: '20px auto',
      padding: '20px',
      backgroundColor: '#f8f9fa',
      borderRadius: '8px',
      border: '1px solid #dee2e6'
    }}>
      <h2 style={{ marginBottom: '20px', color: '#333' }}>Admin: Quản lý nhận xét</h2>
      
      <button
        onClick={fetchCommentsForAdmin}
        disabled={isLoading}
        style={{
          backgroundColor: isLoading ? '#6c757d' : '#28a745',
          color: 'white',
          padding: '10px 20px',
          border: 'none',
          borderRadius: '4px',
          cursor: isLoading ? 'not-allowed' : 'pointer',
          fontSize: '16px',
          marginBottom: '20px'
        }}
      >
        {isLoading ? 'Đang tải...' : 'Lấy nhận xét'}
      </button>

      {status && (
        <div style={{
          marginBottom: '15px',
          padding: '10px',
          borderRadius: '4px',
          backgroundColor: status.includes('Lỗi') ? '#f8d7da' : '#d4edda',
          color: status.includes('Lỗi') ? '#721c24' : '#155724',
          border: `1px solid ${status.includes('Lỗi') ? '#f5c6cb' : '#c3e6cb'}`
        }}>
          {status}
        </div>
      )}

      {comments.length > 0 && (
        <div>
          <h3 style={{ marginBottom: '15px', color: '#333' }}>Danh sách nhận xét:</h3>
          <div style={{ display: 'grid', gap: '15px' }}>
            {comments.map((comment, index) => (
              <div
                key={index}
                style={{
                  padding: '15px',
                  backgroundColor: 'white',
                  borderRadius: '6px',
                  border: '1px solid #dee2e6',
                  boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
                }}
              >
                <div style={{ marginBottom: '10px' }}>
                  <strong style={{ color: '#007bff' }}>Tài xế:</strong> {comment.userName || 'Không xác định'} (ID: {comment.userId})
                </div>
                <div style={{ marginBottom: '10px' }}>
                  <strong style={{ color: '#007bff' }}>Trạm:</strong> {comment.stationName || 'Không xác định'} (Swap ID: {comment.swapId})
                </div>
                <div style={{ marginBottom: '10px' }}>
                  <strong style={{ color: '#007bff' }}>Nội dung:</strong>
                  <div style={{
                    marginTop: '5px',
                    padding: '10px',
                    backgroundColor: '#f8f9fa',
                    borderRadius: '4px',
                    border: '1px solid #e9ecef'
                  }}>
                    {comment.content}
                  </div>
                </div>
                <div style={{ fontSize: '14px', color: '#6c757d' }}>
                  <strong>Thời gian:</strong> {comment.timePost ? new Date(comment.timePost).toLocaleString('vi-VN') : 'Không xác định'}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {comments.length === 0 && !isLoading && status && !status.includes('Lỗi') && (
        <div style={{
          padding: '20px',
          textAlign: 'center',
          color: '#6c757d',
          backgroundColor: 'white',
          borderRadius: '6px',
          border: '1px solid #dee2e6'
        }}>
          Chưa có nhận xét nào.
        </div>
      )}
    </div>
  );
}
