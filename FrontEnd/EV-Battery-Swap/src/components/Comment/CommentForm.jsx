import React, { useState, useEffect } from 'react';
import { useUser } from '../../contexts/UserContext';

export default function CommentForm() {
  const { user } = useUser();
  const [stations, setStations] = useState([]);
  const [selectedStation, setSelectedStation] = useState('');
  const [commentContent, setCommentContent] = useState('');
  const [status, setStatus] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Load stations for the logged-in driver
  useEffect(() => {
    if (user && user.role?.toLowerCase() === 'driver') {
      loadStationsForSelect();
    }
  }, [user]);

  const loadStationsForSelect = async () => {
    setIsLoading(true);
    setStatus('');
    
    try {
      const response = await fetch('http://localhost:8080/webAPI/api/checkUserSwaps', {
        credentials: 'include'
      });

      if (response.status === 401) {
        setStatus('Vui lòng đăng nhập để gửi nhận xét.');
        return;
      }

      if (response.status === 403) {
        setStatus('Chỉ tài xế (Driver) được gửi nhận xét. Bạn không có quyền.');
        return;
      }

      if (response.status === 204) {
        setStatus('Bạn chưa có giao dịch hoàn thành (Completed). Không thể gửi nhận xét.');
        return;
      }

      if (!response.ok) {
        setStatus('Lỗi kiểm tra giao dịch: HTTP ' + response.status);
        return;
      }

      const data = await response.json();
      
      if (!Array.isArray(data) || data.length === 0) {
        setStatus('Bạn chưa có giao dịch hoàn thành (Completed). Không thể gửi nhận xét.');
        return;
      }

      setStations(data);
      setStatus('Chọn giao dịch để gửi nhận xét.');
    } catch (error) {
      console.error('Error loading stations:', error);
      setStatus('Lỗi kết nối mạng.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!selectedStation || !commentContent.trim()) {
      setStatus('Vui lòng chọn giao dịch và nhập nội dung nhận xét.');
      return;
    }

    setIsSubmitting(true);
    setStatus('');

    try {
      const response = await fetch('http://localhost:8080/webAPI/api/comment', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        credentials: 'include',
        body: JSON.stringify({
          swapId: parseInt(selectedStation),
          content: commentContent.trim()
        })
      });

      const data = await response.json();

      if (response.ok) {
        setStatus('Gửi nhận xét thành công!');
        setCommentContent('');
        setSelectedStation('');
        // Reload stations
        loadStationsForSelect();
      } else {
        setStatus('Lỗi: ' + (data.message || 'Không thể gửi nhận xét'));
      }
    } catch (error) {
      console.error('Error submitting comment:', error);
      setStatus('Lỗi kết nối mạng.');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!user || user.role?.toLowerCase() !== 'driver') {
    return null;
  }

  return (
    <div style={{
      maxWidth: '600px',
      margin: '20px auto',
      padding: '20px',
      backgroundColor: '#f8f9fa',
      borderRadius: '8px',
      border: '1px solid #dee2e6'
    }}>
      <h2 style={{ marginBottom: '20px', color: '#333' }}>Gửi nhận xét</h2>
      
      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: '15px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
            Chọn giao dịch:
          </label>
          <select
            value={selectedStation}
            onChange={(e) => setSelectedStation(e.target.value)}
            disabled={isLoading || stations.length === 0}
            style={{
              width: '100%',
              padding: '8px',
              borderRadius: '4px',
              border: '1px solid #ccc'
            }}
          >
            <option value="">-- Chọn giao dịch --</option>
            {stations.map((station) => (
              <option key={station.swapId} value={station.swapId}>
                {station.name} - {station.address} (Swap ID: {station.swapId})
              </option>
            ))}
          </select>
        </div>

        <div style={{ marginBottom: '15px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
            Nội dung nhận xét:
          </label>
          <textarea
            value={commentContent}
            onChange={(e) => setCommentContent(e.target.value)}
            placeholder="Nhập nhận xét của bạn về trạm và dịch vụ..."
            rows="4"
            style={{
              width: '100%',
              padding: '8px',
              borderRadius: '4px',
              border: '1px solid #ccc',
              resize: 'vertical'
            }}
          />
        </div>

        <button
          type="submit"
          disabled={isSubmitting || isLoading || !selectedStation || !commentContent.trim()}
          style={{
            backgroundColor: isSubmitting ? '#6c757d' : '#007bff',
            color: 'white',
            padding: '10px 20px',
            border: 'none',
            borderRadius: '4px',
            cursor: isSubmitting ? 'not-allowed' : 'pointer',
            fontSize: '16px'
          }}
        >
          {isSubmitting ? 'Đang gửi...' : 'Gửi nhận xét'}
        </button>
      </form>

      {status && (
        <div style={{
          marginTop: '15px',
          padding: '10px',
          borderRadius: '4px',
          backgroundColor: status.includes('thành công') ? '#d4edda' : '#f8d7da',
          color: status.includes('thành công') ? '#155724' : '#721c24',
          border: `1px solid ${status.includes('thành công') ? '#c3e6cb' : '#f5c6cb'}`
        }}>
          {status}
        </div>
      )}
    </div>
  );
}
