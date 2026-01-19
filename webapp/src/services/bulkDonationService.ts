import apiClient from '../api/axios';
import type { BulkDonationResult } from '../types';

export const bulkDonationService = {
  uploadCsv: async (file: File): Promise<BulkDonationResult> => {
    const formData = new FormData();
    formData.append('file', file);

    const response = await apiClient.post<BulkDonationResult>('/bulk-donations/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },
};
