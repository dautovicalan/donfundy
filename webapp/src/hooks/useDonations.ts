import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { donationService } from '../services/donationService';
import type {DonationRequest} from '../types';

export const useDonations = (campaignId?: number, donorId?: number) => {
  return useQuery({
    queryKey: ['donations', campaignId, donorId],
    queryFn: () => donationService.getAll(campaignId, donorId),
  });
};

export const useDonation = (id: number) => {
  return useQuery({
    queryKey: ['donation', id],
    queryFn: () => donationService.getById(id),
    enabled: !!id,
  });
};

export const useCreateDonation = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: DonationRequest) => donationService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['donations'] });
      queryClient.invalidateQueries({ queryKey: ['campaigns'] });
    },
  });
};

export const useDeleteDonation = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => donationService.delete(id),
    onSuccess: (_data, id) => {
      queryClient.invalidateQueries({ queryKey: ['donations'] });
      queryClient.invalidateQueries({ queryKey: ['donation', id] });
      queryClient.invalidateQueries({ queryKey: ['campaigns'] });
    },
  });
};
