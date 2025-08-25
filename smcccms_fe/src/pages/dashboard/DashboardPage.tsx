import { useState, useEffect } from 'react';
import { Layout } from '../../components/Layout';
import { User } from '../../lib/api';

export const DashboardPage = () => {
  const [user, setUser] = useState<User | null>(null);

  useEffect(() => {
    // In a real app, we'd fetch user data from an API endpoint
    // For now, we'll get it from the session storage or make a /me call
    const userData = sessionStorage.getItem('smcccms_current_user');
    if (userData) {
      setUser(JSON.parse(userData));
    }
  }, []);

  const getRoleDescription = (role: string): string => {
    const roleMap: Record<string, string> = {
      'RES': 'Resident',
      'SOL': 'Solicitor', 
      'CWS': 'Caseworker',
      'JDG': 'Judge'
    };
    return roleMap[role] || role;
  };

  const handleSignOut = () => {
    // Clear session storage and reload
    sessionStorage.clear();
    // In a real app, we'd call a sign out API endpoint
    window.location.href = '/login/gov-id';
  };

  return (
    <Layout>
      <div className="govuk-grid-row">
        <div className="govuk-grid-column-two-thirds">
          <h1 className="govuk-heading-xl">Dashboard</h1>
          
          {user ? (
            <div>
              <div className="govuk-panel govuk-panel--confirmation">
                <h2 className="govuk-panel__title">
                  Welcome, {user.firstName} {user.lastName}
                </h2>
                <div className="govuk-panel__body">
                  You are successfully signed in
                </div>
              </div>

              <dl className="govuk-summary-list">
                <div className="govuk-summary-list__row">
                  <dt className="govuk-summary-list__key">Government ID</dt>
                  <dd className="govuk-summary-list__value">{user.govId}</dd>
                </div>
                <div className="govuk-summary-list__row">
                  <dt className="govuk-summary-list__key">Name</dt>
                  <dd className="govuk-summary-list__value">{user.firstName} {user.lastName}</dd>
                </div>
                <div className="govuk-summary-list__row">
                  <dt className="govuk-summary-list__key">Roles</dt>
                  <dd className="govuk-summary-list__value">
                    {user.roles.map(role => getRoleDescription(role)).join(', ')}
                  </dd>
                </div>
              </dl>

              <h2 className="govuk-heading-m">Available Actions</h2>
              <p className="govuk-body">
                Role-specific functionality will be available here in future releases.
              </p>

              <div className="govuk-inset-text">
                <p className="govuk-body">
                  This is a demo dashboard. Full case management functionality 
                  will be implemented in subsequent milestones.
                </p>
              </div>

              <button
                className="govuk-button govuk-button--secondary"
                onClick={handleSignOut}
              >
                Sign out
              </button>
            </div>
          ) : (
            <div className="govuk-warning-text">
              <span className="govuk-warning-text__icon" aria-hidden="true">!</span>
              <strong className="govuk-warning-text__text">
                <span className="govuk-warning-text__assistive">Warning</span>
                Unable to load user information. Please sign in again.
              </strong>
            </div>
          )}
        </div>
      </div>
    </Layout>
  );
};