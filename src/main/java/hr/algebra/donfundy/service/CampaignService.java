package hr.algebra.donfundy.service;

import hr.algebra.donfundy.domain.Campaign;
import hr.algebra.donfundy.domain.enums.Status;
import hr.algebra.donfundy.dto.CampaignRequest;
import hr.algebra.donfundy.dto.CampaignResponse;
import hr.algebra.donfundy.exception.BusinessException;
import hr.algebra.donfundy.exception.ResourceNotFoundException;
import hr.algebra.donfundy.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;

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

    @Transactional
    public CampaignResponse create(CampaignRequest request) {
        validateCampaignDates(request);

        Campaign campaign = new Campaign();
        campaign.setName(request.getName());
        campaign.setDescription(request.getDescription());
        campaign.setGoalAmount(request.getGoalAmount());
        campaign.setRaisedAmount(0.0);
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        campaign.setStatus(request.getStatus());

        Campaign saved = campaignRepository.save(campaign);
        return mapToResponse(saved);
    }

    @Transactional
    public CampaignResponse update(Long id, CampaignRequest request) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.campaign.not.found", new Object[]{id}));

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

        double raisedAmount = campaign.getRaisedAmount() != null ? campaign.getRaisedAmount() : 0.0;
        double goalAmount = campaign.getGoalAmount();
        double percentage = goalAmount > 0 ? (raisedAmount / goalAmount) * 100 : 0;
        response.setProgressPercentage(Math.min(percentage, 100.0));

        return response;
    }
}
