package hr.algebra.donfundy.controller;

import hr.algebra.donfundy.domain.Campaign;
import hr.algebra.donfundy.domain.Donation;
import hr.algebra.donfundy.domain.Donor;
import hr.algebra.donfundy.domain.User;
import hr.algebra.donfundy.domain.enums.PaymentMethod;
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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("DonationController Integration Tests")
@TestPropertySource(properties = {
        "spring.datasource.username=postgres-dev",
        "spring.datasource.password=dev",
        "spring.datasource.url=jdbc:postgresql://localhost:5432/donfundy",
        "jwt.secret=TestJwtSecretKeyForDonFundyApplication"
})
class DonationControllerIntegrationTest {

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
    private Donation testDonation;

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
        testCampaign.setGoalAmount(1000.0);
        testCampaign.setRaisedAmount(0.0);
        testCampaign.setStartDate(LocalDate.now());
        testCampaign.setEndDate(LocalDate.now().plusDays(30));
        testCampaign.setStatus(Status.ACTIVE);
        testCampaign.setCreatedBy(adminDonor);
        testCampaign = campaignRepository.save(testCampaign);

        testDonation = new Donation();
        testDonation.setCampaign(testCampaign);
        testDonation.setDonor(regularDonor);
        testDonation.setAmount(new BigDecimal("100.00"));
        testDonation.setDonationDate(LocalDate.now());
        testDonation.setPaymentMethod(PaymentMethod.CARD);
        testDonation.setMessage("Test donation");
        testDonation = donationRepository.save(testDonation);
    }

    @Test
    @DisplayName("Should get all donations with authentication")
    void shouldGetAllDonationsWithAuthentication() throws Exception {
        mockMvc.perform(get("/donations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                // if your API returns amount as number 100.00, jsonPath usually sees it as 100.0
                .andExpect(jsonPath("$[0].amount").value(100.0));
    }

    @Test
    @DisplayName("Should return 401 when getting donations without authentication")
    void shouldReturn401WhenGettingDonationsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/donations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should get donations by campaign ID")
    void shouldGetDonationsByCampaignId() throws Exception {
        mockMvc.perform(get("/donations")
                        .param("campaignId", String.valueOf(testCampaign.getId()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].campaignId").value(testCampaign.getId()));
    }

    @Test
    @DisplayName("Should get donations by donor ID")
    void shouldGetDonationsByDonorId() throws Exception {
        mockMvc.perform(get("/donations")
                        .param("donorId", String.valueOf(regularDonor.getId()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].donorId").value(regularDonor.getId()));
    }

    @Test
    @DisplayName("Should get donation by ID")
    void shouldGetDonationById() throws Exception {
        mockMvc.perform(get("/donations/{id}", testDonation.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testDonation.getId()))
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.paymentMethod").value("CARD"))
                .andExpect(jsonPath("$.message").value("Test donation"));
    }

    @Test
    @DisplayName("Should return 404 when donation not found")
    void shouldReturn404WhenDonationNotFound() throws Exception {
        mockMvc.perform(get("/donations/{id}", 999999L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should create donation successfully")
    void shouldCreateDonationSuccessfully() throws Exception {
        String body = String.format("""
            {
              "campaignId": %d,
              "donorId": %d,
              "amount": 50.00,
              "paymentMethod": "BANK_TRANSFER",
              "message": "Another donation"
            }
            """, testCampaign.getId(), adminDonor.getId());

        mockMvc.perform(post("/donations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(50.0))
                .andExpect(jsonPath("$.paymentMethod").value("BANK_TRANSFER"))
                .andExpect(jsonPath("$.message").value("Another donation"));
    }

    @Test
    @DisplayName("Should return 401 when creating donation without authentication")
    void shouldReturn401WhenCreatingDonationWithoutAuthentication() throws Exception {
        String body = String.format("""
            {
              "campaignId": %d,
              "donorId": %d,
              "amount": 50.00,
              "paymentMethod": "CARD"
            }
            """, testCampaign.getId(), regularDonor.getId());

        mockMvc.perform(post("/donations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 404 when creating donation with non-existent campaign")
    void shouldReturn404WhenCreatingDonationWithNonExistentCampaign() throws Exception {
        String body = String.format("""
            {
              "campaignId": 999999,
              "donorId": %d,
              "amount": 50.00,
              "paymentMethod": "CARD"
            }
            """, regularDonor.getId());

        mockMvc.perform(post("/donations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when creating donation with non-existent donor")
    void shouldReturn404WhenCreatingDonationWithNonExistentDonor() throws Exception {
        String body = String.format("""
            {
              "campaignId": %d,
              "donorId": 999999,
              "amount": 50.00,
              "paymentMethod": "CARD"
            }
            """, testCampaign.getId());

        mockMvc.perform(post("/donations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 when creating donation for inactive campaign")
    void shouldReturn400WhenCreatingDonationForInactiveCampaign() throws Exception {
        testCampaign.setStatus(Status.COMPLETED);
        campaignRepository.save(testCampaign);

        String body = String.format("""
            {
              "campaignId": %d,
              "donorId": %d,
              "amount": 50.00,
              "paymentMethod": "CARD"
            }
            """, testCampaign.getId(), regularDonor.getId());

        mockMvc.perform(post("/donations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when creating donation with zero amount")
    void shouldReturn400WhenCreatingDonationWithZeroAmount() throws Exception {
        String body = String.format("""
            {
              "campaignId": %d,
              "donorId": %d,
              "amount": 0,
              "paymentMethod": "CARD"
            }
            """, testCampaign.getId(), regularDonor.getId());

        mockMvc.perform(post("/donations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when creating donation with negative amount")
    void shouldReturn400WhenCreatingDonationWithNegativeAmount() throws Exception {
        String body = String.format("""
            {
              "campaignId": %d,
              "donorId": %d,
              "amount": -50.00,
              "paymentMethod": "CARD"
            }
            """, testCampaign.getId(), regularDonor.getId());

        mockMvc.perform(post("/donations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should validate donation request fields")
    void shouldValidateDonationRequestFields() throws Exception {
        mockMvc.perform(post("/donations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should delete donation successfully")
    void shouldDeleteDonationSuccessfully() throws Exception {
        mockMvc.perform(delete("/donations/{id}", testDonation.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/donations/{id}", testDonation.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 401 when deleting donation without authentication")
    void shouldReturn401WhenDeletingDonationWithoutAuthentication() throws Exception {
        mockMvc.perform(delete("/donations/{id}", testDonation.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return empty list when no donations found for campaign")
    void shouldReturnEmptyListWhenNoDonationsFoundForCampaign() throws Exception {
        Campaign newCampaign = new Campaign();
        newCampaign.setName("New Campaign");
        newCampaign.setDescription("No donations");
        newCampaign.setGoalAmount(500.0);
        newCampaign.setRaisedAmount(0.0);
        newCampaign.setStartDate(LocalDate.now());
        newCampaign.setEndDate(LocalDate.now().plusDays(15));
        newCampaign.setStatus(Status.ACTIVE);
        newCampaign.setCreatedBy(adminDonor);
        newCampaign = campaignRepository.save(newCampaign);

        mockMvc.perform(get("/donations")
                        .param("campaignId", String.valueOf(newCampaign.getId()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when no donations found for donor")
    void shouldReturnEmptyListWhenNoDonationsFoundForDonor() throws Exception {
        User newUser = new User();
        newUser.setEmail("newuser@example.com");
        newUser.setPasswordHash(passwordEncoder.encode("password"));
        newUser.setRole(Role.USER);
        newUser = userRepository.save(newUser);

        Donor newDonor = new Donor();
        newDonor.setUser(newUser);
        newDonor.setFirstName("New");
        newDonor.setLastName("Donor");
        newDonor.setEmail("newuser@example.com");
        newDonor = donorRepository.save(newDonor);

        mockMvc.perform(get("/donations")
                        .param("donorId", String.valueOf(newDonor.getId()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}