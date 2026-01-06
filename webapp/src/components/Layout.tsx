import { Link, Outlet } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { LanguageToggle } from './LanguageToggle';

export const Layout = () => {
  const { isAuthenticated, user, logout } = useAuth();

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <nav
        style={{
          backgroundColor: '#333',
          color: 'white',
          padding: '15px 20px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        <Link
          to="/"
          style={{
            color: 'white',
            textDecoration: 'none',
            fontSize: '24px',
            fontWeight: 'bold',
          }}
        >
          DonFundy
        </Link>

        <div style={{ display: 'flex', gap: '20px', alignItems: 'center' }}>
          <LanguageToggle />

          {isAuthenticated ? (
            <>
              <Link
                to="/campaigns"
                style={{
                  color: 'white',
                  textDecoration: 'none',
                }}
              >
                Campaigns
              </Link>

              <span style={{ color: '#ccc' }}>{user?.email}</span>

              <button
                onClick={logout}
                style={{
                  backgroundColor: '#dc3545',
                  color: 'white',
                  border: 'none',
                  padding: '8px 16px',
                  borderRadius: '4px',
                  cursor: 'pointer',
                }}
              >
                Logout
              </button>
            </>
          ) : (
            <>
              <Link
                to="/login"
                style={{
                  color: 'white',
                  textDecoration: 'none',
                }}
              >
                Login
              </Link>

              <Link
                to="/register"
                style={{
                  backgroundColor: '#28a745',
                  color: 'white',
                  textDecoration: 'none',
                  padding: '8px 16px',
                  borderRadius: '4px',
                }}
              >
                Register
              </Link>
            </>
          )}
        </div>
      </nav>

      <main style={{ flex: 1 }}>
        <Outlet />
      </main>

      <footer
        style={{
          backgroundColor: '#f8f9fa',
          padding: '20px',
          textAlign: 'center',
          color: '#666',
          borderTop: '1px solid #ddd',
        }}
      >
        <p>&copy; 2026 DonFundy. All rights reserved.</p>
      </footer>
    </div>
  );
};
