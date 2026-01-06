import { Link } from 'react-router-dom';
import { useCampaigns } from '../hooks/useCampaigns';
import { Status } from '../types';

export const CampaignsPage = () => {
  const { data: campaigns, isLoading, error } = useCampaigns();

  if (isLoading) {
    return <div style={{ padding: '20px' }}>Loading campaigns...</div>;
  }

  if (error) {
    return <div style={{ padding: '20px', color: 'red' }}>Error loading campaigns</div>;
  }

  return (
    <div style={{ padding: '20px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '20px' }}>
        <h1>Campaigns</h1>
        <Link
          to="/campaigns/new"
          style={{
            padding: '10px 20px',
            backgroundColor: '#007bff',
            color: 'white',
            textDecoration: 'none',
            borderRadius: '4px',
          }}
        >
          Create Campaign
        </Link>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '20px' }}>
        {campaigns?.map((campaign) => (
          <div
            key={campaign.id}
            style={{
              border: '1px solid #ddd',
              borderRadius: '8px',
              padding: '20px',
              backgroundColor: '#fff',
            }}
          >
            <h3>{campaign.name}</h3>
            <p style={{ color: '#666', marginBottom: '10px' }}>{campaign.description}</p>

            <div style={{ marginBottom: '10px' }}>
              <strong>Goal:</strong> ${campaign.goalAmount.toFixed(2)}
            </div>

            <div style={{ marginBottom: '10px' }}>
              <strong>Raised:</strong> ${campaign.raisedAmount.toFixed(2)}
            </div>

            <div style={{ marginBottom: '10px' }}>
              <div style={{ backgroundColor: '#e0e0e0', borderRadius: '4px', height: '20px' }}>
                <div
                  style={{
                    width: `${campaign.progressPercentage}%`,
                    backgroundColor: '#28a745',
                    height: '100%',
                    borderRadius: '4px',
                  }}
                />
              </div>
              <small>{campaign.progressPercentage.toFixed(1)}% funded</small>
            </div>

            <div style={{ marginBottom: '10px' }}>
              <span
                style={{
                  padding: '4px 8px',
                  borderRadius: '4px',
                  fontSize: '12px',
                  backgroundColor:
                    campaign.status === Status.ACTIVE
                      ? '#d4edda'
                      : campaign.status === Status.COMPLETED
                      ? '#cce5ff'
                      : '#f8d7da',
                }}
              >
                {campaign.status}
              </span>
            </div>

            <Link
              to={`/campaigns/${campaign.id}`}
              style={{
                display: 'inline-block',
                marginTop: '10px',
                padding: '8px 16px',
                backgroundColor: '#007bff',
                color: 'white',
                textDecoration: 'none',
                borderRadius: '4px',
              }}
            >
              View Details
            </Link>
          </div>
        ))}
      </div>

      {campaigns?.length === 0 && (
        <div style={{ textAlign: 'center', padding: '40px', color: '#666' }}>
          No campaigns found. Create your first campaign!
        </div>
      )}
    </div>
  );
};
