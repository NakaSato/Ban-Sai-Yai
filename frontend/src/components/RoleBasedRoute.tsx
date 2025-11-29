import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAppSelector } from '@/hooks/redux';
import { PERMISSIONS } from '@/constants';
import { UserRole } from '@/types';

interface RoleBasedRouteProps {
  children: React.ReactNode;
  requiredRoles?: UserRole[];
  requiredPermissions?: string[];
  fallbackPath?: string;
}

export const RoleBasedRoute: React.FC<RoleBasedRouteProps> = ({
  children,
  requiredRoles,
  requiredPermissions,
  fallbackPath = '/dashboard',
}) => {
  const { user, isAuthenticated } = useAppSelector((state) => state.auth);

  if (!isAuthenticated || !user) {
    return <Navigate to="/login" replace />;
  }

  // Check role-based access
  if (requiredRoles && requiredRoles.length > 0) {
    const hasRequiredRole = requiredRoles.includes(user.role);
    if (!hasRequiredRole) {
      return <Navigate to={fallbackPath} replace />;
    }
  }

  // Check permission-based access
  if (requiredPermissions && requiredPermissions.length > 0) {
    const userPermissions = (PERMISSIONS[user.role as keyof typeof PERMISSIONS] || []) as string[];
    const hasRequiredPermissions = requiredPermissions.every(permission =>
      userPermissions.includes(permission)
    );
    if (!hasRequiredPermissions) {
      return <Navigate to={fallbackPath} replace />;
    }
  }

  return <>{children}</>;
};

// Higher-order component for role-based protection
export const withRoleProtection = (
  Component: React.ComponentType<any>,
  options: {
    requiredRoles?: UserRole[];
    requiredPermissions?: string[];
    fallbackPath?: string;
  }
) => {
  return (props: any) => (
    <RoleBasedRoute {...options}>
      <Component {...props} />
    </RoleBasedRoute>
  );
};

// Hook to check if user has specific permissions
export const usePermissions = () => {
  const { user } = useAppSelector((state) => state.auth);

  const hasPermission = (permission: string): boolean => {
    if (!user) return false;
    const userPermissions = (PERMISSIONS[user.role as keyof typeof PERMISSIONS] || []) as string[];
    return userPermissions.includes(permission);
  };

  const hasAnyPermission = (permissions: string[]): boolean => {
    if (!user) return false;
    const userPermissions = (PERMISSIONS[user.role as keyof typeof PERMISSIONS] || []) as string[];
    return permissions.some(permission => userPermissions.includes(permission));
  };

  const hasAllPermissions = (permissions: string[]): boolean => {
    if (!user) return false;
    const userPermissions = (PERMISSIONS[user.role as keyof typeof PERMISSIONS] || []) as string[];
    return permissions.every(permission => userPermissions.includes(permission));
  };

  const hasRole = (role: UserRole): boolean => {
    if (!user) return false;
    return user.role === role;
  };

  const isAtLeastRole = (minimumRole: UserRole): boolean => {
    if (!user) return false;
    
    const roleHierarchy = {
      [UserRole.MEMBER]: 1,
      [UserRole.OFFICER]: 2,
      [UserRole.SECRETARY]: 3,
      [UserRole.PRESIDENT]: 4,
    };
    
    return roleHierarchy[user.role] >= roleHierarchy[minimumRole];
  };

  return {
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    hasRole,
    isAtLeastRole,
    userRole: user?.role,
    userPermissions: user ? (PERMISSIONS[user.role as keyof typeof PERMISSIONS] || []) as string[] : [],
  };
};

export default RoleBasedRoute;
