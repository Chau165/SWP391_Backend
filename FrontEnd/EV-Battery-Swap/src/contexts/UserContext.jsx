import React, { createContext, useContext, useState, useEffect } from 'react';

const UserContext = createContext();

export const useUser = () => {
  const context = useContext(UserContext);
  if (!context) {
    throw new Error('useUser must be used within a UserProvider');
  }
  return context;
};

export const UserProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  // Load user from localStorage on app start
  useEffect(() => {
    const savedUser = localStorage.getItem('user');
    if (savedUser) {
      try {
        const parsed = JSON.parse(savedUser);
        // Defensive: normalize role from saved data (trim) so isAdmin/isStaff checks work
        if (parsed && parsed.role) {
          parsed.role = parsed.role.trim();
          // persist normalized role back to localStorage so subsequent loads are correct
          localStorage.setItem('user', JSON.stringify(parsed));
        }
        setUser(parsed);
      } catch (error) {
        console.error('Error parsing saved user:', error);
        localStorage.removeItem('user');
      }
    }
  }, []);

  const login = async (email, password) => {
    setIsLoading(true);
    try {
  const response = await fetch('http://localhost:8080/TestWebAPI/api/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        credentials: 'include', // Quan trọng: để gửi cookies/session
        body: JSON.stringify({ email, password })
      });

      const data = await response.json();

      if (response.ok && data.status === 'success') {
        const userData = data.user;
        // Defensive: trim role to avoid issues from backend whitespace
        if (userData && userData.role) userData.role = userData.role.trim();
        setUser(userData);
        localStorage.setItem('user', JSON.stringify(userData));
        return { success: true, user: userData };
      } else {
        return { success: false, message: data.message || 'Đăng nhập thất bại' };
      }
    } catch (error) {
      console.error('Login error:', error);
      return { success: false, message: 'Lỗi kết nối mạng' };
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('user');
  };

  const isAdmin = user?.role?.toLowerCase() === 'admin';
  const isDriver = user?.role?.toLowerCase() === 'driver';
  const isStaff = user?.role?.toLowerCase() === 'staff';

  const value = {
    user,
    isLoading,
    login,
    logout,
    isAdmin,
    isDriver,
    isStaff,
    isLoggedIn: !!user
  };

  return (
    <UserContext.Provider value={value}>
      {children}
    </UserContext.Provider>
  );
};
