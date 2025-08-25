import { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { hasSessionCookie, canAccessContactPage, canAccessCodePage } from '../lib/auth';

interface AuthGuardProps {
  children: ReactNode;
  requireAuth?: boolean;
  requireGovIdStep?: boolean;
  requireContactStep?: boolean;
}

export const AuthGuard = ({ 
  children, 
  requireAuth = false,
  requireGovIdStep = false,
  requireContactStep = false 
}: AuthGuardProps) => {
  // Check if user is authenticated for protected routes
  if (requireAuth && !hasSessionCookie()) {
    return <Navigate to="/login/gov-id" replace />;
  }

  // Check if Gov ID step was completed for contact page
  if (requireGovIdStep && !canAccessContactPage()) {
    return <Navigate to="/login/gov-id" replace />;
  }

  // Check if contact step was completed for code page
  if (requireContactStep && !canAccessCodePage()) {
    return <Navigate to="/login/gov-id" replace />;
  }

  // If authenticated but trying to access login pages, redirect to dashboard
  if (!requireAuth && !requireGovIdStep && !requireContactStep && hasSessionCookie()) {
    return <Navigate to="/dashboard" replace />;
  }

  return <>{children}</>;
};