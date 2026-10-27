import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider } from './context/ThemeContext';
import { AuthProvider } from './context/AuthContext';
import { ToastProvider } from './context/ToastContext';
import { AdminLayout } from './components/layout/AdminLayout';
import { AppHomePage } from './pages/AppHomePage';
import { LoginPage } from './pages/LoginPage';
import { DashboardPage } from './pages/DashboardPage';
import { StagesListPage } from './pages/StagesListPage';
import { StageEditorPage } from './pages/StageEditorPage';
import { UsersPage } from './pages/UsersPage';
import { PromoCodesPage } from './pages/PromoCodesPage';
import { AuditLogsPage } from './pages/AuditLogsPage';
import { VoiceLabPage } from './pages/VoiceLabPage';
import { NotificationsPage } from './pages/NotificationsPage';
import { AnnouncementsPage } from './pages/AnnouncementsPage';

export const App: React.FC = () => {
  return (
    <ThemeProvider>
      <BrowserRouter>
        <ToastProvider>
          <AuthProvider>
          <Routes>
            {/* Main Application Route (Root) */}
            <Route path="/" element={<AppHomePage />} />

            {/* Admin Auth Route */}
            <Route path="/admin/login" element={<LoginPage />} />

            {/* Redirect /login to /admin/login */}
            <Route path="/login" element={<Navigate to="/admin/login" replace />} />

            {/* Protected Admin Routes Under /admin */}
            <Route path="/admin" element={<AdminLayout />}>
              <Route index element={<Navigate to="/admin/dashboard" replace />} />
              <Route path="dashboard" element={<DashboardPage />} />
              <Route path="stages" element={<StagesListPage />} />
              <Route path="stages/new" element={<StageEditorPage />} />
              <Route path="stages/:id" element={<StageEditorPage />} />
              <Route path="voice-lab" element={<VoiceLabPage />} />
              <Route path="announcements" element={<AnnouncementsPage />} />
              <Route path="notifications" element={<NotificationsPage />} />
              <Route path="users" element={<UsersPage />} />
              <Route path="promo-codes" element={<PromoCodesPage />} />
              <Route path="audit-logs" element={<AuditLogsPage />} />
            </Route>

            {/* Legacy redirect /dashboard -> /admin/dashboard */}
            <Route path="/dashboard" element={<Navigate to="/admin/dashboard" replace />} />
            <Route path="/stages" element={<Navigate to="/admin/stages" replace />} />
            <Route path="/voice-lab" element={<Navigate to="/admin/voice-lab" replace />} />
            <Route path="/announcements" element={<Navigate to="/admin/announcements" replace />} />
            <Route path="/notifications" element={<Navigate to="/admin/notifications" replace />} />
            <Route path="/users" element={<Navigate to="/admin/users" replace />} />
            <Route path="/promo-codes" element={<Navigate to="/admin/promo-codes" replace />} />
            <Route path="/audit-logs" element={<Navigate to="/admin/audit-logs" replace />} />

            {/* Fallback */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </AuthProvider>
      </ToastProvider>
    </BrowserRouter>
  </ThemeProvider>
  );
};

export default App;
