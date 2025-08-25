import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Layout } from '../../components/Layout';
import { authApi } from '../../lib/api';
import { storeAuthData, getAuthData } from '../../lib/auth';

export const ContactPage = () => {
  const [contact, setContact] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    setLoading(true);
    try {
      const result = await authApi.requestCode(contact || 'demo@example.com');
      
      // Store the code and update auth data
      const authData = getAuthData();
      storeAuthData({
        ...authData,
        codeRequested: true,
        verificationCode: result.code,
        contact
      });

      // Show toast with the verification code
      const toastId = toast.success(
        <div>
          <strong>Verification code: {result.code}</strong>
          <br />
          <button 
            className="govuk-link" 
            style={{ border: 'none', background: 'none', color: 'white', textDecoration: 'underline' }}
            onClick={() => {
              toast.dismiss(toastId);
              navigate('/login/code');
            }}
          >
            Click here to enter code
          </button>
        </div>,
        { 
          duration: 10000,
          icon: '📱'
        }
      );

    } catch (error) {
      console.error('Code request failed:', error);
      toast.error('Failed to request verification code. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Layout>
      <div className="govuk-grid-row">
        <div className="govuk-grid-column-two-thirds">
          <h1 className="govuk-heading-xl">Enter your contact details</h1>
          
          <form onSubmit={handleSubmit} className="govuk-form">
            <div className="govuk-form-group">
              <label className="govuk-label govuk-label--m" htmlFor="contact">
                Mobile number or email address
              </label>
              <div className="govuk-hint">
                We'll send you a verification code (demo mode - any value accepted)
              </div>
              <input
                className="govuk-input"
                id="contact"
                name="contact"
                type="text"
                placeholder="your.email@example.com or 07700 900123"
                value={contact}
                onChange={(e) => setContact(e.target.value)}
              />
            </div>

            <button
              type="submit"
              className="govuk-button"
              data-module="govuk-button"
              disabled={loading}
            >
              {loading ? 'Sending...' : 'Request login code'}
            </button>
          </form>

          <div className="govuk-inset-text">
            <p className="govuk-body">
              <strong>Demo mode:</strong> The verification code will be shown in a notification 
              instead of being sent to your contact details.
            </p>
          </div>
        </div>
      </div>
    </Layout>
  );
};