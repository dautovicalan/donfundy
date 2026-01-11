package hr.algebra.donfundy.service;

import hr.algebra.donfundy.domain.Campaign;
import hr.algebra.donfundy.domain.Donor;
import hr.algebra.donfundy.domain.enums.Status;
import hr.algebra.donfundy.dto.BulkDonationResult;
import hr.algebra.donfundy.repository.CampaignRepository;
import hr.algebra.donfundy.repository.DonorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BulkDonationService Unit Tests")
class BulkDonationServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private DonorRepository donorRepository;

    @Mock
    private CampaignRepository campaignRepository;

    @InjectMocks
    private BulkDonationService bulkDonationService;

    private Campaign testCampaign;
    private Donor testDonor;
    private Donor anonymousDonor;

    @BeforeEach
    void setUp() {
        // Setup test campaign
        testCampaign = new Campaign();
        testCampaign.setId(1L);
        testCampaign.setName("Test Campaign");
        testCampaign.setGoalAmount(1000.0);
        testCampaign.setRaisedAmount(0.0);
        testCampaign.setStatus(Status.ACTIVE);
        testCampaign.setStartDate(LocalDate.now());

        // Setup test donor
        testDonor = new Donor();
        testDonor.setId(1L);
        testDonor.setFirstName("John");
        testDonor.setLastName("Doe");
        testDonor.setEmail("john@example.com");

        // Setup anonymous donor
        anonymousDonor = new Donor();
        anonymousDonor.setId(2L);
        anonymousDonor.setFirstName("Anonymous");
        anonymousDonor.setLastName("Donor");
        anonymousDonor.setEmail("anonymous@donfundy.com");
    }

    @Test
    @DisplayName("Should process valid CSV file successfully")
    void shouldProcessValidCsvFileSuccessfully() {
        // Given
        String csvContent = """
                campaignId,amount,donorEmail,donorFirstName,donorLastName,paymentMethod,message
                1,100.50,john@example.com,John,Doe,CARD,Thank you
                1,50.00,anonymous,,,BANK_TRANSFER,
                """;

        MultipartFile file = new MockMultipartFile(
                "file",
                "donations.csv",
                "text/csv",
                csvContent.getBytes()
        );

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));
        when(donorRepository.findByEmail("anonymous@donfundy.com")).thenReturn(Optional.of(anonymousDonor));
        when(donorRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testDonor));
        when(jdbcTemplate.batchUpdate(anyString(), any(BatchPreparedStatementSetter.class)))
                .thenReturn(new int[]{1, 1});
        when(campaignRepository.save(any(Campaign.class))).thenReturn(testCampaign);

        // When
        BulkDonationResult result = bulkDonationService.processBulkDonations(file);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSuccessCount()).isEqualTo(2);
        assertThat(result.getFailureCount()).isEqualTo(0);
        assertThat(result.getTotalRows()).isEqualTo(2);
        verify(jdbcTemplate, times(1)).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
        verify(campaignRepository, atLeastOnce()).save(any(Campaign.class));
    }

    @Test
    @DisplayName("Should create anonymous donor if not exists")
    void shouldCreateAnonymousDonorIfNotExists() {
        // Given
        String csvContent = """
                campaignId,amount,donorEmail,donorFirstName,donorLastName,paymentMethod,message
                1,50.00,anonymous,,,CARD,
                """;

        MultipartFile file = new MockMultipartFile(
                "file",
                "donations.csv",
                "text/csv",
                csvContent.getBytes()
        );

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));
        when(donorRepository.findByEmail("anonymous@donfundy.com")).thenReturn(Optional.empty());
        when(donorRepository.save(any(Donor.class))).thenReturn(anonymousDonor);
        when(jdbcTemplate.batchUpdate(anyString(), any(BatchPreparedStatementSetter.class)))
                .thenReturn(new int[]{1});
        when(campaignRepository.save(any(Campaign.class))).thenReturn(testCampaign);

        // When
        BulkDonationResult result = bulkDonationService.processBulkDonations(file);

        // Then
        assertThat(result.getSuccessCount()).isEqualTo(1);
        verify(donorRepository, times(1)).save(any(Donor.class));
    }

    @Test
    @DisplayName("Should create new donor if not exists")
    void shouldCreateNewDonorIfNotExists() {
        // Given
        String csvContent = """
                campaignId,amount,donorEmail,donorFirstName,donorLastName,paymentMethod,message
                1,100.00,new@example.com,New,User,CARD,
                """;

        MultipartFile file = new MockMultipartFile(
                "file",
                "donations.csv",
                "text/csv",
                csvContent.getBytes()
        );

        Donor newDonor = new Donor();
        newDonor.setId(3L);
        newDonor.setEmail("new@example.com");
        newDonor.setFirstName("New");
        newDonor.setLastName("User");

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));
        when(donorRepository.findByEmail("anonymous@donfundy.com")).thenReturn(Optional.of(anonymousDonor));
        when(donorRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(donorRepository.save(any(Donor.class))).thenReturn(newDonor);
        when(jdbcTemplate.batchUpdate(anyString(), any(BatchPreparedStatementSetter.class)))
                .thenReturn(new int[]{1});
        when(campaignRepository.save(any(Campaign.class))).thenReturn(testCampaign);

        // When
        BulkDonationResult result = bulkDonationService.processBulkDonations(file);

        // Then
        assertThat(result.getSuccessCount()).isEqualTo(1);
        verify(donorRepository, times(1)).save(argThat(donor ->
            donor.getEmail().equals("new@example.com")));
    }

    @Test
    @DisplayName("Should handle invalid campaign ID")
    void shouldHandleInvalidCampaignId() {
        // Given
        String csvContent = """
                campaignId,amount,donorEmail,donorFirstName,donorLastName,paymentMethod,message
                999,100.00,john@example.com,John,Doe,CARD,
                """;

        MultipartFile file = new MockMultipartFile(
                "file",
                "donations.csv",
                "text/csv",
                csvContent.getBytes()
        );

        when(campaignRepository.findById(999L)).thenReturn(Optional.empty());
        when(donorRepository.findByEmail("anonymous@donfundy.com")).thenReturn(Optional.of(anonymousDonor));

        // When
        BulkDonationResult result = bulkDonationService.processBulkDonations(file);

        // Then
        assertThat(result.getFailureCount()).isGreaterThan(0);
        assertThat(result.getErrors()).isNotEmpty();
        verify(jdbcTemplate, never()).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
    }

    @Test
    @DisplayName("Should handle inactive campaign")
    void shouldHandleInactiveCampaign() {
        // Given
        testCampaign.setStatus(Status.COMPLETED);

        String csvContent = """
                campaignId,amount,donorEmail,donorFirstName,donorLastName,paymentMethod,message
                1,100.00,john@example.com,John,Doe,CARD,
                """;

        MultipartFile file = new MockMultipartFile(
                "file",
                "donations.csv",
                "text/csv",
                csvContent.getBytes()
        );

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));
        when(donorRepository.findByEmail("anonymous@donfundy.com")).thenReturn(Optional.of(anonymousDonor));

        // When
        BulkDonationResult result = bulkDonationService.processBulkDonations(file);

        // Then
        assertThat(result.getFailureCount()).isGreaterThan(0);
        assertThat(result.getErrors()).anyMatch(error -> error.contains("not active"));
        verify(jdbcTemplate, never()).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
    }

    @Test
    @DisplayName("Should handle invalid amount")
    void shouldHandleInvalidAmount() {
        // Given
        String csvContent = """
                campaignId,amount,donorEmail,donorFirstName,donorLastName,paymentMethod,message
                1,-100.00,john@example.com,John,Doe,CARD,
                1,0,jane@example.com,Jane,Doe,CARD,
                """;

        MultipartFile file = new MockMultipartFile(
                "file",
                "donations.csv",
                "text/csv",
                csvContent.getBytes()
        );

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));
        when(donorRepository.findByEmail("anonymous@donfundy.com")).thenReturn(Optional.of(anonymousDonor));

        // When
        BulkDonationResult result = bulkDonationService.processBulkDonations(file);

        // Then
        assertThat(result.getFailureCount()).isEqualTo(2);
        assertThat(result.getErrors()).hasSize(2);
        verify(jdbcTemplate, never()).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
    }

    @Test
    @DisplayName("Should update campaign raised amount")
    void shouldUpdateCampaignRaisedAmount() {
        // Given
        String csvContent = """
                campaignId,amount,donorEmail,donorFirstName,donorLastName,paymentMethod,message
                1,100.00,john@example.com,John,Doe,CARD,
                1,50.00,jane@example.com,Jane,Doe,CARD,
                """;

        MultipartFile file = new MockMultipartFile(
                "file",
                "donations.csv",
                "text/csv",
                csvContent.getBytes()
        );

        Donor janeDonor = new Donor();
        janeDonor.setId(4L);
        janeDonor.setEmail("jane@example.com");

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));
        when(donorRepository.findByEmail("anonymous@donfundy.com")).thenReturn(Optional.of(anonymousDonor));
        when(donorRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testDonor));
        when(donorRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(janeDonor));
        when(jdbcTemplate.batchUpdate(anyString(), any(BatchPreparedStatementSetter.class)))
                .thenReturn(new int[]{1, 1});
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(invocation -> {
            Campaign saved = invocation.getArgument(0);
            assertThat(saved.getRaisedAmount()).isEqualTo(150.0);
            return saved;
        });

        // When
        BulkDonationResult result = bulkDonationService.processBulkDonations(file);

        // Then
        assertThat(result.getSuccessCount()).isEqualTo(2);
        verify(campaignRepository, atLeastOnce()).save(any(Campaign.class));
    }

    @Test
    @DisplayName("Should mark campaign as completed when goal reached")
    void shouldMarkCampaignAsCompletedWhenGoalReached() {
        // Given
        testCampaign.setGoalAmount(100.0);
        testCampaign.setRaisedAmount(50.0);

        String csvContent = """
                campaignId,amount,donorEmail,donorFirstName,donorLastName,paymentMethod,message
                1,60.00,john@example.com,John,Doe,CARD,
                """;

        MultipartFile file = new MockMultipartFile(
                "file",
                "donations.csv",
                "text/csv",
                csvContent.getBytes()
        );

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));
        when(donorRepository.findByEmail("anonymous@donfundy.com")).thenReturn(Optional.of(anonymousDonor));
        when(donorRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testDonor));
        when(jdbcTemplate.batchUpdate(anyString(), any(BatchPreparedStatementSetter.class)))
                .thenReturn(new int[]{1});
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(invocation -> {
            Campaign saved = invocation.getArgument(0);
            assertThat(saved.getStatus()).isEqualTo(Status.COMPLETED);
            return saved;
        });

        // When
        BulkDonationResult result = bulkDonationService.processBulkDonations(file);

        // Then
        assertThat(result.getSuccessCount()).isEqualTo(1);
        verify(campaignRepository, atLeastOnce()).save(any(Campaign.class));
    }

    @Test
    @DisplayName("Should handle invalid payment method")
    void shouldHandleInvalidPaymentMethod() {
        // Given
        String csvContent = """
                campaignId,amount,donorEmail,donorFirstName,donorLastName,paymentMethod,message
                1,100.00,john@example.com,John,Doe,INVALID_METHOD,
                """;

        MultipartFile file = new MockMultipartFile(
                "file",
                "donations.csv",
                "text/csv",
                csvContent.getBytes()
        );

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));
        when(donorRepository.findByEmail("anonymous@donfundy.com")).thenReturn(Optional.of(anonymousDonor));

        // When
        BulkDonationResult result = bulkDonationService.processBulkDonations(file);

        // Then
        assertThat(result.getFailureCount()).isGreaterThan(0);
        assertThat(result.getErrors()).anyMatch(error -> error.contains("Invalid payment method"));
        verify(jdbcTemplate, never()).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
    }
}
