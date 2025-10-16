import React, { useState, useEffect } from 'react';
import { useUser } from '../../contexts/UserContext';

export default function DebugInfo() {
  const { user } = useUser();
  const [debugInfo, setDebugInfo] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  const testAPI = async () => {
    if (!user) {
      setDebugInfo({ error: 'No user logged in' });
      return;
    }

    setIsLoading(true);
    setDebugInfo(null);

    try {
      // Test checkUserSwaps API
      const response = await fetch('http://localhost:8080/webAPI/api/checkUserSwaps', {
        credentials: 'include'
      });

      const debugData = {
        user: user,
        apiUrl: 'http://localhost:8080/webAPI/api/checkUserSwaps',
        responseStatus: response.status,
        responseStatusText: response.statusText,
        responseHeaders: Object.fromEntries(response.headers.entries()),
        timestamp: new Date().toISOString()
      };

      if (response.ok) {
        const data = await response.text();
        try {
          debugData.responseData = JSON.parse(data);
        } catch {
          debugData.responseData = data;
        }
      } else {
        debugData.error = `HTTP ${response.status}: ${response.statusText}`;
        try {
          const errorText = await response.text();
          debugData.errorData = errorText;
        } catch {
          debugData.errorData = 'Could not read error response';
        }
      }

      setDebugInfo(debugData);
    } catch (error) {
      setDebugInfo({
        error: error.message,
        user: user,
        timestamp: new Date().toISOString()
      });
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (user && user.role?.toLowerCase() === 'driver') {
      testAPI();
    }
  }, [user]);

  if (!user) {
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
      <h3 style={{ marginBottom: '15px', color: '#333' }}>🔍 Debug Information</h3>
      
      <div style={{ marginBottom: '15px' }}>
        <strong>Current User:</strong>
        <pre style={{ 
          backgroundColor: '#e9ecef', 
          padding: '10px', 
          borderRadius: '4px',
          fontSize: '12px',
          overflow: 'auto'
        }}>
          {JSON.stringify(user, null, 2)}
        </pre>
      </div>

      <button
        onClick={testAPI}
        disabled={isLoading}
        style={{
          backgroundColor: isLoading ? '#6c757d' : '#007bff',
          color: 'white',
          padding: '10px 20px',
          border: 'none',
          borderRadius: '4px',
          cursor: isLoading ? 'not-allowed' : 'pointer',
          fontSize: '14px',
          marginBottom: '15px'
        }}
      >
        {isLoading ? 'Testing...' : 'Test checkUserSwaps API'}
      </button>

      {debugInfo && (
        <div>
          <strong>API Response:</strong>
          <pre style={{ 
            backgroundColor: '#e9ecef', 
            padding: '10px', 
            borderRadius: '4px',
            fontSize: '12px',
            overflow: 'auto',
            maxHeight: '400px'
          }}>
            {JSON.stringify(debugInfo, null, 2)}
          </pre>
        </div>
      )}
    </div>
  );
}

