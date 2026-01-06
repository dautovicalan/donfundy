package hr.algebra.donfundy.service;

import hr.algebra.donfundy.domain.Campaign;
import hr.algebra.donfundy.domain.Donation;
import hr.algebra.donfundy.domain.Donor;
import hr.algebra.donfundy.domain.enums.Status;
import hr.algebra.donfundy.dto.DonationRequest;
import hr.algebra.donfundy.dto.DonationResponse;
import hr.algebra.donfundy.exception.BusinessException;
import hr.algebra.donfundy.exception.ResourceNotFoundException;
import hr.algebra.donfundy.repository.CampaignRepository;
import hr.algebra.donfundy.repository.DonationRepository;
import hr.algebra.donfundy.repository.DonorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonationService {

    private final DonationRepository donationRepository;
    private final CampaignRepository campaignRepository;
    private final DonorRepository donorRepository;
    private final CampaignService campaignService;

    @Transactional(readOnly = true)
    public List<DonationResponse> findAll() {
        return donationRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DonationResponse> findByCampaignId(Long campaignId) {
        return donationRepository.findByCampaignId(campaignId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DonationResponse> findByDonorId(Long donorId) {
        return donationRepository.findByDonorId(donorId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DonationResponse findById(Long id) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.donation.not.found", new Object[]{id}));
        return mapToResponse(donation);
    }

    @Transactional
    public DonationResponse create(DonationRequest request) {
        validateDonationAmount(request.getAmount());

        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new ResourceNotFoundException("error.campaign.not.found", new Object[]{request.getCampaignId()}));

        Donor donor = donorRepository.findById(request.getDonorId())
                .orElseThrow(() -> new ResourceNotFoundException("error.donor.not.found", new Object[]{request.getDonorId()}));

        validateCampaignStatus(campaign);

        Donation donation = new Donation();
        donation.setCampaign(campaign);
        donation.setDonor(donor);
        donation.setAmount(request.getAmount());
        donation.setDonationDate(LocalDate.now());
        donation.setMessage(request.getMessage());
        donation.setPaymentMethod(request.getPaymentMethod());

        Donation saved = donationRepository.save(donation);

        campaignService.updateRaisedAmount(campaign.getId(), request.getAmount().doubleValue());

        return mapToResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.donation.not.found", new Object[]{id}));
        donationRepository.delete(donation);
    }

    private void validateDonationAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("error.donation.amount.invalid");
        }
    }

    private void validateCampaignStatus(Campaign campaign) {
        if (campaign.getStatus() == Status.COMPLETED) {
            throw new BusinessException("error.campaign.already.completed");
        }
        if (campaign.getStatus() != Status.ACTIVE) {
            throw new BusinessException("error.campaign.not.active");
        }
    }

    private DonationResponse mapToResponse(Donation donation) {
        DonationResponse response = new DonationResponse();
        response.setId(donation.getId());
        response.setCampaignId(donation.getCampaign().getId());
        response.setCampaignName(donation.getCampaign().getName());
        response.setDonorId(donation.getDonor().getId());
        response.setDonorName(donation.getDonor().getFirstName() + " " + donation.getDonor().getLastName());
        response.setAmount(donation.getAmount());
        response.setDonationDate(donation.getDonationDate());
        response.setMessage(donation.getMessage());
        response.setPaymentMethod(donation.getPaymentMethod());
        return response;
    }
}
