import { ReactNode } from 'react';

interface LayoutProps {
  children: ReactNode;
}

export const Layout = ({ children }: LayoutProps) => {
  return (
    <div className="govuk-template">
      <header className="govuk-header bhcc-header" role="banner" data-module="govuk-header">
        <div className="govuk-header__container govuk-width-container">
          <div className="govuk-header__logo">
            <a href="/" className="govuk-header__link govuk-header__link--homepage">
              <span className="govuk-header__logotype">
                <span className="govuk-header__logotype-text">
                  Brighton & Hove City Council
                </span>
              </span>
            </a>
          </div>
        </div>
      </header>

      <div className="govuk-width-container">
        <main className="govuk-main-wrapper bhcc-content" id="main-content" role="main">
          {children}
        </main>
      </div>

      <footer className="govuk-footer" role="contentinfo">
        <div className="govuk-width-container">
          <div className="govuk-footer__meta">
            <div className="govuk-footer__meta-item govuk-footer__meta-item--grow">
              <span className="govuk-footer__licence-description">
                Small Claims Court Council Management System - Demo
              </span>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
};