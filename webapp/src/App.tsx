import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { AuthProvider } from './contexts/AuthContext';
import { LanguageProvider } from './contexts/LanguageContext';
import { Layout } from './components/Layout';
import { ProtectedRoute } from './components/ProtectedRoute';
import { HomePage } from './pages/HomePage';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { CampaignsPage } from './pages/CampaignsPage';
import { CampaignDetailPage } from './pages/CampaignDetailPage';
import { CreateCampaignPage } from './pages/CreateCampaignPage';
import { EditCampaignPage } from './pages/EditCampaignPage';
import { DonatePage } from './pages/DonatePage';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
      staleTime: 5 * 60 * 1000,
    },
  },
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <LanguageProvider>
        <BrowserRouter>
          <AuthProvider>
            <Routes>
              <Route element={<Layout />}>
              <Route path="/" element={<HomePage />} />
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />

              <Route
                path="/campaigns"
                element={
                  <ProtectedRoute>
                    <CampaignsPage />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/campaigns/new"
                element={
                  <ProtectedRoute>
                    <CreateCampaignPage />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/campaigns/:id/edit"
                element={
                  <ProtectedRoute>
                    <EditCampaignPage />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/campaigns/:id/donate"
                element={
                  <ProtectedRoute>
                    <DonatePage />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/campaigns/:id"
                element={
                  <ProtectedRoute>
                    <CampaignDetailPage />
                  </ProtectedRoute>
                }
              />
            </Route>
          </Routes>
        </AuthProvider>
      </BrowserRouter>
      </LanguageProvider>

      <ReactQueryDevtools initialIsOpen={false} />
    </QueryClientProvider>
  );
}

export default App;
