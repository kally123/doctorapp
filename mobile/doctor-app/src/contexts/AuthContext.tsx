import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import * as SecureStore from 'expo-secure-store';
import { api } from '../services/api';

interface Doctor {
  id: string;
  email: string;
  phone: string;
  firstName: string;
  lastName: string;
  specialization: string;
  registrationNumber: string;
  avatarUrl?: string;
  isVerified: boolean;
}

interface AuthContextType {
  user: Doctor | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  updateUser: (user: Doctor) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const TOKEN_KEY = 'doctor_auth_token';
const USER_KEY = 'doctor_user_data';

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<Doctor | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadStoredAuth();
  }, []);

  const loadStoredAuth = async () => {
    try {
      const [token, userData] = await Promise.all([
        SecureStore.getItemAsync(TOKEN_KEY),
        SecureStore.getItemAsync(USER_KEY),
      ]);

      if (token && userData) {
        api.setAuthToken(token);
        setUser(JSON.parse(userData));
      }
    } catch (error) {
      console.error('Failed to load auth:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const login = async (email: string, password: string) => {
    try {
      const response = await api.post('/doctors/auth/login', { email, password });
      const { token, doctor } = response.data;

      await Promise.all([
        SecureStore.setItemAsync(TOKEN_KEY, token),
        SecureStore.setItemAsync(USER_KEY, JSON.stringify(doctor)),
      ]);

      api.setAuthToken(token);
      setUser(doctor);
    } catch (error) {
      throw error;
    }
  };

  const logout = async () => {
    try {
      await api.post('/doctors/auth/logout');
    } catch (error) {
      console.error('Logout API error:', error);
    } finally {
      await Promise.all([
        SecureStore.deleteItemAsync(TOKEN_KEY),
        SecureStore.deleteItemAsync(USER_KEY),
      ]);
      api.setAuthToken(null);
      setUser(null);
    }
  };

  const updateUser = (userData: Doctor) => {
    setUser(userData);
    SecureStore.setItemAsync(USER_KEY, JSON.stringify(userData));
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        logout,
        updateUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
