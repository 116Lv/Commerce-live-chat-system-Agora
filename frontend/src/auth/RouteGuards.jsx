import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from './AuthContext.jsx';

export function RequireUserAuth() {
  const { isUserAuthenticated } = useAuth();
  const location = useLocation();

  return isUserAuthenticated ? <Outlet /> : <Navigate to="/login" state={{ from: location }} replace />;
}

export function RequireAdminAuth() {
  const { isAdminAuthenticated } = useAuth();
  const location = useLocation();

  return isAdminAuthenticated ? <Outlet /> : <Navigate to="/admin/login" state={{ from: location }} replace />;
}
