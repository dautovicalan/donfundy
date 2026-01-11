package hr.algebra.donfundy.service;

import hr.algebra.donfundy.domain.Campaign;
import hr.algebra.donfundy.domain.Donation;
import hr.algebra.donfundy.domain.Donor;
import hr.algebra.donfundy.domain.enums.PaymentMethod;
import hr.algebra.donfundy.domain.enums.Status;
import hr.algebra.donfundy.dto.DonationRequest;
import hr.algebra.donfundy.dto.DonationResponse;
import hr.algebra.donfundy.exception.BusinessException;
import hr.algebra.donfundy.exception.ResourceNotFoundException;
import hr.algebra.donfundy.repository.CampaignRepository;
import hr.algebra.donfundy.repository.DonationRepository;
import hr.algebra.donfundy.repository.DonorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DonationService Unit Tests")
class DonationServiceTest {

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private DonorRepository donorRepository;

    @Mock
    private CampaignService campaignService;

    @InjectMocks
    private DonationService donationService;

    private Campaign testCampaign;
    private Donor testDonor;
    private Donation testDonation;
    private DonationRequest testRequest;

    @BeforeEach
    void setUp() {
        // Setup test donor
        testDonor = new Donor();
        testDonor.setId(1L);
        testDonor.setFirstName("John");
        testDonor.setLastName("Doe");
        testDonor.setEmail("john@example.com");

        // Setup test campaign
        testCampaign = new Campaign();
        testCampaign.setId(1L);
        testCampaign.setName("Test Campaign");
        testCampaign.setGoalAmount(1000.0);
        testCampaign.setRaisedAmount(0.0);
        testCampaign.setStatus(Status.ACTIVE);
        testCampaign.setStartDate(LocalDate.now());

        // Setup test donation
        testDonation = new Donation();
        testDonation.setId(1L);
        testDonation.setCampaign(testCampaign);
        testDonation.setDonor(testDonor);
        testDonation.setAmount(new BigDecimal("100.00"));
        testDonation.setDonationDate(LocalDate.now());
        testDonation.setPaymentMethod(PaymentMethod.CARD);
        testDonation.setMessage("Test donation");

        // Setup test request
        testRequest = new DonationRequest();
        testRequest.setCampaignId(1L);
        testRequest.setDonorId(1L);
        testRequest.setAmount(new BigDecimal("100.00"));
        testRequest.setPaymentMethod(PaymentMethod.CARD);
        testRequest.setMessage("Test donation");
    }

    @Test
    @DisplayName("Should find all donations")
    void shouldFindAllDonations() {
        // Given
        List<Donation> donations = Arrays.asList(testDonation);
        when(donationRepository.findAll()).thenReturn(donations);

        // When
        List<DonationResponse> result = donationService.findAll();

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        verify(donationRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find donations by campaign ID")
    void shouldFindDonationsByCampaignId() {
        // Given
        List<Donation> donations = Arrays.asList(testDonation);
        when(donationRepository.findByCampaignId(1L)).thenReturn(donations);

        // When
        List<DonationResponse> result = donationService.findByCampaignId(1L);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCampaignId()).isEqualTo(1L);
        verify(donationRepository, times(1)).findByCampaignId(1L);
    }

    @Test
    @DisplayName("Should find donations by donor ID")
    void shouldFindDonationsByDonorId() {
        // Given
        List<Donation> donations = Arrays.asList(testDonation);
        when(donationRepository.findByDonorId(1L)).thenReturn(donations);

        // When
        List<DonationResponse> result = donationService.findByDonorId(1L);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDonorId()).isEqualTo(1L);
        verify(donationRepository, times(1)).findByDonorId(1L);
    }

