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
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("CampaignController Integration Tests")
class CampaignControllerIntegrationTest extends BaseIntegrationTest{

    @Autowired
    private CampaignRepository campaignRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DonorRepository donorRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private CustomUserDetailsService userDetailsService;

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
    void shouldGetAllCampaignsWithAuthentication() {
        given()
            .header("Authorization", "Bearer " + userToken)
        .when()
            .get("/campaigns")
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(1)))
            .body("[0].name", equalTo("Test Campaign"));
    }

    @Test
    @DisplayName("Should return 401 when getting campaigns without authentication")
    void shouldReturn401WhenGettingCampaignsWithoutAuthentication() {
        given()
        .when()
            .get("/campaigns")
        .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("Should get campaign by ID")
    void shouldGetCampaignById() {
        given()
            .header("Authorization", "Bearer " + userToken)
        .when()
            .get("/campaigns/{id}", testCampaign.getId())
        .then()
            .statusCode(200)
            .body("id", equalTo(testCampaign.getId().intValue()))
            .body("name", equalTo("Test Campaign"))
            .body("goalAmount", equalTo(1000.0f));
    }

    @Test
    @DisplayName("Should return 404 when campaign not found")
    void shouldReturn404WhenCampaignNotFound() {
        given()
            .header("Authorization", "Bearer " + userToken)
        .when()
            .get("/campaigns/{id}", 999999L)
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("Admin should create campaign successfully")
    void adminShouldCreateCampaignSuccessfully() {
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

        given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/campaigns")
        .then()
            .statusCode(201)
            .body("name", equalTo("New Campaign"))
            .body("goalAmount", equalTo(2000.0f));
    }

    @Test
    @DisplayName("Regular user should not be able to create campaign")
    void regularUserShouldNotCreateCampaign() {
        String body = String.format("""
            {
              "name": "New Campaign",
              "description": "New Description",
              "goalAmount": 2000.0,
              "startDate": "%s",
              "endDate": "%s"
            }
            """, LocalDate.now(), LocalDate.now().plusDays(60));

        given()
            .header("Authorization", "Bearer " + userToken)
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/campaigns")
        .then()
            .statusCode(403);
    }

    @Test
    @DisplayName("Admin should update campaign successfully")
    void adminShouldUpdateCampaignSuccessfully() {
        String body = String.format("""
            {
              "name": "Updated Campaign",
              "description": "Updated Description",
              "goalAmount": 1500.0,
              "startDate": "%s",
              "endDate": "%s"
            }
            """, testCampaign.getStartDate(), testCampaign.getEndDate());

        given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .put("/campaigns/{id}", testCampaign.getId())
        .then()
            .statusCode(200)
            .body("name", equalTo("Updated Campaign"))
            .body("goalAmount", equalTo(1500.0f));
    }

    @Test
    @DisplayName("Regular user should not be able to update campaign")
    void regularUserShouldNotUpdateCampaign() {
        String body = String.format("""
            {
              "name": "Updated Campaign",
              "description": "Updated Description",
              "goalAmount": 1500.0,
              "startDate": "%s",
              "endDate": "%s"
            }
            """, testCampaign.getStartDate(), testCampaign.getEndDate());

        given()
            .header("Authorization", "Bearer " + userToken)
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .put("/campaigns/{id}", testCampaign.getId())
        .then()
            .statusCode(403);
    }

    @Test
    @DisplayName("Admin should delete campaign successfully")
    void adminShouldDeleteCampaignSuccessfully() {
        given()
            .header("Authorization", "Bearer " + adminToken)
        .when()
            .delete("/campaigns/{id}", testCampaign.getId())
        .then()
            .statusCode(204);

        // Verify deletion
        given()
            .header("Authorization", "Bearer " + adminToken)
        .when()
            .get("/campaigns/{id}", testCampaign.getId())
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("Regular user should not be able to delete campaign")
    void regularUserShouldNotDeleteCampaign() {
        given()
            .header("Authorization", "Bearer " + userToken)
        .when()
            .delete("/campaigns/{id}", testCampaign.getId())
        .then()
            .statusCode(403);
    }

    @Test
    @DisplayName("Should validate campaign request fields")
    void shouldValidateCampaignRequestFields() {
        given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/campaigns")
        .then()
            .statusCode(400);
    }
}
