import { Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export const HomePage = () => {
  const { isAuthenticated } = useAuth();

  return (
    <div style={{ textAlign: 'center', padding: '50px 20px' }}>
      <h1 style={{ fontSize: '48px', marginBottom: '20px' }}>Welcome to DonFundy</h1>
      <p style={{ fontSize: '20px', color: '#666', marginBottom: '40px' }}>
        Help make a difference by supporting campaigns that matter
      </p>

      {!isAuthenticated ? (
        <div>
          <Link
            to="/register"
            style={{
              display: 'inline-block',
              padding: '15px 30px',
              backgroundColor: '#28a745',
              color: 'white',
              textDecoration: 'none',
              borderRadius: '4px',
              marginRight: '10px',
              fontSize: '18px',
            }}
          >
            Get Started
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
              fontSize: '18px',
            }}
          >
            Login
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
            fontSize: '18px',
          }}
        >
          View Campaigns
        </Link>
      )}

      <div
        style={{
          marginTop: '60px',
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
          gap: '30px',
          maxWidth: '900px',
          margin: '60px auto 0',
        }}
      >
        <div style={{ padding: '20px' }}>
          <h3>Support Causes</h3>
          <p style={{ color: '#666' }}>
            Browse and support various fundraising campaigns
          </p>
        </div>

        <div style={{ padding: '20px' }}>
          <h3>Create Campaigns</h3>
          <p style={{ color: '#666' }}>
            Start your own campaign and reach your goals
          </p>
        </div>

        <div style={{ padding: '20px' }}>
          <h3>Track Progress</h3>
          <p style={{ color: '#666' }}>
            Monitor campaign progress and donations in real-time
          </p>
        </div>
      </div>
    </div>
  );
};
