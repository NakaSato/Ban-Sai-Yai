import React from 'react';
import { UserRole, ViewState } from '../types';
import MemberDashboard from './dashboard/MemberDashboard';
import OfficerDashboard from './dashboard/OfficerDashboard';
import SecretaryDashboard from './dashboard/SecretaryDashboard';
import PresidentDashboard from './dashboard/PresidentDashboard';
import AdminDashboard from './dashboard/AdminDashboard';

interface DashboardProps {
    userRole: UserRole;
    onNavigate: (view: ViewState) => void;
}

const Dashboard: React.FC<DashboardProps> = ({ userRole, onNavigate }) => {
  // Render specific dashboard based on role
  if (userRole === UserRole.MEMBER) {
      return <MemberDashboard />;
  }

  if (userRole === UserRole.OFFICER) {
    return <OfficerDashboard onNavigate={onNavigate} />;
  }

  if (userRole === UserRole.SECRETARY) {
    return <SecretaryDashboard onNavigate={onNavigate} />;
  }

  if (userRole === UserRole.PRESIDENT) {
    return <PresidentDashboard onNavigate={onNavigate} />;
  }

  if (userRole === UserRole.ADMIN) {
      return <AdminDashboard onNavigate={onNavigate} />;
  }

  return <div>Role not recognized</div>;
};

export default Dashboard;