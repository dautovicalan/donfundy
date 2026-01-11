package hr.algebra.donfundy.controller;

import hr.algebra.donfundy.domain.Campaign;
import hr.algebra.donfundy.domain.Donor;
import hr.algebra.donfundy.domain.User;
import hr.algebra.donfundy.domain.enums.Role;
import hr.algebra.donfundy.domain.enums.Status;
import hr.algebra.donfundy.repository.CampaignRepository;
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

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("CampaignController Integration Tests")
@TestPropertySource(properties = {
        "spring.datasource.username=postgres-dev",
        "spring.datasource.password=dev",
        "spring.datasource.url=jdbc:postgresql://localhost:5432/donfundy",
        "jwt.secret=TestJwtSecretKeyForDonFundyApplication"
})
class CampaignControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;

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
    }

    @Test
    @DisplayName("Should get all campaigns with authentication")
    void shouldGetAllCampaignsWithAuthentication() throws Exception {
        mockMvc.perform(get("/campaigns")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Campaign"));
    }

    @Test
    @DisplayName("Should return 401 when getting campaigns without authentication")
    void shouldReturn401WhenGettingCampaignsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/campaigns"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should get campaign by ID")
    void shouldGetCampaignById() throws Exception {
        mockMvc.perform(get("/campaigns/{id}", testCampaign.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCampaign.getId()))
                .andExpect(jsonPath("$.name").value("Test Campaign"))
                .andExpect(jsonPath("$.goalAmount").value(1000.0));
    }

    @Test
    @DisplayName("Should return 404 when campaign not found")
    void shouldReturn404WhenCampaignNotFound() throws Exception {
        mockMvc.perform(get("/campaigns/{id}", 999999L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Admin should create campaign successfully")
    void adminShouldCreateCampaignSuccessfully() throws Exception {
        String body = String.format("""
            {
              "name": "New Campaign",
              "description": "New Description",
              "status": "ACTIVE",
              "goalAmount": 2000.0,
              "startDate": "%s",
              "endDate": "%s"
            }
            """, LocalDate.now(), LocalDate.now().plusDays(60));

        mockMvc.perform(post("/campaigns")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Campaign"))
                .andExpect(jsonPath("$.goalAmount").value(2000.0));
    }

    @Test
    @DisplayName("Regular user should not be able to create campaign")
    void regularUserShouldNotCreateCampaign() throws Exception {
        String body = String.format("""
            {
              "name": "New Campaign",
              "description": "New Description",
              "goalAmount": 2000.0,
              "startDate": "%s",
              "endDate": "%s"
            }
            """, LocalDate.now(), LocalDate.now().plusDays(60));

        mockMvc.perform(post("/campaigns")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin should update campaign successfully")
    void adminShouldUpdateCampaignSuccessfully() throws Exception {
        String body = String.format("""
            {
              "name": "Updated Campaign",
              "description": "Updated Description",
              "goalAmount": 1500.0,
              "startDate": "%s",
              "endDate": "%s"
            }
            """, testCampaign.getStartDate(), testCampaign.getEndDate());

        mockMvc.perform(put("/campaigns/{id}", testCampaign.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Campaign"))
                .andExpect(jsonPath("$.goalAmount").value(1500.0));
    }

    @Test
    @DisplayName("Regular user should not be able to update campaign")
    void regularUserShouldNotUpdateCampaign() throws Exception {
        String body = String.format("""
            {
              "name": "Updated Campaign",
              "description": "Updated Description",
              "goalAmount": 1500.0,
              "startDate": "%s",
              "endDate": "%s"
            }
            """, testCampaign.getStartDate(), testCampaign.getEndDate());

        mockMvc.perform(put("/campaigns/{id}", testCampaign.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin should delete campaign successfully")
    void adminShouldDeleteCampaignSuccessfully() throws Exception {
        mockMvc.perform(delete("/campaigns/{id}", testCampaign.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/campaigns/{id}", testCampaign.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Regular user should not be able to delete campaign")
    void regularUserShouldNotDeleteCampaign() throws Exception {
        mockMvc.perform(delete("/campaigns/{id}", testCampaign.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should validate campaign request fields")
    void shouldValidateCampaignRequestFields() throws Exception {
        mockMvc.perform(post("/campaigns")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}