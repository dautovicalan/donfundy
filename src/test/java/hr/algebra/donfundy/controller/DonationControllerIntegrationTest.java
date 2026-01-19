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
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("DonationController Integration Tests")
class DonationControllerIntegrationTest extends BaseIntegrationTest{

    @Autowired
    private DonationRepository donationRepository;
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
    void shouldGetAllDonationsWithAuthentication() {
        given()
            .header("Authorization", "Bearer " + userToken)
        .when()
            .get("/donations")
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(1)))
            .body("[0].amount", equalTo(100.0f));
    }

    @Test
    @DisplayName("Should return 401 when getting donations without authentication")
    void shouldReturn401WhenGettingDonationsWithoutAuthentication() {
        given()
        .when()
            .get("/donations")
        .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("Should get donations by campaign ID")
    void shouldGetDonationsByCampaignId() {
        given()
            .header("Authorization", "Bearer " + userToken)
            .queryParam("campaignId", testCampaign.getId())
        .when()
            .get("/donations")
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(1)))
            .body("[0].campaignId", equalTo(testCampaign.getId().intValue()));
    }

    @Test
    @DisplayName("Should get donations by donor ID")
    void shouldGetDonationsByDonorId() {
        given()
            .header("Authorization", "Bearer " + userToken)
            .queryParam("donorId", regularDonor.getId())
        .when()
            .get("/donations")
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(1)))
            .body("[0].donorId", equalTo(regularDonor.getId().intValue()));
    }

    @Test
    @DisplayName("Should get donation by ID")
    void shouldGetDonationById() {
        given()
            .header("Authorization", "Bearer " + userToken)
        .when()
            .get("/donations/{id}", testDonation.getId())
        .then()
            .statusCode(200)
            .body("id", equalTo(testDonation.getId().intValue()))
            .body("amount", equalTo(100.0f))
            .body("paymentMethod", equalTo("CARD"))
            .body("message", equalTo("Test donation"));
    }

    @Test
    @DisplayName("Should return 404 when donation not found")
    void shouldReturn404WhenDonationNotFound() {
        given()
            .header("Authorization", "Bearer " + userToken)
        .when()
            .get("/donations/{id}", 999999L)
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("Should create donation successfully")
    void shouldCreateDonationSuccessfully() {
        String body = String.format("""
            {
              "campaignId": %d,
              "donorId": %d,
              "amount": 50.00,
              "paymentMethod": "BANK_TRANSFER",
              "message": "Another donation"
            }
            """, testCampaign.getId(), adminDonor.getId());

        given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/donations")
        .then()
            .statusCode(201)
            .body("amount", equalTo(50.0f))
            .body("paymentMethod", equalTo("BANK_TRANSFER"))
            .body("message", equalTo("Another donation"));
    }

    @Test
    @DisplayName("Should return 401 when creating donation without authentication")
    void shouldReturn401WhenCreatingDonationWithoutAuthentication() {
        String body = String.format("""
            {
              "campaignId": %d,
              "donorId": %d,
              "amount": 50.00,
              "paymentMethod": "CARD"
            }
            """, testCampaign.getId(), regularDonor.getId());

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/donations")
        .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("Should return 404 when creating donation with non-existent campaign")
    void shouldReturn404WhenCreatingDonationWithNonExistentCampaign() {
        String body = String.format("""
            {
              "campaignId": 999999,
              "donorId": %d,
              "amount": 50.00,
              "paymentMethod": "CARD"
            }
            """, regularDonor.getId());

        given()
            .header("Authorization", "Bearer " + userToken)
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/donations")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("Should return 404 when creating donation with non-existent donor")
    void shouldReturn404WhenCreatingDonationWithNonExistentDonor() {
        String body = String.format("""
            {
              "campaignId": %d,
              "donorId": 999999,
              "amount": 50.00,
              "paymentMethod": "CARD"
            }
            """, testCampaign.getId());

        given()
            .header("Authorization", "Bearer " + userToken)
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/donations")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("Should return 400 when creating donation for inactive campaign")
    void shouldReturn400WhenCreatingDonationForInactiveCampaign() {
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

        given()
            .header("Authorization", "Bearer " + userToken)
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/donations")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should return 400 when creating donation with zero amount")
    void shouldReturn400WhenCreatingDonationWithZeroAmount() {
        String body = String.format("""
            {
              "campaignId": %d,
              "donorId": %d,
              "amount": 0,
              "paymentMethod": "CARD"
            }
            """, testCampaign.getId(), regularDonor.getId());

        given()
            .header("Authorization", "Bearer " + userToken)
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/donations")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should return 400 when creating donation with negative amount")
    void shouldReturn400WhenCreatingDonationWithNegativeAmount() {
        String body = String.format("""
            {
              "campaignId": %d,
              "donorId": %d,
              "amount": -50.00,
              "paymentMethod": "CARD"
            }
            """, testCampaign.getId(), regularDonor.getId());

        given()
            .header("Authorization", "Bearer " + userToken)
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/donations")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should validate donation request fields")
    void shouldValidateDonationRequestFields() {
        given()
            .header("Authorization", "Bearer " + userToken)
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/donations")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should delete donation successfully")
    void shouldDeleteDonationSuccessfully() {
        given()
            .header("Authorization", "Bearer " + userToken)
        .when()
            .delete("/donations/{id}", testDonation.getId())
        .then()
            .statusCode(204);

        // Verify deletion
        given()
            .header("Authorization", "Bearer " + userToken)
        .when()
            .get("/donations/{id}", testDonation.getId())
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("Should return 401 when deleting donation without authentication")
    void shouldReturn401WhenDeletingDonationWithoutAuthentication() {
        given()
        .when()
            .delete("/donations/{id}", testDonation.getId())
        .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("Should return empty list when no donations found for campaign")
    void shouldReturnEmptyListWhenNoDonationsFoundForCampaign() {
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

        given()
            .header("Authorization", "Bearer " + userToken)
            .queryParam("campaignId", newCampaign.getId())
        .when()
            .get("/donations")
        .then()
            .statusCode(200)
            .body("$", hasSize(0));
    }

    @Test
    @DisplayName("Should return empty list when no donations found for donor")
    void shouldReturnEmptyListWhenNoDonationsFoundForDonor() {
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

        given()
            .header("Authorization", "Bearer " + userToken)
            .queryParam("donorId", newDonor.getId())
        .when()
            .get("/donations")
        .then()
            .statusCode(200)
            .body("$", hasSize(0));
    }
}
