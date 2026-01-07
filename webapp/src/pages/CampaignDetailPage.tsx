import { useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useCampaign, useDeleteCampaign } from '../hooks/useCampaigns';
import { useDonations } from '../hooks/useDonations';
import { useAuth } from '../contexts/AuthContext';
import { useTranslation } from '../i18n/useTranslation';
import { Status } from '../types';

export const CampaignDetailPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const campaignId = Number(id);
  const { isAdmin } = useAuth();
  const { t } = useTranslation();

  const { data: campaign, isLoading: campaignLoading, error: campaignError } = useCampaign(campaignId);
  const { data: donations, isLoading: donationsLoading } = useDonations(campaignId);
  const deleteCampaign = useDeleteCampaign();

  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [deleteError, setDeleteError] = useState('');

  const handleDelete = async () => {
    try {
      await deleteCampaign.mutateAsync(campaignId);
      navigate('/campaigns');
    } catch (err: any) {
      setDeleteError(err.response?.data?.message || t.errors.deleteCampaignFailed);
      setShowDeleteConfirm(false);
    }
  };

  if (campaignLoading) {
    return <div style={{ padding: '20px' }}>{t.campaigns.loadingCampaign}</div>;
  }

  if (campaignError || !campaign) {
    return <div style={{ padding: '20px', color: 'red' }}>{t.errors.loadingCampaigns}</div>;
  }

  return (
    <div style={{ padding: '20px', maxWidth: '800px', width: '100%', margin: '0 auto' }}>
      <Link to="/campaigns" style={{ marginBottom: '20px', display: 'inline-block' }}>
        &larr; {t.campaigns.backToCampaigns}
      </Link>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px', flexWrap: 'wrap', gap: '10px' }}>
        <h1 style={{ margin: 0, fontSize: 'clamp(24px, 5vw, 32px)' }}>{campaign.name}</h1>
        {isAdmin && (
          <div className="campaign-actions" style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
            <Link
              to={`/campaigns/${campaign.id}/edit`}
              style={{
                padding: '8px 16px',
                backgroundColor: '#007bff',
                color: 'white',
                textDecoration: 'none',
                borderRadius: '4px',
                fontSize: '14px',
              }}
            >
              {t.campaigns.editCampaign}
            </Link>
            <button
              onClick={() => setShowDeleteConfirm(true)}
              style={{
                padding: '8px 16px',
                backgroundColor: '#dc3545',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                fontSize: '14px',
                cursor: 'pointer',
              }}
            >
              {t.campaigns.deleteCampaign}
            </button>
          </div>
        )}
      </div>

      {deleteError && (
        <div
          style={{
            padding: '12px',
            backgroundColor: '#f8d7da',
            color: '#721c24',
            border: '1px solid #f5c6cb',
            borderRadius: '4px',
            marginBottom: '20px',
          }}
        >
          {deleteError}
        </div>
      )}

      {showDeleteConfirm && (
        <div
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.5)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
          }}
          onClick={() => setShowDeleteConfirm(false)}
        >
          <div
            style={{
              backgroundColor: 'white',
              padding: '30px',
              borderRadius: '8px',
              maxWidth: '500px',
              width: '90%',
            }}
            onClick={(e) => e.stopPropagation()}
          >
            <h2 style={{ marginTop: 0 }}>{t.campaigns.confirmDelete}</h2>
            <p>{t.campaigns.deleteWarning}</p>
            <p style={{ fontWeight: 'bold', color: '#dc3545' }}>
              {t.campaigns.campaignLabel}: {campaign.name}
            </p>

            <div style={{ display: 'flex', gap: '10px', marginTop: '20px', flexWrap: 'wrap' }}>
              <button
                onClick={handleDelete}
                disabled={deleteCampaign.isPending}
                style={{
                  flex: 1,
                  padding: '12px 24px',
                  backgroundColor: '#dc3545',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  fontSize: '16px',
                  cursor: deleteCampaign.isPending ? 'not-allowed' : 'pointer',
                  fontWeight: 'bold',
                }}
              >
                {deleteCampaign.isPending ? t.campaigns.deleting : t.campaigns.yesDelete}
              </button>
              <button
                onClick={() => setShowDeleteConfirm(false)}
                disabled={deleteCampaign.isPending}
                style={{
                  flex: 1,
                  padding: '12px 24px',
                  backgroundColor: '#6c757d',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  fontSize: '16px',
                  cursor: deleteCampaign.isPending ? 'not-allowed' : 'pointer',
                }}
              >
                {t.common.cancel}
              </button>
            </div>
          </div>
        </div>
      )}

      <div
        style={{
          padding: '4px 8px',
          borderRadius: '4px',
          fontSize: '14px',
          display: 'inline-block',
          marginBottom: '20px',
          backgroundColor:
            campaign.status === Status.ACTIVE
              ? '#d4edda'
              : campaign.status === Status.COMPLETED
              ? '#cce5ff'
              : '#f8d7da',
        }}
      >
        {t.campaignStatus[campaign.status]}
      </div>

      <p style={{ fontSize: '18px', marginBottom: '20px' }}>{campaign.description}</p>

      <div style={{ backgroundColor: '#f8f9fa', padding: '20px', borderRadius: '8px', marginBottom: '20px' }}>
        <h3>{t.campaigns.campaignProgress}</h3>

        <div style={{ marginBottom: '10px' }}>
          <strong>{t.campaigns.goal}:</strong> ${campaign.goalAmount.toFixed(2)}
        </div>

        <div style={{ marginBottom: '10px' }}>
          <strong>{t.campaigns.raised}:</strong> ${campaign.raisedAmount.toFixed(2)}
        </div>

        <div style={{ marginBottom: '10px' }}>
          <div style={{ backgroundColor: '#e0e0e0', borderRadius: '4px', height: '30px' }}>
            <div
              style={{
                width: `${campaign.progressPercentage}%`,
                backgroundColor: '#28a745',
                height: '100%',
                borderRadius: '4px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'white',
                fontWeight: 'bold',
              }}
            >
              {campaign.progressPercentage.toFixed(1)}%
            </div>
          </div>
        </div>

        <div>
          <strong>{t.campaigns.startDate}:</strong> {new Date(campaign.startDate).toLocaleDateString()}
        </div>

        {campaign.endDate && (
          <div>
            <strong>{t.campaigns.endDate}:</strong> {new Date(campaign.endDate).toLocaleDateString()}
          </div>
        )}
      </div>

      {campaign.status === Status.ACTIVE && (
        <Link
          to={`/campaigns/${campaign.id}/donate`}
          style={{
            display: 'inline-block',
            padding: '12px 24px',
            backgroundColor: '#28a745',
            color: 'white',
            textDecoration: 'none',
            borderRadius: '4px',
            marginBottom: '30px',
          }}
        >
          {t.donations.makeADonation}
        </Link>
      )}

      <h2>{t.donations.recentDonations}</h2>

      {donationsLoading ? (
        <div>{t.donations.loadingDonations}</div>
      ) : donations && donations.length > 0 ? (
        <div>
          {donations.map((donation) => (
            <div
              key={donation.id}
              style={{
                border: '1px solid #ddd',
                borderRadius: '8px',
                padding: '15px',
                marginBottom: '10px',
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '5px' }}>
                <strong>{donation.donorName}</strong>
                <strong style={{ color: '#28a745' }}>${donation.amount.toFixed(2)}</strong>
              </div>

              <div style={{ color: '#666', fontSize: '14px', marginBottom: '5px' }}>
                {new Date(donation.donationDate).toLocaleDateString()}
              </div>

              {donation.message && (
                <div style={{ marginTop: '10px', fontStyle: 'italic' }}>
                  "{donation.message}"
                </div>
              )}
            </div>
          ))}
        </div>
      ) : (
        <div style={{ padding: '20px', textAlign: 'center', color: '#666' }}>
          {t.donations.noDonations}
        </div>
      )}
    </div>
  );
};
