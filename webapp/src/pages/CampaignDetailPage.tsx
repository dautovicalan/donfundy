import { useParams, Link } from 'react-router-dom';
import { useCampaign } from '../hooks/useCampaigns';
import { useDonations } from '../hooks/useDonations';
import { useAuth } from '../contexts/AuthContext';
import { Status } from '../types';

export const CampaignDetailPage = () => {
  const { id } = useParams<{ id: string }>();
  const campaignId = Number(id);
  const { user } = useAuth();

  const { data: campaign, isLoading: campaignLoading, error: campaignError } = useCampaign(campaignId);
  const { data: donations, isLoading: donationsLoading } = useDonations(campaignId);

  const isOwner = campaign?.createdByEmail && user?.email && campaign.createdByEmail === user.email;

  if (campaignLoading) {
    return <div style={{ padding: '20px' }}>Loading campaign...</div>;
  }

  if (campaignError || !campaign) {
    return <div style={{ padding: '20px', color: 'red' }}>Error loading campaign</div>;
  }

  return (
    <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
      <Link to="/campaigns" style={{ marginBottom: '20px', display: 'inline-block' }}>
        &larr; Back to Campaigns
      </Link>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
        <h1 style={{ margin: 0 }}>{campaign.name}</h1>
        {isOwner && (
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
            Edit Campaign
          </Link>
        )}
      </div>

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
        {campaign.status}
      </div>

      <p style={{ fontSize: '18px', marginBottom: '20px' }}>{campaign.description}</p>

      <div style={{ backgroundColor: '#f8f9fa', padding: '20px', borderRadius: '8px', marginBottom: '20px' }}>
        <h3>Campaign Progress</h3>

        <div style={{ marginBottom: '10px' }}>
          <strong>Goal:</strong> ${campaign.goalAmount.toFixed(2)}
        </div>

        <div style={{ marginBottom: '10px' }}>
          <strong>Raised:</strong> ${campaign.raisedAmount.toFixed(2)}
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
          <strong>Start Date:</strong> {new Date(campaign.startDate).toLocaleDateString()}
        </div>

        {campaign.endDate && (
          <div>
            <strong>End Date:</strong> {new Date(campaign.endDate).toLocaleDateString()}
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
          Make a Donation
        </Link>
      )}

      <h2>Recent Donations</h2>

      {donationsLoading ? (
        <div>Loading donations...</div>
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
          No donations yet. Be the first to support this campaign!
        </div>
      )}
    </div>
  );
};