    @Test
    @DisplayName("Should find donation by ID")
    void shouldFindDonationById() {
        // Given
        when(donationRepository.findById(1L)).thenReturn(Optional.of(testDonation));

        // When
        DonationResponse result = donationService.findById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDonorName()).isEqualTo("John Doe");
        verify(donationRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when donation not found")
    void shouldThrowExceptionWhenDonationNotFound() {
        // Given
        when(donationRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> donationService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(donationRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should create donation successfully")
    void shouldCreateDonationSuccessfully() {
        // Given
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));
        when(donorRepository.findById(1L)).thenReturn(Optional.of(testDonor));
        when(donationRepository.save(any(Donation.class))).thenReturn(testDonation);
        doNothing().when(campaignService).updateRaisedAmount(anyLong(), anyDouble());

        // When
        DonationResponse result = donationService.create(testRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        verify(donationRepository, times(1)).save(any(Donation.class));
        verify(campaignService, times(1)).updateRaisedAmount(eq(1L), anyDouble());
    }

    @Test
    @DisplayName("Should throw exception when campaign not found")
    void shouldThrowExceptionWhenCampaignNotFound() {
        // Given
        when(campaignRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> donationService.create(testRequest))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(donationRepository, never()).save(any(Donation.class));
    }

    @Test
    @DisplayName("Should throw exception when donor not found")
    void shouldThrowExceptionWhenDonorNotFound() {
        // Given
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));
        when(donorRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> donationService.create(testRequest))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(donationRepository, never()).save(any(Donation.class));
    }

    @Test
    @DisplayName("Should throw exception when campaign is not active")
    void shouldThrowExceptionWhenCampaignNotActive() {
        // Given
        testCampaign.setStatus(Status.COMPLETED);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));
        when(donorRepository.findById(1L)).thenReturn(Optional.of(testDonor));

        // When & Then
        assertThatThrownBy(() -> donationService.create(testRequest))
                .isInstanceOf(BusinessException.class);
        verify(donationRepository, never()).save(any(Donation.class));
    }

    @Test
    @DisplayName("Should throw exception when donation amount is zero")
    void shouldThrowExceptionWhenDonationAmountIsZero() {
        // Given
        testRequest.setAmount(BigDecimal.ZERO);

        // When & Then
        assertThatThrownBy(() -> donationService.create(testRequest))
                .isInstanceOf(BusinessException.class);
        verify(donationRepository, never()).save(any(Donation.class));
    }

    @Test
    @DisplayName("Should throw exception when donation amount is negative")
    void shouldThrowExceptionWhenDonationAmountIsNegative() {
        // Given
        testRequest.setAmount(new BigDecimal("-50.00"));

        // When & Then
        assertThatThrownBy(() -> donationService.create(testRequest))
                .isInstanceOf(BusinessException.class);
        verify(donationRepository, never()).save(any(Donation.class));
    }

    @Test
    @DisplayName("Should delete donation successfully")
    void shouldDeleteDonationSuccessfully() {
        // Given
        when(donationRepository.findById(1L)).thenReturn(Optional.of(testDonation));

        // When
        donationService.delete(1L);

        // Then
        verify(donationRepository, times(1)).delete(testDonation);
    }

    @Test
    @DisplayName("Should map donation to response correctly")
    void shouldMapDonationToResponseCorrectly() {
        // Given
        when(donationRepository.findById(1L)).thenReturn(Optional.of(testDonation));

        // When
        DonationResponse result = donationService.findById(1L);

        // Then
        assertThat(result.getId()).isEqualTo(testDonation.getId());
        assertThat(result.getCampaignId()).isEqualTo(testCampaign.getId());
        assertThat(result.getCampaignName()).isEqualTo(testCampaign.getName());
        assertThat(result.getDonorId()).isEqualTo(testDonor.getId());
        assertThat(result.getDonorName()).isEqualTo("John Doe");
        assertThat(result.getAmount()).isEqualByComparingTo(testDonation.getAmount());
        assertThat(result.getPaymentMethod()).isEqualTo(testDonation.getPaymentMethod());
        assertThat(result.getMessage()).isEqualTo(testDonation.getMessage());
    }
}
