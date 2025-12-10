
import React, { useState, useRef, useEffect } from 'react';
import { Bell, HelpCircle, User, ChevronRight, Menu, CheckCircle, AlertTriangle, Info, X } from 'lucide-react';
import { UserRole, ViewState, Notification } from '../types';

interface NavbarProps {
  userRole: UserRole;
  currentView: ViewState;
  onMenuClick: () => void;
  notifications: Notification[];
  onMarkAsRead: (id: string) => void;
  onMarkAllAsRead: () => void;
}

const Navbar: React.FC<NavbarProps> = ({ 
  userRole, 
  currentView, 
  onMenuClick,
  notifications,
  onMarkAsRead,
  onMarkAllAsRead
}) => {
  const [showNotifications, setShowNotifications] = useState(false);
  const notificationRef = useRef<HTMLDivElement>(null);
  const unreadCount = notifications.filter(n => !n.read).length;

  const getBreadcrumb = () => {
    switch (currentView) {
      case 'DASHBOARD': return 'Dashboard';
      case 'MEMBERS': return userRole === UserRole.MEMBER ? 'My Profile' : 'Member Management';
      case 'LOANS': return 'Loans & Credits';
      case 'ACCOUNTING': return 'Accounting & Reports';
      case 'ASSISTANT': return 'Smart Assistant';
      default: return 'Overview';
    }
  };

  const getRoleLabel = (role: UserRole) => {
    switch (role) {
      case UserRole.ADMIN: return 'System Admin';
      case UserRole.OFFICER: return 'Officer (Teller)';
      case UserRole.SECRETARY: return 'Secretary';
      case UserRole.PRESIDENT: return 'President';
      case UserRole.MEMBER: return 'Member';
      default: return role;
    }
  };

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (notificationRef.current && !notificationRef.current.contains(event.target as Node)) {
        setShowNotifications(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const getNotificationIcon = (type: string) => {
      switch (type) {
          case 'SUCCESS': return <CheckCircle className="w-4 h-4 text-emerald-600" />;
          case 'ALERT': return <AlertTriangle className="w-4 h-4 text-red-600" />;
          default: return <Info className="w-4 h-4 text-blue-600" />;
      }
  };

  return (
    <header className="sticky top-0 z-20 h-20 px-4 md:px-8 flex items-center justify-between bg-white/80 backdrop-blur-md border-b border-gray-200/50 transition-all duration-300 print:hidden">
      {/* Left: Menu & Breadcrumbs */}
      <div className="flex items-center space-x-3 md:space-x-5">
        {/* Mobile Hamburger Menu */}
        <button 
            onClick={onMenuClick}
            className="lg:hidden p-2 -ml-2 text-gray-600 hover:bg-gray-100 rounded-xl transition"
        >
            <Menu className="w-6 h-6" />
        </button>

        <div className="flex flex-col md:flex-row md:items-center md:space-x-3">
          <span className="hidden md:inline text-sm font-medium text-gray-400 hover:text-emerald-600 cursor-pointer transition">Bansaiyai Savings</span>
          <ChevronRight className="w-4 h-4 text-gray-300 hidden md:inline" />
          <h1 className="text-lg font-bold text-gray-800 tracking-tight">{getBreadcrumb()}</h1>
        </div>
      </div>

      {/* Right: Actions & Profile */}
      <div className="flex items-center space-x-3 md:space-x-6">
        {/* Utilities */}
        <div className="flex items-center space-x-2 md:space-x-4 pr-2 md:pr-6 border-r border-gray-200/50">
          
          {/* Notification Bell */}
          <div className="relative" ref={notificationRef}>
              <button 
                onClick={() => setShowNotifications(!showNotifications)}
                className={`relative p-2.5 rounded-full transition-all duration-200 outline-none group ${showNotifications ? 'bg-emerald-50 text-emerald-600' : 'text-gray-400 hover:bg-gray-100 hover:text-gray-600'}`}
              >
                <Bell className="w-5 h-5" />
                {unreadCount > 0 && (
                  <span className="absolute top-2 right-2.5 w-2 h-2 bg-red-500 rounded-full ring-2 ring-white animate-pulse"></span>
                )}
              </button>

              {/* Notification Dropdown */}
              {showNotifications && (
                  <div className="absolute right-0 mt-4 w-80 md:w-96 bg-white rounded-2xl shadow-xl border border-gray-100 ring-1 ring-black/5 z-50 overflow-hidden origin-top-right animate-in fade-in zoom-in-95 duration-200">
                      <div className="px-5 py-4 border-b border-gray-50 flex justify-between items-center bg-white sticky top-0 z-10">
                          <h3 className="font-bold text-gray-800 text-sm">Notifications</h3>
                          {unreadCount > 0 && (
                              <button 
                                onClick={onMarkAllAsRead}
                                className="text-xs text-emerald-600 hover:text-emerald-700 font-semibold hover:bg-emerald-50 px-2 py-1 rounded-md transition"
                              >
                                  Mark all read
                              </button>
                          )}
                      </div>
                      <div className="max-h-[350px] overflow-y-auto scrollbar-thin">
                          {notifications.length > 0 ? (
                              notifications.map((n) => (
                                  <div 
                                    key={n.id} 
                                    className={`p-4 border-b border-gray-50 hover:bg-gray-50/80 transition cursor-pointer flex gap-4 relative group ${!n.read ? 'bg-emerald-50/40' : ''}`}
                                    onClick={() => onMarkAsRead(n.id)}
                                  >
                                      {!n.read && (
                                          <div className="absolute left-0 top-3 bottom-3 w-1 rounded-r-md bg-emerald-500"></div>
                                      )}
                                      <div className={`flex-shrink-0 mt-1 p-2 rounded-xl h-fit ${
                                          n.type === 'ALERT' ? 'bg-red-100 text-red-600' : 
                                          n.type === 'SUCCESS' ? 'bg-emerald-100 text-emerald-600' : 
                                          'bg-blue-100 text-blue-600'
                                      }`}>
                                          {getNotificationIcon(n.type)}
                                      </div>
                                      <div className="flex-1">
                                          <div className="flex justify-between items-start mb-1">
                                              <p className={`text-sm leading-tight ${!n.read ? 'font-bold text-gray-900' : 'font-medium text-gray-700'}`}>
                                                  {n.title}
                                              </p>
                                              <span className="text-[10px] text-gray-400 whitespace-nowrap ml-2 font-medium">{n.date}</span>
                                          </div>
                                          <p className="text-xs text-gray-500 leading-relaxed line-clamp-2">
                                              {n.message}
                                          </p>
                                      </div>
                                  </div>
                              ))
                          ) : (
                              <div className="p-12 text-center text-gray-400 flex flex-col items-center justify-center min-h-[200px]">
                                  <div className="bg-gray-50 p-4 rounded-full mb-3">
                                      <Bell className="w-6 h-6 text-gray-300" />
                                  </div>
                                  <p className="text-sm font-medium">You're all caught up!</p>
                              </div>
                          )}
                      </div>
                      <div className="p-2 border-t border-gray-100 bg-gray-50/50 text-center sticky bottom-0 z-10">
                          <button className="text-xs text-gray-500 hover:text-emerald-600 font-bold transition flex items-center justify-center gap-1 mx-auto w-full py-2 rounded-lg hover:bg-gray-100 group/btn">
                              View All History <ChevronRight className="w-3 h-3 transition-transform group-hover/btn:translate-x-0.5" />
                          </button>
                      </div>
                  </div>
              )}
          </div>

          <button className="p-2.5 text-gray-400 hover:text-emerald-600 transition rounded-full hover:bg-gray-100 hidden sm:block group">
            <HelpCircle className="w-5 h-5 transition-transform" />
          </button>
        </div>

        {/* Profile */}
        <div className="flex items-center gap-3 pl-2 py-1 pr-1 rounded-full hover:bg-gray-50 transition border border-transparent hover:border-gray-200 cursor-pointer group">
          <div className="text-right hidden md:block leading-tight">
            <p className="text-sm font-bold text-gray-800">Demo User</p>
            <p className="text-[11px] text-emerald-600 font-bold uppercase tracking-wider">{getRoleLabel(userRole)}</p>
          </div>
          <div className="w-9 h-9 md:w-10 md:h-10 bg-gradient-to-br from-emerald-100 to-teal-100 rounded-full flex items-center justify-center border-2 border-white shadow-sm ring-1 ring-gray-100 text-emerald-700 group-hover:scale-105 transition-transform">
            <span className="font-bold text-sm">US</span>
          </div>
        </div>
      </div>
    </header>
  );
};

export default Navbar;
