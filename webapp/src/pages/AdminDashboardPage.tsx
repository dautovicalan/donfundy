import { useState, useRef } from 'react';
import { Navigate } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import { useAuth } from '../contexts/AuthContext';
import { useTranslation } from '../i18n/useTranslation';
import { useCampaigns } from '../hooks/useCampaigns';
import { useDonations } from '../hooks/useDonations';
import { useDonors } from '../hooks/useDonors';
import { bulkDonationService } from '../services/bulkDonationService';
import type { BulkDonationResult } from '../types';

export const AdminDashboardPage = () => {
  const { isAdmin, isLoading: authLoading } = useAuth();
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const { data: campaigns, isLoading: campaignsLoading } = useCampaigns();
  const { data: donations, isLoading: donationsLoading } = useDonations();
  const { data: donors, isLoading: donorsLoading } = useDonors();

  const [uploading, setUploading] = useState(false);
  const [uploadResult, setUploadResult] = useState<BulkDonationResult | null>(null);
  const [uploadError, setUploadError] = useState<string | null>(null);

  if (authLoading) {
    return <div style={{ padding: '20px' }}>{t.common.loading}</div>;
  }

  if (!isAdmin) {
    return <Navigate to="/campaigns" replace />;
  }

  const isLoading = campaignsLoading || donationsLoading || donorsLoading;

  const totalRaised = campaigns?.reduce((sum, c) => sum + c.raisedAmount, 0) || 0;
  const activeCampaigns = campaigns?.filter(c => c.status === 'ACTIVE').length || 0;

  const handleFileSelect = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    if (!file.name.toLowerCase().endsWith('.csv')) {
      setUploadError(t.admin.invalidFileType);
      return;
    }

    setUploading(true);
    setUploadResult(null);
    setUploadError(null);

    try {
      const result = await bulkDonationService.uploadCsv(file);
      setUploadResult(result);
      // Refresh all data after successful upload
      queryClient.invalidateQueries({ queryKey: ['donations'] });
      queryClient.invalidateQueries({ queryKey: ['campaigns'] });
      queryClient.invalidateQueries({ queryKey: ['donors'] });
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : t.admin.uploadFailed;
      setUploadError(errorMessage);
    } finally {
      setUploading(false);
      // Reset file input
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  const cardStyle: React.CSSProperties = {
    backgroundColor: '#fff',
    borderRadius: '8px',
    padding: '20px',
    boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
  };

  const statCardStyle: React.CSSProperties = {
    ...cardStyle,
    textAlign: 'center',
  };

  return (
    <div style={{ padding: '20px', maxWidth: '1200px', margin: '0 auto' }}>
      <h1 style={{ marginBottom: '20px' }}>{t.admin.title}</h1>

      {isLoading ? (
        <div>{t.common.loading}</div>
      ) : (
        <>
          {/* Stats Cards */}
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
            gap: '20px',
            marginBottom: '30px'
          }}>
            <div style={statCardStyle}>
              <h3 style={{ color: '#666', margin: '0 0 10px 0', fontSize: '14px' }}>
                {t.admin.totalCampaigns}
              </h3>
              <p style={{ fontSize: '32px', fontWeight: 'bold', margin: 0, color: '#007bff' }}>
                {campaigns?.length || 0}
              </p>
              <small style={{ color: '#28a745' }}>{activeCampaigns} {t.admin.active}</small>
            </div>

            <div style={statCardStyle}>
              <h3 style={{ color: '#666', margin: '0 0 10px 0', fontSize: '14px' }}>
                {t.admin.totalDonations}
              </h3>
              <p style={{ fontSize: '32px', fontWeight: 'bold', margin: 0, color: '#28a745' }}>
                {donations?.length || 0}
              </p>
            </div>

            <div style={statCardStyle}>
              <h3 style={{ color: '#666', margin: '0 0 10px 0', fontSize: '14px' }}>
                {t.admin.totalDonors}
              </h3>
              <p style={{ fontSize: '32px', fontWeight: 'bold', margin: 0, color: '#6f42c1' }}>
                {donors?.length || 0}
              </p>
            </div>

            <div style={statCardStyle}>
              <h3 style={{ color: '#666', margin: '0 0 10px 0', fontSize: '14px' }}>
                {t.admin.totalRaised}
              </h3>
              <p style={{ fontSize: '32px', fontWeight: 'bold', margin: 0, color: '#fd7e14' }}>
                ${totalRaised.toFixed(2)}
              </p>
            </div>
          </div>

          {/* Bulk Upload Section */}
          <div style={{ ...cardStyle, marginBottom: '30px' }}>
            <h2 style={{ marginTop: 0 }}>{t.admin.bulkUpload}</h2>
            <p style={{ color: '#666', marginBottom: '15px' }}>
              {t.admin.bulkUploadDescription}
            </p>
            <p style={{ color: '#888', fontSize: '14px', marginBottom: '15px' }}>
              {t.admin.csvFormat}
            </p>

            <div style={{ display: 'flex', gap: '10px', alignItems: 'center', flexWrap: 'wrap' }}>
              <input
                ref={fileInputRef}
                type="file"
                accept=".csv"
                onChange={handleFileSelect}
                disabled={uploading}
                style={{ display: 'none' }}
                id="csv-upload"
              />
              <label
                htmlFor="csv-upload"
                style={{
                  padding: '10px 20px',
                  backgroundColor: uploading ? '#ccc' : '#007bff',
                  color: 'white',
                  borderRadius: '4px',
                  cursor: uploading ? 'not-allowed' : 'pointer',
                  display: 'inline-block',
                }}
              >
                {uploading ? t.admin.uploading : t.admin.selectFile}
              </label>
            </div>

            {/* Upload Result */}
            {uploadResult && (
              <div style={{
                marginTop: '15px',
                padding: '15px',
                backgroundColor: uploadResult.failureCount === 0 ? '#d4edda' : '#fff3cd',
                borderRadius: '4px',
                border: `1px solid ${uploadResult.failureCount === 0 ? '#c3e6cb' : '#ffeeba'}`,
              }}>
                <h4 style={{ margin: '0 0 10px 0' }}>{t.admin.uploadComplete}</h4>
                <p style={{ margin: '5px 0' }}>
                  {t.admin.totalRows}: <strong>{uploadResult.totalRows}</strong>
                </p>
                <p style={{ margin: '5px 0', color: '#28a745' }}>
                  {t.admin.successCount}: <strong>{uploadResult.successCount}</strong>
                </p>
                {uploadResult.failureCount > 0 && (
                  <>
                    <p style={{ margin: '5px 0', color: '#dc3545' }}>
                      {t.admin.failureCount}: <strong>{uploadResult.failureCount}</strong>
                    </p>
                    <div style={{ marginTop: '10px' }}>
                      <strong>{t.admin.errors}:</strong>
                      <ul style={{ margin: '5px 0', paddingLeft: '20px' }}>
                        {uploadResult.errors.map((error, index) => (
                          <li key={index} style={{ color: '#dc3545' }}>{error}</li>
                        ))}
                      </ul>
                    </div>
                  </>
                )}
              </div>
            )}

            {/* Upload Error */}
            {uploadError && (
              <div style={{
                marginTop: '15px',
                padding: '15px',
                backgroundColor: '#f8d7da',
                borderRadius: '4px',
                border: '1px solid #f5c6cb',
                color: '#721c24',
              }}>
                {uploadError}
              </div>
            )}
          </div>

          {/* Recent Donations Table */}
          <div style={cardStyle}>
            <h2 style={{ marginTop: 0 }}>{t.admin.recentDonations}</h2>
            {donations && donations.length > 0 ? (
              <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                  <thead>
                    <tr style={{ backgroundColor: '#f8f9fa' }}>
                      <th style={{ padding: '12px', textAlign: 'left', borderBottom: '2px solid #dee2e6' }}>
                        {t.admin.donor}
                      </th>
                      <th style={{ padding: '12px', textAlign: 'left', borderBottom: '2px solid #dee2e6' }}>
                        {t.admin.campaign}
                      </th>
                      <th style={{ padding: '12px', textAlign: 'right', borderBottom: '2px solid #dee2e6' }}>
                        {t.admin.amount}
                      </th>
                      <th style={{ padding: '12px', textAlign: 'left', borderBottom: '2px solid #dee2e6' }}>
                        {t.admin.date}
                      </th>
                      <th style={{ padding: '12px', textAlign: 'left', borderBottom: '2px solid #dee2e6' }}>
                        {t.admin.paymentMethod}
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {donations.slice(0, 10).map((donation) => (
                      <tr key={donation.id} style={{ borderBottom: '1px solid #dee2e6' }}>
                        <td style={{ padding: '12px' }}>{donation.donorName}</td>
                        <td style={{ padding: '12px' }}>{donation.campaignName}</td>
                        <td style={{ padding: '12px', textAlign: 'right', fontWeight: 'bold', color: '#28a745' }}>
                          ${donation.amount.toFixed(2)}
                        </td>
                        <td style={{ padding: '12px' }}>{donation.donationDate}</td>
                        <td style={{ padding: '12px' }}>
                          <span style={{
                            padding: '4px 8px',
                            backgroundColor: '#e9ecef',
                            borderRadius: '4px',
                            fontSize: '12px',
                          }}>
                            {t.paymentMethods[donation.paymentMethod]}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <p style={{ color: '#666' }}>{t.admin.noDonations}</p>
            )}
          </div>
        </>
      )}
    </div>
  );
};
