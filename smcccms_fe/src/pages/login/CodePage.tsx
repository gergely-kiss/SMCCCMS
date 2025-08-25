import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Layout } from '../../components/Layout';
import { authApi } from '../../lib/api';
import { getAuthData, clearAuthData } from '../../lib/auth';

export const CodePage = () => {
  const [code, setCode] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  // Auto-fill code if available from previous step
  useEffect(() => {
    const authData = getAuthData();
    if (authData && authData.verificationCode) {
      setCode(authData.verificationCode);
    }
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!code.trim() || code.length !== 6) {
      toast.error('Please enter a valid 6-digit code');
      return;
    }

    setLoading(true);
    try {
      const user = await authApi.verifyCode(code);
      
      // Store user data for dashboard
      sessionStorage.setItem('smcccms_current_user', JSON.stringify(user));
      
      // Clear temporary auth data
      clearAuthData();
      
      toast.success('Successfully signed in!');
      navigate('/dashboard');
    } catch (error) {
      console.error('Code verification failed:', error);
      toast.error('Invalid or expired verification code. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleCodeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value.replace(/\D/g, '').slice(0, 6);
    setCode(value);
  };

  return (
    <Layout>
      <div className="govuk-grid-row">
        <div className="govuk-grid-column-two-thirds">
          <h1 className="govuk-heading-xl">Enter your verification code</h1>
          
          <form onSubmit={handleSubmit} className="govuk-form">
            <div className="govuk-form-group">
              <label className="govuk-label govuk-label--m" htmlFor="code">
                6-digit verification code
              </label>
              <div className="govuk-hint">
                Enter the code from the notification
              </div>
              <input
                className="govuk-input govuk-input--width-10"
                id="code"
                name="code"
                type="text"
                inputMode="numeric"
                maxLength={6}
                value={code}
                onChange={handleCodeChange}
                style={{ 
                  fontSize: '2rem', 
                  letterSpacing: '0.5rem', 
                  textAlign: 'center',
                  fontFamily: 'monospace'
                }}
                required
              />
            </div>

            <button
              type="submit"
              className="govuk-button"
              data-module="govuk-button"
              disabled={loading || code.length !== 6}
            >
              {loading ? 'Verifying...' : 'Sign in'}
            </button>
          </form>

          <p className="govuk-body">
            <a href="/login/contact" className="govuk-link">
              Request a new code
            </a>
          </p>
        </div>
      </div>
    </Layout>
  );
};