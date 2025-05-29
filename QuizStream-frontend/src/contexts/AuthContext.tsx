import React, { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import { message } from 'antd';
import api from '../services/api';

interface User {
  id: number;
  username: string;
  email: string;
}

interface AuthContextType {
  user: User | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (username: string, email: string, password: string) => Promise<void>;
  logout: () => void;
  //fetchUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const initAuth = async () => {
      try {
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
          setUser(JSON.parse(storedUser));
        }
        //await fetchUser();
      } catch (error) {
        console.error('Failed to fetch user', error);
      } finally {
        setLoading(false);
      }
    };

    initAuth();
  }, []);

  /*
  const fetchUser = async () => {
    try {
      const response = await api.get('/api/auth/current-user');
      if (response.data.id) {
        const userData = {
          id: response.data.id,
          username: response.data.username,
          email: response.data.email,
        };
        setUser(userData);
        localStorage.setItem('user', JSON.stringify(userData));
      }
    } catch (error) {
      logout();
    }
  };
  */

  const login = async (email: string, password: string) => {
    const response = await api.post('/auth/login', { email, password });
    const userData = {
      id: response.data.id,
      username: response.data.username,
      email: response.data.email,
    };
    setUser(userData);
    localStorage.setItem('user', JSON.stringify(userData));
    message.success('Uspješno prijavljeni!');
  };

  const register = async (username: string, email: string, password: string) => {
    const response = await api.post('/auth/register', { username, email, password });
    const userData = {
      id: response.data.id,
      username: response.data.username,
      email: response.data.email,
    };
    setUser(userData);
    localStorage.setItem('user', JSON.stringify(userData));
    message.success('Registracija uspješna!');
  };

  const logout = async () => {
    /*
    try {
      await api.post('/api/auth/logout');
    } catch (error) {
      console.error('Logout error', error);
    }
    */
    setUser(null);
    localStorage.removeItem('user');
    message.success('Odjavljeni ste');
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
