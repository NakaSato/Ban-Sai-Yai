import React from "react";
import { ViewState, UserRole } from "../types";
import {
  LayoutDashboard,
  Users,
  HandCoins,
  FileText,
  Bot,
  LogOut,
  Building2,
  ShieldCheck,
  X,
  ChevronLeft,
  ChevronRight,
  Settings,
} from "lucide-react";

interface SidebarProps {
  currentView: ViewState;
  onChangeView: (view: ViewState) => void;
  userRole: UserRole;
  onSignOut: () => void;
  isOpen: boolean;
  onClose: () => void;
  isCollapsed: boolean;
  toggleCollapse: () => void;
}

const Sidebar: React.FC<SidebarProps> = ({
  currentView,
  onChangeView,
  userRole,
  onSignOut,
  isOpen,
  onClose,
  isCollapsed,
  toggleCollapse,
}) => {
  // Define available menus
  const menuItems = [
    { id: "DASHBOARD", label: "Dashboard", icon: LayoutDashboard },
    {
      id: "MEMBERS",
      label: userRole === UserRole.MEMBER ? "My Profile" : "Member Management",
      icon: Users,
    },
    {
      id: "LOANS",
      label: userRole === UserRole.MEMBER ? "My Loans" : "Loans & Credits",
      icon: HandCoins,
    },
    { id: "ACCOUNTING", label: "Accounting & Reports", icon: FileText },
    { id: "ASSISTANT", label: "Smart Assistant", icon: Bot },
  ];

  // Filtering Logic based on RBAC Specification
  const filteredMenu = menuItems.filter((item) => {
    // Members cannot see Accounting (Officers CAN see it for General Expenses)
    if (userRole === UserRole.MEMBER && item.id === "ACCOUNTING") return false;
    // Secretary cannot see Member Management
    if (userRole === UserRole.SECRETARY && item.id === "MEMBERS") return false;
    return true;
  });

  return (
    <>
      {/* Mobile Overlay */}
      <div
        className={`fixed inset-0 bg-slate-900/60 backdrop-blur-sm z-30 lg:hidden transition-opacity duration-300 print:hidden ${
          isOpen ? "opacity-100" : "opacity-0 pointer-events-none"
        }`}
        onClick={onClose}
      />

      <div
        className={`h-screen bg-gradient-to-b from-[#064e3b] to-[#022c22] text-white flex flex-col fixed left-0 top-0 shadow-2xl z-40 transition-all duration-300 cubic-bezier(0.4, 0, 0.2, 1) print:hidden
        ${isOpen ? "translate-x-0" : "-translate-x-full lg:translate-x-0"} 
        w-72 ${isCollapsed ? "lg:w-20" : "lg:w-72"}
        border-r border-emerald-800/30
      `}
      >
        {/* Header */}
        <div
          className={`flex items-center h-24 transition-all duration-300 ${
            isCollapsed ? "lg:justify-center px-0" : "px-8"
          }`}
        >
          <div className="flex items-center space-x-3 overflow-hidden">
            <div className="bg-emerald-500/20 p-2.5 rounded-xl border border-emerald-400/20 group cursor-default shadow-[0_0_15px_rgba(16,185,129,0.2)]">
              <Building2
                className={`w-7 h-7 text-emerald-300 shrink-0 transition-all duration-300 ${
                  isCollapsed ? "lg:scale-110" : ""
                }`}
              />
            </div>
            <div
              className={`transition-all duration-300 origin-left ${
                isCollapsed ? "lg:hidden w-0 opacity-0" : "w-auto opacity-100"
              }`}
            >
              <h1 className="text-xl font-bold leading-none tracking-tight text-white font-display">
                Bansaiyai
              </h1>
              <p className="text-[10px] text-emerald-300/80 font-semibold mt-1.5 uppercase tracking-wider">
                Savings Group
              </p>
            </div>
          </div>
          {/* Close button for mobile */}
          <button
            onClick={onClose}
            className="lg:hidden ml-auto text-emerald-300 hover:text-white p-2 rounded-lg transition hover:bg-emerald-800/50"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Navigation */}
        <div className="flex-1 py-4 px-4 space-y-2 overflow-y-auto overflow-x-hidden scrollbar-thin scrollbar-thumb-emerald-800/50 scrollbar-track-transparent">
          <p
            className={`px-4 text-[11px] font-bold text-emerald-400/60 uppercase tracking-widest mb-4 transition-opacity duration-300 ${
              isCollapsed ? "lg:hidden" : ""
            }`}
          >
            Main Menu
          </p>
          {filteredMenu.map((item) => {
            const Icon = item.icon;
            const isActive = currentView === item.id;
            return (
              <button
                key={item.id}
                onClick={() => {
                  onChangeView(item.id as ViewState);
                  onClose();
                }}
                title={isCollapsed ? item.label : ""}
                className={`w-full flex items-center transition-all duration-200 group relative
                  ${
                    isCollapsed
                      ? "lg:justify-center lg:px-0"
                      : "space-x-3.5 px-5"
                  } 
                  py-3.5 rounded-2xl 
                  ${
                    isActive
                      ? "bg-emerald-600 text-white shadow-lg shadow-emerald-900/20 font-semibold ring-1 ring-white/10"
                      : "text-emerald-100/70 hover:bg-emerald-800/40 hover:text-white font-medium"
                  }`}
              >
                <Icon
                  className={`w-[1.35rem] h-[1.35rem] shrink-0 transition-transform duration-300 ${
                    isActive
                      ? "text-white"
                      : "group-hover:scale-110 text-emerald-300/60 group-hover:text-emerald-200"
                  }`}
                />
                <span
                  className={`whitespace-nowrap transition-all duration-300 text-[15px] ${
                    isCollapsed
                      ? "lg:hidden lg:w-0 lg:opacity-0"
                      : "opacity-100"
                  }`}
                >
                  {item.label}
                </span>
                {isActive && !isCollapsed && (
                  <div className="absolute right-4 w-1.5 h-1.5 rounded-full bg-emerald-200 shadow-[0_0_8px_rgba(255,255,255,0.8)]" />
                )}
              </button>
            );
          })}
        </div>

        {/* Footer / Role Switcher */}
        <div className="p-5 bg-black/10 backdrop-blur-sm border-t border-emerald-800/30">
          {/* Role Toggle */}
          <div
            className={`bg-emerald-900/40 border border-emerald-700/30 rounded-2xl transition-all duration-300 group overflow-hidden ${
              isCollapsed ? "lg:p-2 lg:flex lg:justify-center" : "p-3.5"
            }`}
          >
            <div
              className={`flex items-center ${
                isCollapsed ? "lg:justify-center" : "space-x-3.5"
              }`}
            >
              <div className="p-2 bg-emerald-500/10 rounded-xl shrink-0 border border-emerald-500/20">
                <ShieldCheck className="w-4 h-4 text-emerald-300" />
              </div>
              <div
                className={`transition-all duration-300 overflow-hidden ${
                  isCollapsed ? "lg:hidden w-0 opacity-0" : "flex-1"
                }`}
              >
                <p className="text-[10px] text-emerald-400 font-bold uppercase tracking-wider mb-0.5">
                  Current Role
                </p>
                <p className="text-sm font-semibold text-white capitalize truncate leading-tight">
                  {userRole.toLowerCase()}
                </p>
              </div>
            </div>
          </div>

          <div className="mt-4 flex items-center justify-between gap-2">
            <button
              onClick={onSignOut}
              className={`flex items-center justify-center text-emerald-300/60 hover:text-white transition-all duration-200 py-2.5 rounded-xl hover:bg-emerald-800/30 flex-1 group`}
              title="Sign Out"
            >
              <LogOut className="w-[1.2rem] h-[1.2rem] shrink-0 group-hover:-translate-x-0.5 transition-transform" />
              <span
                className={`ml-2 text-sm font-medium transition-all duration-300 ${
                  isCollapsed ? "lg:hidden w-0 opacity-0" : "block"
                }`}
              >
                Sign Out
              </span>
            </button>

            <button
              onClick={toggleCollapse}
              className="hidden lg:flex items-center justify-center p-2.5 text-emerald-400/50 hover:text-white hover:bg-emerald-800/30 rounded-xl transition-colors"
            >
              {isCollapsed ? (
                <ChevronRight className="w-5 h-5" />
              ) : (
                <ChevronLeft className="w-5 h-5" />
              )}
            </button>
          </div>
        </div>
      </div>
    </>
  );
};

export default Sidebar;
