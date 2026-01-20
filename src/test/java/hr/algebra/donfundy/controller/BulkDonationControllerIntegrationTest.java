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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("BulkDonationController Integration Tests")
class BulkDonationControllerIntegrationTest extends BaseIntegrationTest{

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

    private File createTempCsvFile(String content) throws IOException {
        File tempFile = File.createTempFile("donations", ".csv");
        tempFile.deleteOnExit();
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(content);
        }
        return tempFile;
    }

    @Test
    @DisplayName("Admin should upload valid CSV successfully")
    void adminShouldUploadValidCsvSuccessfully() throws IOException {
        String csvContent = String.format("""
                campaignId,amount,donorEmail,donorFirstName,donorLastName,paymentMethod,message
                %d,100.50,john@example.com,John,Doe,CARD,Thank you
                %d,50.00,jane@example.com,Jane,Smith,BANK_TRANSFER,Great cause
                """, testCampaign.getId(), testCampaign.getId());

        File csvFile = createTempCsvFile(csvContent);

        given()
            .header("Authorization", "Bearer " + adminToken)
            .multiPart("file", csvFile, "text/csv")
        .when()
            .post("/bulk-donations/upload")
        .then()
            .statusCode(201)
            .body("successCount", equalTo(2))
            .body("failureCount", equalTo(0))
            .body("totalRows", equalTo(2));
    }

    @Test
    @DisplayName("Regular user should not be able to upload CSV")
    void regularUserShouldNotUploadCsv() throws IOException {
        String csvContent = String.format("""
                campaignId,amount,donorEmail,donorFirstName,donorLastName,paymentMethod,message
                %d,100.00,test@example.com,Test,User,CARD,
                """, testCampaign.getId());

        File csvFile = createTempCsvFile(csvContent);

        given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", csvFile, "text/csv")
        .when()
            .post("/bulk-donations/upload")
        .then()
            .statusCode(403);
    }

    @Test
    @DisplayName("Should return 401 when uploading without authentication")
    void shouldReturn401WhenUploadingWithoutAuthentication() throws IOException {
        String csvContent = String.format("""
                campaignId,amount,donorEmail,donorFirstName,donorLastName,paymentMethod,message
                %d,100.00,test@example.com,Test,User,CARD,
                """, testCampaign.getId());

        File csvFile = createTempCsvFile(csvContent);

        given()
            .multiPart("file", csvFile, "text/csv")
        .when()
            .post("/bulk-donations/upload")
        .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("Should return 400 for empty file")
    void shouldReturn400ForEmptyFile() throws IOException {
        File csvFile = createTempCsvFile("");

        given()
            .header("Authorization", "Bearer " + adminToken)
            .multiPart("file", csvFile, "text/csv")
        .when()
            .post("/bulk-donations/upload")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should return 400 for non-CSV file")
    void shouldReturn400ForNonCsvFile() throws IOException {
        File tempFile = File.createTempFile("donations", ".txt");
        tempFile.deleteOnExit();
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("not a csv");
        }

        given()
            .header("Authorization", "Bearer " + adminToken)
            .multiPart("file", tempFile, "text/plain")
        .when()
            .post("/bulk-donations/upload")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should return 206 for partial success")
    void shouldReturn206ForPartialSuccess() throws IOException {
        String csvContent = String.format("""
                campaignId,amount,donorEmail,donorFirstName,donorLastName,paymentMethod,message
                %d,100.00,valid@example.com,Valid,User,CARD,
                999999,-50.00,invalid@example.com,Invalid,User,CARD,
                %d,75.00,another@example.com,Another,User,BANK_TRANSFER,
                """, testCampaign.getId(), testCampaign.getId());

        File csvFile = createTempCsvFile(csvContent);

        given()
            .header("Authorization", "Bearer " + adminToken)
            .multiPart("file", csvFile, "text/csv")
        .when()
            .post("/bulk-donations/upload")
        .then()
            .statusCode(206)
            .body("successCount", equalTo(2))
            .body("failureCount", equalTo(1));
    }

    @Test
    @DisplayName("Should return 400 when all donations fail")
    void shouldReturn400WhenAllDonationsFail() throws IOException {
        String csvContent = """
                campaignId,amount,donorEmail,donorFirstName,donorLastName,paymentMethod,message
                999999,100.00,test1@example.com,Test,User1,CARD,
                999998,-50.00,test2@example.com,Test,User2,CARD,
                """;

        File csvFile = createTempCsvFile(csvContent);

        given()
            .header("Authorization", "Bearer " + adminToken)
            .multiPart("file", csvFile, "text/csv")
        .when()
            .post("/bulk-donations/upload")
        .then()
            .statusCode(400)
            .body("successCount", equalTo(0))
            .body("failureCount", equalTo(2));
    }

    @Test
    @DisplayName("Should process anonymous donations")
    void shouldProcessAnonymousDonations() throws IOException {
        String csvContent = String.format("""
                campaignId,amount,donorEmail,donorFirstName,donorLastName,paymentMethod,message
                %d,100.00,anonymous,,,CARD,Anonymous donation
                %d,50.00,,,,,Cash donation
                """, testCampaign.getId(), testCampaign.getId());

        File csvFile = createTempCsvFile(csvContent);

        given()
            .header("Authorization", "Bearer " + adminToken)
            .multiPart("file", csvFile, "text/csv")
        .when()
            .post("/bulk-donations/upload")
        .then()
            .statusCode(201)
            .body("successCount", equalTo(2))
            .body("failureCount", equalTo(0));

        // Verify anonymous donor was created
        assertThat(donorRepository.findByEmail("anonymous@donfundy.com")).isPresent();
    }

    @Test
    @DisplayName("Should handle invalid campaign ID")
    void shouldHandleInvalidCampaignId() throws IOException {
        String csvContent = """
                campaignId,amount,donorEmail,donorFirstName,donorLastName,paymentMethod,message
                999999,100.00,test@example.com,Test,User,CARD,
                """;

        File csvFile = createTempCsvFile(csvContent);

        given()
            .header("Authorization", "Bearer " + adminToken)
            .multiPart("file", csvFile, "text/csv")
        .when()
            .post("/bulk-donations/upload")
        .then()
            .statusCode(400)
            .body("failureCount", equalTo(1))
            .body("errors[0]", containsString("Campaign not found"));
    }

    @Test
    @DisplayName("Should handle inactive campaign")
    void shouldHandleInactiveCampaign() throws IOException {
        testCampaign.setStatus(Status.COMPLETED);
        campaignRepository.save(testCampaign);

        String csvContent = String.format("""
                campaignId,amount,donorEmail,donorFirstName,donorLastName,paymentMethod,message
                %d,100.00,test@example.com,Test,User,CARD,
                """, testCampaign.getId());

        File csvFile = createTempCsvFile(csvContent);

        given()
            .header("Authorization", "Bearer " + adminToken)
            .multiPart("file", csvFile, "text/csv")
        .when()
            .post("/bulk-donations/upload")
        .then()
            .statusCode(400)
            .body("failureCount", equalTo(1))
            .body("errors[0]", containsString("not active"));
    }

    @Test
    @DisplayName("Should handle invalid amount")
    void shouldHandleInvalidAmount() throws IOException {
        String csvContent = String.format("""
                campaignId,amount,donorEmail,donorFirstName,donorLastName,paymentMethod,message
                %d,-100.00,test@example.com,Test,User,CARD,
                %d,0,test2@example.com,Test,User2,CARD,
                """, testCampaign.getId(), testCampaign.getId());

        File csvFile = createTempCsvFile(csvContent);

        given()
            .header("Authorization", "Bearer " + adminToken)
            .multiPart("file", csvFile, "text/csv")
        .when()
            .post("/bulk-donations/upload")
        .then()
            .statusCode(400)
            .body("failureCount", equalTo(2))
            .body("successCount", equalTo(0));
    }

    @Test
    @DisplayName("Should handle invalid payment method")
    void shouldHandleInvalidPaymentMethod() throws IOException {
        String csvContent = String.format("""
                campaignId,amount,donorEmail,donorFirstName,donorLastName,paymentMethod,message
                %d,100.00,test@example.com,Test,User,INVALID_METHOD,
                """, testCampaign.getId());

        File csvFile = createTempCsvFile(csvContent);

        given()
            .header("Authorization", "Bearer " + adminToken)
            .multiPart("file", csvFile, "text/csv")
        .when()
            .post("/bulk-donations/upload")
        .then()
            .statusCode(400)
            .body("failureCount", equalTo(1))
            .body("errors[0]", containsString("Invalid payment method"));
    }

    @Test
    @DisplayName("Should update campaign raised amount")
    void shouldUpdateCampaignRaisedAmount() throws IOException {
        double initialRaised = testCampaign.getRaisedAmount();

        String csvContent = String.format("""
                campaignId,amount,donorEmail,donorFirstName,donorLastName,paymentMethod,message
                %d,100.00,donor1@example.com,Donor,One,CARD,
                %d,150.00,donor2@example.com,Donor,Two,BANK_TRANSFER,
                """, testCampaign.getId(), testCampaign.getId());

        File csvFile = createTempCsvFile(csvContent);

        given()
            .header("Authorization", "Bearer " + adminToken)
            .multiPart("file", csvFile, "text/csv")
        .when()
            .post("/bulk-donations/upload")
        .then()
            .statusCode(201)
            .body("successCount", equalTo(2));

        // Verify campaign raised amount was updated
        Campaign updatedCampaign = campaignRepository.findById(testCampaign.getId()).orElseThrow();
        assertThat(updatedCampaign.getRaisedAmount()).isEqualTo(initialRaised + 250.0);
    }

    @Test
    @DisplayName("Should mark campaign as completed when goal reached")
    void shouldMarkCampaignAsCompletedWhenGoalReached() throws IOException {
        testCampaign.setGoalAmount(200.0);
        testCampaign.setRaisedAmount(0.0);
        campaignRepository.save(testCampaign);

        String csvContent = String.format("""
                campaignId,amount,donorEmail,donorFirstName,donorLastName,paymentMethod,message
                %d,150.00,donor1@example.com,Donor,One,CARD,
                %d,100.00,donor2@example.com,Donor,Two,BANK_TRANSFER,
                """, testCampaign.getId(), testCampaign.getId());

        File csvFile = createTempCsvFile(csvContent);

        given()
            .header("Authorization", "Bearer " + adminToken)
            .multiPart("file", csvFile, "text/csv")
        .when()
            .post("/bulk-donations/upload")
        .then()
            .statusCode(201)
            .body("successCount", equalTo(2));

        // Verify campaign status changed to COMPLETED
        Campaign updatedCampaign = campaignRepository.findById(testCampaign.getId()).orElseThrow();
        assertThat(updatedCampaign.getStatus()).isEqualTo(Status.COMPLETED);
        assertThat(updatedCampaign.getRaisedAmount()).isEqualTo(250.0);
    }

    @Test
    @DisplayName("Should create new donors from CSV")
    void shouldCreateNewDonorsFromCsv() throws IOException {
        String csvContent = String.format("""
                campaignId,amount,donorEmail,donorFirstName,donorLastName,paymentMethod,message
                %d,100.00,newdonor1@example.com,New,Donor1,CARD,
                %d,50.00,newdonor2@example.com,New,Donor2,BANK_TRANSFER,
                """, testCampaign.getId(), testCampaign.getId());

        File csvFile = createTempCsvFile(csvContent);

        given()
            .header("Authorization", "Bearer " + adminToken)
            .multiPart("file", csvFile, "text/csv")
        .when()
            .post("/bulk-donations/upload")
        .then()
            .statusCode(201)
            .body("successCount", equalTo(2));

        // Verify new donors were created
        assertThat(donorRepository.findByEmail("newdonor1@example.com")).isPresent();
        assertThat(donorRepository.findByEmail("newdonor2@example.com")).isPresent();
    }
}
