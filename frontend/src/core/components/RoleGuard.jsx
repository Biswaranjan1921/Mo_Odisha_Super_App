import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const RoleGuard = ({ children, allowedRoles }) => {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-stone-900 text-amber-400">
        <div className="animate-spin rounded-full h-12 w-12 border-4 border-amber-400 border-t-transparent"></div>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/" replace />;
  }

  if (allowedRoles && allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-stone-100 p-4">
        <div className="bg-white p-8 rounded-2xl shadow-xl max-w-md text-center border border-red-200">
          <div className="text-4xl mb-4">⛔</div>
          <h2 className="text-2xl font-bold text-stone-900 mb-2">Access Restricted</h2>
          <p className="text-stone-600 text-sm mb-6">
            Your account role (<span className="font-mono text-amber-700 font-bold">{user.role}</span>) does not have authorization to view this workspace.
          </p>
          <a
            href="/"
            className="inline-block px-6 py-2.5 rounded-xl bg-amber-600 text-white font-bold hover:bg-amber-700 transition-all"
          >
            Return to Home Portal
          </a>
        </div>
      </div>
    );
  }

  return children;
};

export default RoleGuard;
