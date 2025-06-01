import { JSX } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { Navigate, useParams } from 'react-router-dom';
import { Spin } from 'antd';

interface ProtectedRouteProps {
  children: JSX.Element;
}

const ProtectedRoute = ({ children }: ProtectedRouteProps) => {
  const { user, loading } = useAuth();
  const { moderatorId } = useParams<{ moderatorId: string }>();

  if (loading) {
    return <Spin></Spin>;
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (moderatorId && user.id !== parseInt(moderatorId)) {
    return <Navigate to="/" replace />;
  }

  return children;
};

export default ProtectedRoute;
