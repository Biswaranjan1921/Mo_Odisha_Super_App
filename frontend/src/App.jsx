import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './core/context/AuthContext';
import WelcomeScreen from './features/home/WelcomeScreen';
import HomeDashboard from './features/home/HomeDashboard';
import EmergencyScreen from './features/emergency/EmergencyScreen';
import HealthcareScreen from './features/healthcare/HealthcareScreen';
import ShoppingScreen from './features/shopping/ShoppingScreen';
import TransportScreen from './features/transport/TransportScreen';
import TourismScreen from './features/tourism/TourismScreen';
import EventsScreen from './features/events/EventsScreen';
import DeliveryScreen from './features/delivery/DeliveryScreen';
import TrustScreen from './features/trust/TrustScreen';

// A wrapper component to guard routes that require active session tokens
const ProtectedRoute = ({ children }) => {
  const { token } = useAuth();
  
  if (!token) {
    return <Navigate to="/" replace />;
  }
  
  return children;
};

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<WelcomeScreen />} />
          <Route 
            path="/dashboard" 
            element={
              <ProtectedRoute>
                <HomeDashboard />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/emergency" 
            element={
              <ProtectedRoute>
                <EmergencyScreen />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/healthcare" 
            element={
              <ProtectedRoute>
                <HealthcareScreen />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/shopping" 
            element={
              <ProtectedRoute>
                <ShoppingScreen />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/transport" 
            element={
              <ProtectedRoute>
                <TransportScreen />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/tourism" 
            element={
              <ProtectedRoute>
                <TourismScreen />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/events" 
            element={
              <ProtectedRoute>
                <EventsScreen />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/delivery" 
            element={
              <ProtectedRoute>
                <DeliveryScreen />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/trust" 
            element={
              <ProtectedRoute>
                <TrustScreen />
              </ProtectedRoute>
            } 
          />
          {/* Catch all route - redirects to welcome */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
