import { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useCampaign } from '../hooks/useCampaigns';
import { useCurrentDonor } from '../hooks/useDonors';
import { useCreateDonation } from '../hooks/useDonations';
import { useTranslation } from '../i18n/useTranslation';
import { PaymentMethod, Status } from '../types';

export const DonatePage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const campaignId = Number(id);
  const { t } = useTranslation();

  const { data: campaign, isLoading: campaignLoading } = useCampaign(campaignId);
  const { data: donor, isLoading: donorLoading } = useCurrentDonor();
  const createDonation = useCreateDonation();

  const [formData, setFormData] = useState({
    amount: '',
    message: '',
    paymentMethod: PaymentMethod.CARD,
  });

  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

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
    setSuccess(false);

    if (!donor) {
      setError(t.validation.noDonorProfile);
      return;
    }

    if (!formData.amount || Number(formData.amount) <= 0) {
      setError(t.validation.amountRequired);
      return;
    }

    try {
      await createDonation.mutateAsync({
        campaignId,
        donorId: donor.id,
        amount: Number(formData.amount),
        message: formData.message || undefined,
        paymentMethod: formData.paymentMethod,
      });

      setSuccess(true);
      setFormData({
        amount: '',
        message: '',
        paymentMethod: PaymentMethod.CARD,
      });

      setTimeout(() => {
        navigate(`/campaigns/${campaignId}`);
      }, 2000);
    } catch (err: any) {
      setError(err.response?.data?.message || t.errors.donationFailed);
    }
  };

  if (campaignLoading || donorLoading) {
    return <div style={{ padding: '20px' }}>{t.common.loading}</div>;
  }

  if (!campaign) {
    return <div style={{ padding: '20px', color: 'red' }}>{t.errors.campaignNotFound}</div>;
  }

  if (campaign.status !== Status.ACTIVE) {
    return (
      <div style={{ padding: '20px', maxWidth: '600px', margin: '0 auto' }}>
        <Link to={`/campaigns/${campaignId}`} style={{ marginBottom: '20px', display: 'inline-block' }}>
          &larr; {t.campaigns.backToCampaign}
        </Link>
        <div
          style={{
            padding: '20px',
            backgroundColor: '#f8d7da',
            color: '#721c24',
            border: '1px solid #f5c6cb',
            borderRadius: '4px',
            textAlign: 'center',
          }}
        >
          {t.donations.notAccepting}
        </div>
      </div>
    );
  }

  return (
    <div style={{ padding: '20px', maxWidth: '600px', width: '100%', margin: '0 auto' }}>
      <Link to={`/campaigns/${campaignId}`} style={{ marginBottom: '20px', display: 'inline-block' }}>
        &larr; {t.campaigns.backToCampaign}
      </Link>

      <h1 style={{ fontSize: 'clamp(24px, 5vw, 32px)' }}>{t.donations.makeADonation}</h1>

      <div
        style={{
          backgroundColor: '#f8f9fa',
          padding: '20px',
          borderRadius: '8px',
          marginBottom: '30px',
        }}
      >
        <h3 style={{ marginTop: 0 }}>{campaign.name}</h3>
        <p style={{ marginBottom: '10px' }}>{campaign.description}</p>

        <div style={{ marginTop: '15px' }}>
          <strong>{t.campaigns.goal}:</strong> ${campaign.goalAmount.toFixed(2)}
        </div>
        <div>
          <strong>{t.campaigns.raised}:</strong> ${campaign.raisedAmount.toFixed(2)} ({campaign.progressPercentage.toFixed(1)}%)
        </div>

        {campaign.goalAmount > campaign.raisedAmount && (
          <div style={{ marginTop: '10px', color: '#666' }}>
            <strong>{t.campaigns.remaining}:</strong> ${(campaign.goalAmount - campaign.raisedAmount).toFixed(2)}
          </div>
        )}
      </div>

      {success ? (
        <div
          style={{
            padding: '20px',
            backgroundColor: '#d4edda',
            color: '#155724',
            border: '1px solid #c3e6cb',
            borderRadius: '4px',
            textAlign: 'center',
            marginBottom: '20px',
          }}
        >
          <h3 style={{ marginTop: 0 }}>{t.donations.thankYou}</h3>
          <p>{t.donations.successMessage}</p>
          <p>{t.donations.redirecting}</p>
        </div>
      ) : (
        <form onSubmit={handleSubmit}>
          <div style={{ marginBottom: '20px' }}>
            <label htmlFor="amount" style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold' }}>
              {t.donations.amount} *
            </label>
            <input
              type="number"
              id="amount"
              name="amount"
              value={formData.amount}
              onChange={handleChange}
              required
              min="0.01"
              step="0.01"
              style={{
                width: '100%',
                padding: '10px',
                fontSize: '16px',
                border: '1px solid #ddd',
                borderRadius: '4px',
              }}
              placeholder={t.donations.enterAmount}
            />
          </div>

          <div style={{ marginBottom: '20px' }}>
            <label htmlFor="paymentMethod" style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold' }}>
              {t.donations.paymentMethod} *
            </label>
            <select
              id="paymentMethod"
              name="paymentMethod"
              value={formData.paymentMethod}
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
              <option value={PaymentMethod.CARD}>{t.paymentMethods.CARD}</option>
              <option value={PaymentMethod.BANK_TRANSFER}>{t.paymentMethods.BANK_TRANSFER}</option>
              <option value={PaymentMethod.PAYPAL}>{t.paymentMethods.PAYPAL}</option>
            </select>
          </div>

          <div style={{ marginBottom: '20px' }}>
            <label htmlFor="message" style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold' }}>
              {t.donations.message}
            </label>
            <textarea
              id="message"
              name="message"
              value={formData.message}
              onChange={handleChange}
              rows={4}
              style={{
                width: '100%',
                padding: '10px',
                fontSize: '16px',
                border: '1px solid #ddd',
                borderRadius: '4px',
                fontFamily: 'inherit',
              }}
              placeholder={t.donations.addMessage}
            />
          </div>

          {donor && (
            <div
              style={{
                backgroundColor: '#e7f3ff',
                padding: '15px',
                borderRadius: '4px',
                marginBottom: '20px',
              }}
            >
              <div style={{ fontSize: '14px', color: '#666', marginBottom: '5px' }}>{t.donations.donatingAs}</div>
              <div style={{ fontWeight: 'bold' }}>
                {donor.firstName} {donor.lastName}
              </div>
              <div style={{ fontSize: '14px', color: '#666' }}>{donor.email}</div>
            </div>
          )}

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

          <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
            <button
              type="submit"
              disabled={createDonation.isPending}
              style={{
                flex: 1,
                padding: '12px 24px',
                fontSize: '16px',
                fontWeight: 'bold',
                backgroundColor: '#28a745',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: createDonation.isPending ? 'not-allowed' : 'pointer',
              }}
            >
              {createDonation.isPending ? t.donations.processing : t.donations.donateNow}
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
              {t.common.cancel}
            </button>
          </div>
        </form>
      )}
    </div>
  );
};
