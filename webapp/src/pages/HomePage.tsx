import { Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useTranslation } from '../i18n/useTranslation';

export const HomePage = () => {
  const { isAuthenticated } = useAuth();
  const { t } = useTranslation();

  return (
    <div style={{ textAlign: 'center', padding: '20px', maxWidth: '100%' }}>
      <h1 style={{ fontSize: 'clamp(28px, 8vw, 48px)', marginBottom: '20px' }}>{t.home.welcome}</h1>
      <p style={{ fontSize: 'clamp(16px, 4vw, 20px)', color: '#666', marginBottom: '40px' }}>
        {t.home.tagline}
      </p>

      {!isAuthenticated ? (
        <div style={{ display: 'flex', gap: '10px', justifyContent: 'center', flexWrap: 'wrap', padding: '0 10px' }}>
          <Link
            to="/register"
            style={{
              display: 'inline-block',
              padding: '15px 30px',
              backgroundColor: '#28a745',
              color: 'white',
              textDecoration: 'none',
              borderRadius: '4px',
              fontSize: 'clamp(14px, 3vw, 18px)',
              minWidth: '140px',
              textAlign: 'center',
            }}
          >
            {t.home.getStarted}
          </Link>

          <Link
            to="/login"
            style={{
              display: 'inline-block',
              padding: '15px 30px',
              backgroundColor: '#007bff',
              color: 'white',
              textDecoration: 'none',
              borderRadius: '4px',
              fontSize: 'clamp(14px, 3vw, 18px)',
              minWidth: '140px',
              textAlign: 'center',
            }}
          >
            {t.nav.login}
          </Link>
        </div>
      ) : (
        <Link
          to="/campaigns"
          style={{
            display: 'inline-block',
            padding: '15px 30px',
            backgroundColor: '#007bff',
            color: 'white',
            textDecoration: 'none',
            borderRadius: '4px',
            fontSize: 'clamp(14px, 3vw, 18px)',
            minWidth: '140px',
            textAlign: 'center',
          }}
        >
          {t.home.viewCampaigns}
        </Link>
      )}

      <div
        className="home-features"
        style={{
          marginTop: '60px',
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
          gap: '30px',
          maxWidth: '900px',
          margin: '60px auto 0',
          padding: '0 20px',
        }}
      >
        <div style={{ padding: '20px' }}>
          <h3>{t.home.feature1Title}</h3>
          <p style={{ color: '#666' }}>
            {t.home.feature1Description}
          </p>
        </div>

        <div style={{ padding: '20px' }}>
          <h3>{t.home.feature2Title}</h3>
          <p style={{ color: '#666' }}>
            {t.home.feature2Description}
          </p>
        </div>

        <div style={{ padding: '20px' }}>
          <h3>{t.home.feature3Title}</h3>
          <p style={{ color: '#666' }}>
            {t.home.feature3Description}
          </p>
        </div>
      </div>
    </div>
  );
};
