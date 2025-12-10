import React, { useState } from 'react';
import Sidebar from './components/Sidebar';
import Dashboard from './components/Dashboard';
import MemberManagement from './components/MemberManagement';
import LoanManagement from './components/LoanManagement';
import AccountingReports from './components/AccountingReports';
import GeminiAssistant from './components/GeminiAssistant';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import LoginPage from './components/LoginPage';
import { ViewState, UserRole, Notification } from './types';
import { MOCK_NOTIFICATIONS } from './constants';

const App: React.FC = () => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [currentView, setCurrentView] = useState<ViewState>('DASHBOARD');
  const [userRole, setUserRole] = useState<UserRole>(UserRole.OFFICER);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);
  
  // Notification State
  const [notifications, setNotifications] = useState<Notification[]>(MOCK_NOTIFICATIONS);

  const handleLoginSuccess = (role: UserRole) => {
    setUserRole(role);
    setIsAuthenticated(true);
    // Reset view to Dashboard on new login
    setCurrentView('DASHBOARD');
  };

  const handleSignOut = () => {
    setIsAuthenticated(false);
    setUserRole(UserRole.OFFICER); // Reset to default
  };

  const handleMarkAsRead = (id: string) => {
    setNotifications(prev => prev.map(n => n.id === id ? { ...n, read: true } : n));
  };

  const handleMarkAllAsRead = () => {
    setNotifications(prev => prev.map(n => ({ ...n, read: true })));
  };

  const renderContent = () => {
    switch (currentView) {
      case 'DASHBOARD':
        return <Dashboard userRole={userRole} onNavigate={handleViewChange} />;
      case 'MEMBERS':
        return <MemberManagement userRole={userRole} />;
      case 'LOANS':
        return <LoanManagement userRole={userRole} />;
      case 'ACCOUNTING':
        return <AccountingReports userRole={userRole} />;
      case 'ASSISTANT':
        return <GeminiAssistant />;
      default:
        return <Dashboard userRole={userRole} onNavigate={handleViewChange} />;
    }
  };

  const handleViewChange = (view: ViewState) => {
    setCurrentView(view);
    setIsMobileMenuOpen(false);
  };

  // --- Authentication Guard ---
  if (!isAuthenticated) {
    return <LoginPage onLoginSuccess={handleLoginSuccess} />;
  }

  // --- Main App Layout ---
  return (
    <div className="flex h-screen bg-[#f8fafc] overflow-hidden">
      
      {/* Responsive Sidebar */}
      <Sidebar 
        currentView={currentView} 
        onChangeView={handleViewChange} 
        userRole={userRole}
        onSignOut={handleSignOut}
        isOpen={isMobileMenuOpen}
        onClose={() => setIsMobileMenuOpen(false)}
        isCollapsed={isSidebarCollapsed}
        toggleCollapse={() => setIsSidebarCollapsed(!isSidebarCollapsed)}
      />
      
      {/* Main Content Area */}
      {/* Adjusted left margin logic for new sidebar width (w-72) */}
      <div className={`flex-1 flex flex-col h-screen relative transition-all duration-300 ease-in-out ${isSidebarCollapsed ? 'lg:ml-20' : 'lg:ml-72'}`}>
        
        {/* Top Navbar */}
        <Navbar 
          userRole={userRole} 
          currentView={currentView} 
          onMenuClick={() => setIsMobileMenuOpen(true)}
          notifications={notifications}
          onMarkAsRead={handleMarkAsRead}
          onMarkAllAsRead={handleMarkAllAsRead}
        />

        {/* Scrollable Content */}
        <main className="flex-1 overflow-y-auto p-4 md:p-6 lg:p-8 scroll-smooth">
          <div className="max-w-7xl mx-auto flex flex-col min-h-[calc(100vh-8rem)]">
            <div className="flex-1">
              {renderContent()}
            </div>
            {/* Footer at bottom of content */}
            <Footer />
          </div>
        </main>
      </div>
    </div>
  );
};

export default App;