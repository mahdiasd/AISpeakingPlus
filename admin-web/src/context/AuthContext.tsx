import React, { createContext, useContext, useState, useEffect } from 'react';
import { AdminProfile } from '../types';
import { api } from '../api/client';

interface AuthContextType {
  admin: AdminProfile | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [admin, setAdmin] = useState<AdminProfile | null>(api.getSavedAdmin());
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    const verifyAuth = async () => {
      if (api.isAuthenticated()) {
        try {
          const profile = await api.getMe();
          setAdmin(profile);
        } catch (err) {
          console.error('Failed to restore auth session:', err);
          setAdmin(null);
        }
      } else {
        setAdmin(null);
      }
      setIsLoading(false);
    };

    verifyAuth();
  }, []);

  const login = async (username: string, password: string) => {
    const res = await api.login(username, password);
    setAdmin(res.admin);
  };

  const logout = () => {
    api.logout();
    setAdmin(null);
  };

  return (
    <AuthContext.Provider
      value={{
        admin,
        isAuthenticated: !!admin,
        isLoading,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
