import apiClient from '../api/axios';
import type { CampaignRequest, CampaignResponse, Status } from '../types';

export const campaignService = {
  getAll: async (status?: Status): Promise<CampaignResponse[]> => {
    const params = status ? { status } : {};
    const response = await apiClient.get<CampaignResponse[]>('/campaigns', { params });
    return response.data;
  },

  getMyCampaigns: async (): Promise<CampaignResponse[]> => {
    const response = await apiClient.get<CampaignResponse[]>('/campaigns/my-campaigns');
    return response.data;
  },

  getById: async (id: number): Promise<CampaignResponse> => {
    const response = await apiClient.get<CampaignResponse>(`/campaigns/${id}`);
    return response.data;
  },

  create: async (data: CampaignRequest): Promise<CampaignResponse> => {
    const response = await apiClient.post<CampaignResponse>('/campaigns', data);
    return response.data;
  },

  update: async (id: number, data: CampaignRequest): Promise<CampaignResponse> => {
    const response = await apiClient.put<CampaignResponse>(`/campaigns/${id}`, data);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/campaigns/${id}`);
  },
};
