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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CampaignService Unit Tests")
class CampaignServiceTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DonorRepository donorRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CampaignService campaignService;

    private Campaign testCampaign;
    private Donor testDonor;
    private User testUser;
    private CampaignRequest testRequest;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        // Setup test donor
        testDonor = new Donor();
        testDonor.setId(1L);
        testDonor.setUser(testUser);
        testDonor.setFirstName("Test");
        testDonor.setLastName("Donor");
        testDonor.setEmail("test@example.com");

        // Setup test campaign
        testCampaign = new Campaign();
        testCampaign.setId(1L);
        testCampaign.setName("Test Campaign");
        testCampaign.setDescription("Test Description");
        testCampaign.setGoalAmount(1000.0);
        testCampaign.setRaisedAmount(500.0);
        testCampaign.setStartDate(LocalDate.now());
        testCampaign.setEndDate(LocalDate.now().plusDays(30));
        testCampaign.setStatus(Status.ACTIVE);
        testCampaign.setCreatedBy(testDonor);

        // Setup test request
        testRequest = new CampaignRequest();
        testRequest.setName("Test Campaign");
        testRequest.setDescription("Test Description");
        testRequest.setGoalAmount(1000.0);
        testRequest.setStartDate(LocalDate.now());
        testRequest.setEndDate(LocalDate.now().plusDays(30));
        testRequest.setStatus(Status.ACTIVE);

        // Setup security context
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Should find all campaigns")
    void shouldFindAllCampaigns() {
        // Given
        List<Campaign> campaigns = Arrays.asList(testCampaign);
        when(campaignRepository.findAll()).thenReturn(campaigns);

        // When
        List<CampaignResponse> result = campaignService.findAll();

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Campaign");
        verify(campaignRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find campaigns by status")
    void shouldFindCampaignsByStatus() {
        // Given
        List<Campaign> campaigns = Arrays.asList(testCampaign);
        when(campaignRepository.findByStatus(Status.ACTIVE)).thenReturn(campaigns);

        // When
        List<CampaignResponse> result = campaignService.findByStatus(Status.ACTIVE);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(Status.ACTIVE);
        verify(campaignRepository, times(1)).findByStatus(Status.ACTIVE);
    }

    @Test
    @DisplayName("Should find campaign by ID")
    void shouldFindCampaignById() {
        // Given
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));

        // When
        CampaignResponse result = campaignService.findById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Campaign");
        verify(campaignRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when campaign not found")
    void shouldThrowExceptionWhenCampaignNotFound() {
        // Given
        when(campaignRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> campaignService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(campaignRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should create campaign successfully")
    void shouldCreateCampaignSuccessfully() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.of(testDonor));
        when(campaignRepository.save(any(Campaign.class))).thenReturn(testCampaign);

        // When
        CampaignResponse result = campaignService.create(testRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Campaign");
        assertThat(result.getGoalAmount()).isEqualTo(1000.0);
        verify(campaignRepository, times(1)).save(any(Campaign.class));
    }

    @Test
    @DisplayName("Should throw exception when end date is before start date")
    void shouldThrowExceptionWhenEndDateBeforeStartDate() {
        testRequest.setStartDate(LocalDate.now());
        testRequest.setEndDate(LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> campaignService.create(testRequest))
                .isInstanceOf(BusinessException.class);

        verify(campaignRepository, never()).save(any(Campaign.class));
        verifyNoInteractions(userRepository, donorRepository);
    }

    @Test
    @DisplayName("Should update campaign successfully")
    void shouldUpdateCampaignSuccessfully() {
        // Given
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.of(testDonor));
        when(campaignRepository.save(any(Campaign.class))).thenReturn(testCampaign);

        testRequest.setName("Updated Campaign");

        // When
        CampaignResponse result = campaignService.update(1L, testRequest);

        // Then
        assertThat(result).isNotNull();
        verify(campaignRepository, times(1)).save(any(Campaign.class));
    }

    @Test
    @DisplayName("Should delete campaign successfully")
    void shouldDeleteCampaignSuccessfully() {
        // Given
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.of(testDonor));

        // When
        campaignService.delete(1L);

        // Then
        verify(campaignRepository, times(1)).delete(testCampaign);
    }

    @Test
    @DisplayName("Should update raised amount")
    void shouldUpdateRaisedAmount() {
        // Given
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));
        when(campaignRepository.save(any(Campaign.class))).thenReturn(testCampaign);

        // When
        campaignService.updateRaisedAmount(1L, 100.0);

        // Then
        verify(campaignRepository, times(1)).save(any(Campaign.class));
    }

    @Test
    @DisplayName("Should mark campaign as completed when goal is reached")
    void shouldMarkCampaignAsCompletedWhenGoalReached() {
        // Given
        testCampaign.setRaisedAmount(900.0);
        testCampaign.setGoalAmount(1000.0);
        testCampaign.setStatus(Status.ACTIVE);

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(invocation -> {
            Campaign saved = invocation.getArgument(0);
            assertThat(saved.getStatus()).isEqualTo(Status.COMPLETED);
            return saved;
        });

        // When
        campaignService.updateRaisedAmount(1L, 100.0);

        // Then
        verify(campaignRepository, times(1)).save(any(Campaign.class));
    }

    @Test
    @DisplayName("Should calculate progress percentage correctly")
    void shouldCalculateProgressPercentageCorrectly() {
        // Given
        testCampaign.setRaisedAmount(500.0);
        testCampaign.setGoalAmount(1000.0);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));

        // When
        CampaignResponse result = campaignService.findById(1L);

        // Then
        assertThat(result.getProgressPercentage()).isEqualTo(50.0);
    }
}
