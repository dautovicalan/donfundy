import apiClient from '../api/axios';
import type { DonorRequest, DonorResponse } from '../types';

export const donorService = {
  getAll: async (): Promise<DonorResponse[]> => {
    const response = await apiClient.get<DonorResponse[]>('/donors');
    return response.data;
  },

  getMe: async (): Promise<DonorResponse> => {
    const response = await apiClient.get<DonorResponse>('/donors/me');
    return response.data;
  },

  getById: async (id: number): Promise<DonorResponse> => {
    const response = await apiClient.get<DonorResponse>(`/donors/${id}`);
    return response.data;
  },

  getByUserId: async (userId: number): Promise<DonorResponse> => {
    const response = await apiClient.get<DonorResponse>(`/donors/user/${userId}`);
    return response.data;
  },

  create: async (data: DonorRequest): Promise<DonorResponse> => {
    const response = await apiClient.post<DonorResponse>('/donors', data);
    return response.data;
  },

  update: async (id: number, data: DonorRequest): Promise<DonorResponse> => {
    const response = await apiClient.put<DonorResponse>(`/donors/${id}`, data);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/donors/${id}`);
  },
};
