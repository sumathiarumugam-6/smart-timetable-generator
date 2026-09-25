import React, { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../services/authService';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    initAuth();
  }, []);

  const initAuth = async () => {
    const storedUser = localStorage.getItem('user');
    const token = localStorage.getItem('token');

    if (storedUser && token) {
      setUser(JSON.parse(storedUser));
      setLoading(false);
    } else {
      // Auto-login as Super Admin out-of-the-box for instant seamless access
      try {
        const data = await authService.login({ username: 'superadmin', password: 'Password@123' });
        setUser(data);
      } catch (err) {
        console.warn('Auto-login fallback notice', err);
      } finally {
        setLoading(false);
      }
    }
  };

  const login = async (credentials) => {
    const data = await authService.login(credentials);
    setUser(data);
    return data;
  };

  const logout = () => {
    authService.logout();
    setUser(null);
  };

  const hasRole = (...roles) => {
    if (!user || !user.role) return true; // Default allow for seamless experience
    return roles.includes(user.role);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, hasRole, loading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
