package hr.algebra.donfundy.service;

import hr.algebra.donfundy.domain.Campaign;
import hr.algebra.donfundy.domain.Donor;
import hr.algebra.donfundy.domain.User;
import hr.algebra.donfundy.domain.enums.Status;
import hr.algebra.donfundy.dto.CampaignRequest;
import hr.algebra.donfundy.dto.CampaignResponse;
import hr.algebra.donfundy.exception.BusinessException;
import hr.algebra.donfundy.exception.ResourceNotFoundException;
import hr.algebra.donfundy.repository.CampaignRepository;
import hr.algebra.donfundy.repository.DonorRepository;
import hr.algebra.donfundy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;
    private final DonorRepository donorRepository;

    @Transactional(readOnly = true)
    public List<CampaignResponse> findAll() {
        return campaignRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CampaignResponse> findByStatus(Status status) {
        return campaignRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CampaignResponse findById(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.campaign.not.found", new Object[]{id}));
        return mapToResponse(campaign);
    }

    @Transactional(readOnly = true)
    public List<CampaignResponse> findByCurrentUser() {
        Donor currentDonor = getCurrentDonor();
        return campaignRepository.findAll().stream()
                .filter(campaign -> campaign.getCreatedBy() != null &&
                        campaign.getCreatedBy().getId().equals(currentDonor.getId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CampaignResponse create(CampaignRequest request) {
        validateCampaignDates(request);

        Donor currentDonor = getCurrentDonor();

        Campaign campaign = new Campaign();
        campaign.setName(request.getName());
        campaign.setDescription(request.getDescription());
        campaign.setGoalAmount(request.getGoalAmount());
        campaign.setRaisedAmount(0.0);
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        campaign.setStatus(request.getStatus());
        campaign.setCreatedBy(currentDonor);

        Campaign saved = campaignRepository.save(campaign);
        return mapToResponse(saved);
    }

    @Transactional
    public CampaignResponse update(Long id, CampaignRequest request) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.campaign.not.found", new Object[]{id}));

        validateOwnership(campaign);
        validateCampaignDates(request);

        campaign.setName(request.getName());
        campaign.setDescription(request.getDescription());
        campaign.setGoalAmount(request.getGoalAmount());
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        campaign.setStatus(request.getStatus());

        Campaign updated = campaignRepository.save(campaign);
        return mapToResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.campaign.not.found", new Object[]{id}));

        validateOwnership(campaign);
        campaignRepository.delete(campaign);
    }

    @Transactional
    public void updateRaisedAmount(Long campaignId, Double amount) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("error.campaign.not.found", new Object[]{campaignId}));

        double newRaisedAmount = (campaign.getRaisedAmount() != null ? campaign.getRaisedAmount() : 0.0) + amount;
        campaign.setRaisedAmount(newRaisedAmount);

        if (newRaisedAmount >= campaign.getGoalAmount() && campaign.getStatus() == Status.ACTIVE) {
            campaign.setStatus(Status.COMPLETED);
        }

        campaignRepository.save(campaign);
    }

    private void validateCampaignDates(CampaignRequest request) {
        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("error.invalid.date.range");
        }
    }

    private void validateOwnership(Campaign campaign) {
        Donor currentDonor = getCurrentDonor();
        if (campaign.getCreatedBy() == null ||
                !campaign.getCreatedBy().getId().equals(currentDonor.getId())) {
            throw new BusinessException("error.unauthorized.campaign.access");
        }
    }

    private Donor getCurrentDonor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("error.user.not.found"));

        return donorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException("error.donor.not.found.for.user"));
    }

    private CampaignResponse mapToResponse(Campaign campaign) {
        CampaignResponse response = new CampaignResponse();
        response.setId(campaign.getId());
        response.setName(campaign.getName());
        response.setDescription(campaign.getDescription());
        response.setGoalAmount(campaign.getGoalAmount());
        response.setRaisedAmount(campaign.getRaisedAmount() != null ? campaign.getRaisedAmount() : 0.0);
        response.setStartDate(campaign.getStartDate());
        response.setEndDate(campaign.getEndDate());
        response.setStatus(campaign.getStatus());

        if (campaign.getCreatedBy() != null) {
            Donor creator = campaign.getCreatedBy();
            response.setCreatedById(creator.getId());
            response.setCreatedByName(creator.getFirstName() + " " + creator.getLastName());
            response.setCreatedByEmail(creator.getEmail());
        }

        double raisedAmount = campaign.getRaisedAmount() != null ? campaign.getRaisedAmount() : 0.0;
        double goalAmount = campaign.getGoalAmount();
        double percentage = goalAmount > 0 ? (raisedAmount / goalAmount) * 100 : 0;
        response.setProgressPercentage(Math.min(percentage, 100.0));

        return response;
    }
}
