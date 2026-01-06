import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useCreateCampaign } from '../hooks/useCampaigns';
import { Status } from '../types';

export const CreateCampaignPage = () => {
  const navigate = useNavigate();
  const createCampaign = useCreateCampaign();

  const [formData, setFormData] = useState({
    name: '',
    description: '',
    goalAmount: '',
    startDate: '',
    endDate: '',
    status: Status.PENDING,
  });

  const [error, setError] = useState('');

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

    // Validation
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
      const result = await createCampaign.mutateAsync({
        name: formData.name,
        description: formData.description || undefined,
        goalAmount: Number(formData.goalAmount),
        startDate: formData.startDate,
        endDate: formData.endDate || undefined,
        status: formData.status,
      });

      // Navigate to the created campaign
      navigate(`/campaigns/${result.id}`);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create campaign. Please try again.');
    }
  };

  return (
    <div style={{ padding: '20px', maxWidth: '600px', margin: '0 auto' }}>
      <Link to="/campaigns" style={{ marginBottom: '20px', display: 'inline-block' }}>
        &larr; Back to Campaigns
      </Link>

      <h1>Create New Campaign</h1>

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
            disabled={createCampaign.isPending}
            style={{
              flex: 1,
              padding: '12px 24px',
              fontSize: '16px',
              fontWeight: 'bold',
              backgroundColor: '#28a745',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: createCampaign.isPending ? 'not-allowed' : 'pointer',
            }}
          >
            {createCampaign.isPending ? 'Creating...' : 'Create Campaign'}
          </button>

          <button
            type="button"
            onClick={() => navigate('/campaigns')}
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
