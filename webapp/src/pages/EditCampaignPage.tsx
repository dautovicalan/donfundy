import { useState, useEffect } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { useCampaign, useUpdateCampaign } from '../hooks/useCampaigns';
import { Status } from '../types';

export const EditCampaignPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const campaignId = Number(id);

  const { data: campaign, isLoading } = useCampaign(campaignId);
  const updateCampaign = useUpdateCampaign();

  const [formData, setFormData] = useState({
    name: '',
    description: '',
    goalAmount: '',
    startDate: '',
    endDate: '',
    status: Status.PENDING,
  });

  const [error, setError] = useState('');

  useEffect(() => {
    if (campaign) {
      setFormData({
        name: campaign.name,
        description: campaign.description || '',
        goalAmount: campaign.goalAmount.toString(),
        startDate: campaign.startDate,
        endDate: campaign.endDate || '',
        status: campaign.status,
      });
    }
  }, [campaign]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!formData.name.trim()) {
      setError('Campaign name is required');
      return;
    }

    if (!formData.goalAmount || Number(formData.goalAmount) <= 0) {
      setError('Goal amount must be greater than 0');
      return;
    }

    if (!formData.startDate) {
      setError('Start date is required');
      return;
    }

    if (formData.endDate && formData.endDate < formData.startDate) {
      setError('End date must be after start date');
      return;
    }

    try {
      await updateCampaign.mutateAsync({
        id: campaignId,
        data: {
          name: formData.name,
          description: formData.description || undefined,
          goalAmount: Number(formData.goalAmount),
          startDate: formData.startDate,
          endDate: formData.endDate || undefined,
          status: formData.status,
        },
      });

      navigate(`/campaigns/${campaignId}`);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to update campaign. Please try again.');
    }
  };

  if (isLoading) {
    return <div style={{ padding: '20px' }}>Loading campaign...</div>;
  }

  if (!campaign) {
    return <div style={{ padding: '20px' }}>Campaign not found</div>;
  }

  return (
    <div style={{ padding: '20px', maxWidth: '600px', margin: '0 auto' }}>
      <Link to={`/campaigns/${campaignId}`} style={{ marginBottom: '20px', display: 'inline-block' }}>
        &larr; Back to Campaign
      </Link>

      <h1>Edit Campaign</h1>

      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: '20px' }}>
          <label htmlFor="name" style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold' }}>
            Campaign Name *
          </label>
          <input
            type="text"
            id="name"
            name="name"
            value={formData.name}
            onChange={handleChange}
            required
            style={{
              width: '100%',
              padding: '10px',
              fontSize: '16px',
              border: '1px solid #ddd',
              borderRadius: '4px',
            }}
          />
        </div>

        <div style={{ marginBottom: '20px' }}>
          <label htmlFor="description" style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold' }}>
            Description
          </label>
          <textarea
            id="description"
            name="description"
            value={formData.description}
            onChange={handleChange}
            rows={5}
            style={{
              width: '100%',
              padding: '10px',
              fontSize: '16px',
              border: '1px solid #ddd',
              borderRadius: '4px',
              fontFamily: 'inherit',
            }}
          />
        </div>

        <div style={{ marginBottom: '20px' }}>
          <label htmlFor="goalAmount" style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold' }}>
            Goal Amount ($) *
          </label>
          <input
            type="number"
            id="goalAmount"
            name="goalAmount"
            value={formData.goalAmount}
            onChange={handleChange}
            required
            min="0"
            step="0.01"
            style={{
              width: '100%',
              padding: '10px',
              fontSize: '16px',
              border: '1px solid #ddd',
              borderRadius: '4px',
            }}
          />
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginBottom: '20px' }}>
          <div>
            <label htmlFor="startDate" style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold' }}>
              Start Date *
            </label>
            <input
              type="date"
              id="startDate"
              name="startDate"
              value={formData.startDate}
              onChange={handleChange}
              required
              style={{
                width: '100%',
                padding: '10px',
                fontSize: '16px',
                border: '1px solid #ddd',
                borderRadius: '4px',
              }}
            />
          </div>

          <div>
            <label htmlFor="endDate" style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold' }}>
              End Date
            </label>
            <input
              type="date"
              id="endDate"
              name="endDate"
              value={formData.endDate}
              onChange={handleChange}
              style={{
                width: '100%',
                padding: '10px',
                fontSize: '16px',
                border: '1px solid #ddd',
                borderRadius: '4px',
              }}
            />
          </div>
        </div>

        <div style={{ marginBottom: '20px' }}>
          <label htmlFor="status" style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold' }}>
            Status *
          </label>
          <select
            id="status"
            name="status"
            value={formData.status}
            onChange={handleChange}
            required
            style={{
              width: '100%',
              padding: '10px',
              fontSize: '16px',
              border: '1px solid #ddd',
              borderRadius: '4px',
            }}
          >
            <option value={Status.PENDING}>Pending</option>
            <option value={Status.ACTIVE}>Active</option>
            <option value={Status.COMPLETED}>Completed</option>
            <option value={Status.CANCELLED}>Cancelled</option>
          </select>
        </div>

        {error && (
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
            {error}
          </div>
        )}

        <div style={{ display: 'flex', gap: '10px' }}>
          <button
            type="submit"
            disabled={updateCampaign.isPending}
            style={{
              flex: 1,
              padding: '12px 24px',
              fontSize: '16px',
              fontWeight: 'bold',
              backgroundColor: '#007bff',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: updateCampaign.isPending ? 'not-allowed' : 'pointer',
            }}
          >
            {updateCampaign.isPending ? 'Updating...' : 'Update Campaign'}
          </button>

          <button
            type="button"
            onClick={() => navigate(`/campaigns/${campaignId}`)}
            style={{
              padding: '12px 24px',
              fontSize: '16px',
              backgroundColor: '#6c757d',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
            }}
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
};
