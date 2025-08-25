import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthGuard } from './components/AuthGuard';
import { GovIdPage } from './pages/login/GovIdPage';
import { ContactPage } from './pages/login/ContactPage';
import { CodePage } from './pages/login/CodePage';
import { DashboardPage } from './pages/dashboard/DashboardPage';
import './styles/govuk.scss';

function App() {
  return (
    <Router>
      <div className="App">
        <Routes>
          {/* Redirect root to gov-id login */}
          <Route path="/" element={<Navigate to="/login/gov-id" replace />} />
          
          {/* Login flow routes with guards */}
          <Route 
            path="/login/gov-id" 
            element={
              <AuthGuard>
                <GovIdPage />
              </AuthGuard>
            } 
          />
          
          <Route 
            path="/login/contact" 
            element={
              <AuthGuard requireGovIdStep>
                <ContactPage />
              </AuthGuard>
            } 
          />
          
          <Route 
            path="/login/code" 
            element={
              <AuthGuard requireContactStep>
                <CodePage />
              </AuthGuard>
            } 
          />
          
          {/* Protected dashboard route */}
          <Route 
            path="/dashboard" 
            element={
              <AuthGuard requireAuth>
                <DashboardPage />
              </AuthGuard>
            } 
          />
          
          {/* Catch all redirect */}
          <Route path="*" element={<Navigate to="/login/gov-id" replace />} />
        </Routes>
        
        <Toaster 
          position="top-right"
          toastOptions={{
            duration: 5000,
            style: {
              background: '#00703c',
              color: 'white',
              fontSize: '16px',
              fontWeight: 'bold',
            },
          }}
        />
      </div>
    </Router>
  );
}

export default App;
