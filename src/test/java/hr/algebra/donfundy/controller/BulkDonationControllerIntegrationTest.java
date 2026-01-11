package hr.algebra.donfundy.controller;

import hr.algebra.donfundy.domain.Campaign;
import hr.algebra.donfundy.domain.Donor;
import hr.algebra.donfundy.domain.User;
import hr.algebra.donfundy.domain.enums.Role;
import hr.algebra.donfundy.domain.enums.Status;
import hr.algebra.donfundy.repository.CampaignRepository;
import hr.algebra.donfundy.repository.DonationRepository;
import hr.algebra.donfundy.repository.DonorRepository;
import hr.algebra.donfundy.repository.UserRepository;
import hr.algebra.donfundy.security.CustomUserDetailsService;
import hr.algebra.donfundy.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("BulkDonationController Integration Tests")
@TestPropertySource(properties = {
        "spring.datasource.username=postgres-dev",
        "spring.datasource.password=dev",
        "spring.datasource.url=jdbc:postgresql://localhost:5432/donfundy",
        "jwt.secret=TestJwtSecretKeyForDonFundyApplication"
})
class BulkDonationControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private DonationRepository donationRepository;
    @Autowired private CampaignRepository campaignRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private DonorRepository donorRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private CustomUserDetailsService userDetailsService;

    private String adminToken;
    private String userToken;
    private Campaign testCampaign;
    private User adminUser;
    private User regularUser;
    private Donor adminDonor;
    private Donor regularDonor;

    @BeforeEach
    void setUp() {
        donationRepository.deleteAll();
        campaignRepository.deleteAll();
        donorRepository.deleteAll();
        userRepository.deleteAll();

        adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setPasswordHash(passwordEncoder.encode("password"));
        adminUser.setRole(Role.ADMIN);
        adminUser = userRepository.save(adminUser);

        adminDonor = new Donor();
        adminDonor.setUser(adminUser);
        adminDonor.setFirstName("Admin");
        adminDonor.setLastName("User");
        adminDonor.setEmail("admin@example.com");
        adminDonor = donorRepository.save(adminDonor);

        regularUser = new User();
        regularUser.setEmail("user@example.com");
        regularUser.setPasswordHash(passwordEncoder.encode("password"));
        regularUser.setRole(Role.USER);
        regularUser = userRepository.save(regularUser);

        regularDonor = new Donor();
        regularDonor.setUser(regularUser);
        regularDonor.setFirstName("Regular");
        regularDonor.setLastName("User");
        regularDonor.setEmail("user@example.com");
        regularDonor = donorRepository.save(regularDonor);

        adminToken = jwtUtil.generateToken(userDetailsService.loadUserByUsername(adminUser.getEmail()));
        userToken = jwtUtil.generateToken(userDetailsService.loadUserByUsername(regularUser.getEmail()));

        testCampaign = new Campaign();
        testCampaign.setName("Test Campaign");
        testCampaign.setDescription("Test Description");
        testCampaign.setGoalAmount(10000.0);
        testCampaign.setRaisedAmount(0.0);
        testCampaign.setStartDate(LocalDate.now());
        testCampaign.setEndDate(LocalDate.now().plusDays(30));
        testCampaign.setStatus(Status.ACTIVE);
        testCampaign.setCreatedBy(adminDonor);
        testCampaign = campaignRepository.save(testCampaign);
    }

    @Test
    @DisplayName("Admin should upload valid CSV successfully")
    void adminShouldUploadValidCsvSuccessfully() throws Exception {
        String csvContent = String.format("""
                campaignId,amount,donorEmail,donorFirstName,donorLastName,paymentMethod,message
                %d,100.50,john@example.com,John,Doe,CARD,Thank you
                %d,50.00,jane@example.com,Jane,Smith,BANK_TRANSFER,Great cause
                """, testCampaign.getId(), testCampaign.getId());

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "donations.csv",
                "text/csv",
                csvContent.getBytes()
        );

        mockMvc.perform(
                        multipart("/bulk-donations/upload")
                                .file(file)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.successCount").value(2))
                .andExpect(jsonPath("$.failureCount").value(0))
                .andExpect(jsonPath("$.totalRows").value(2));
    }

    @Test
    @DisplayName("Regular user should not be able to upload CSV")
    void regularUserShouldNotUploadCsv() throws Exception {
        String csvContent = String.format("""
                campaignId,amount,donorEmail,donorFirstName,donorLastName,paymentMethod,message
                %d,100.00,test@example.com,Test,User,CARD,
                """, testCampaign.getId());

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "donations.csv",
                "text/csv",
                csvContent.getBytes()
        );

        mockMvc.perform(
                        multipart("/bulk-donations/upload")
                                .file(file)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 401 when uploading without authentication")
    void shouldReturn401WhenUploadingWithoutAuthentication() throws Exception {
        String csvContent = String.format("""
                campaignId,amount,donorEmail,donorFirstName,donorLastName,paymentMethod,message
                %d,100.00,test@example.com,Test,User,CARD,
                """, testCampaign.getId());

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "donations.csv",
                "text/csv",
                csvContent.getBytes()
        );

        mockMvc.perform(
                        multipart("/bulk-donations/upload")
                                .file(file)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 400 for empty file")
    void shouldReturn400ForEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "donations.csv",
                "text/csv",
                new byte[0]
        );

        mockMvc.perform(
                        multipart("/bulk-donations/upload")
                                .file(file)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("File is empty"));
    }

    @Test
    @DisplayName("Should return 400 for non-CSV file")
    void shouldReturn400ForNonCsvFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "donations.txt",
                "text/plain",
                "not a csv".getBytes()
        );

        mockMvc.perform(
                        multipart("/bulk-donations/upload")
                                .file(file)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("Only CSV files are allowed"));
    }

}