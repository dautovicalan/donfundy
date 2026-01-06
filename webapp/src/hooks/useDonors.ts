import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { donorService } from '../services/donorService';
import type {DonorRequest} from '../types';

export const useDonors = () => {
  return useQuery({
    queryKey: ['donors'],
    queryFn: () => donorService.getAll(),
  });
};

export const useCurrentDonor = () => {
  return useQuery({
    queryKey: ['donor', 'me'],
    queryFn: () => donorService.getMe(),
  });
};

export const useDonor = (id: number) => {
  return useQuery({
    queryKey: ['donor', id],
    queryFn: () => donorService.getById(id),
    enabled: !!id,
  });
};

export const useDonorByUserId = (userId: number) => {
  return useQuery({
    queryKey: ['donor', 'user', userId],
    queryFn: () => donorService.getByUserId(userId),
    enabled: !!userId,
  });
};

export const useCreateDonor = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: DonorRequest) => donorService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['donors'] });
    },
  });
};

export const useUpdateDonor = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: DonorRequest }) =>
      donorService.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['donors'] });
    },
  });
};

export const useDeleteDonor = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => donorService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['donors'] });
    },
  });
};
