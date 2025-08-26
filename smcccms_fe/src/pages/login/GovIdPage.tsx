import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Layout } from '../../components/Layout';
import { authApi } from '../../lib/api';
import { storeAuthData, clearAuthData } from '../../lib/auth';

export const GovIdPage = () => {
  const [govId, setGovId] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!govId.trim()) return;

    setLoading(true);
    try {
      const user = await authApi.verifyId(govId);
      
      // Store user data for the auth flow
      clearAuthData();
      storeAuthData({
        user,
        govIdVerified: true,
        govId: govId
      });

      toast.success(`Welcome, ${user.firstName}!`);
      navigate('/login/contact');
    } catch (error) {
      console.error('Gov ID verification failed:', error);
      toast.error('Government ID verification failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Layout>
      <div className="govuk-grid-row">
        <div className="govuk-grid-column-two-thirds">
          <h1 className="govuk-heading-xl">Sign in to SMCCCMS</h1>
          
          <form onSubmit={handleSubmit} className="govuk-form">
            <div className="govuk-form-group">
              <label className="govuk-label govuk-label--m" htmlFor="gov-id">
                Government ID
              </label>
              <div className="govuk-hint">
                Enter your government ID in the format ID-UK-001
              </div>
              <input
                className="govuk-input"
                id="gov-id"
                name="gov-id"
                type="text"
                placeholder="ID-UK-001"
                value={govId}
                onChange={(e) => setGovId(e.target.value)}
                pattern="^ID-UK-\d{3}$"
                required
              />
            </div>

            <button
              type="submit"
              className="govuk-button"
              data-module="govuk-button"
              disabled={loading}
            >
              {loading ? 'Verifying...' : 'Continue'}
            </button>
          </form>

          <details className="govuk-details" data-module="govuk-details">
            <summary className="govuk-details__summary">
              <span className="govuk-details__summary-text">
                Demo credentials
              </span>
            </summary>
            <div className="govuk-details__text">
              <p className="govuk-body">Use any of these demo IDs:</p>
              <ul className="govuk-list govuk-list--bullet">
                <li><strong>ID-UK-001</strong> - Resident (RES)</li>
                <li><strong>ID-UK-007</strong> - Solicitor (SOL)</li>
                <li><strong>ID-UK-011</strong> - Caseworker (CWS)</li>
                <li><strong>ID-UK-017</strong> - Judge (JDG)</li>
              </ul>
            </div>
          </details>
        </div>
      </div>
    </Layout>
  );
};