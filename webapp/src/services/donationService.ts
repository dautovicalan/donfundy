import apiClient from '../api/axios';
import type { DonationRequest, DonationResponse } from '../types';

export const donationService = {
  getAll: async (campaignId?: number, donorId?: number): Promise<DonationResponse[]> => {
    const params: Record<string, number> = {};
    if (campaignId) params.campaignId = campaignId;
    if (donorId) params.donorId = donorId;

    const response = await apiClient.get<DonationResponse[]>('/donations', { params });
    return response.data;
  },

  getById: async (id: number): Promise<DonationResponse> => {
    const response = await apiClient.get<DonationResponse>(`/donations/${id}`);
    return response.data;
  },

  create: async (data: DonationRequest): Promise<DonationResponse> => {
    const response = await apiClient.post<DonationResponse>('/donations', data);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/donations/${id}`);
  },
};
