package hr.algebra.donfundy.controller;

import hr.algebra.donfundy.dto.DonationRequest;
import hr.algebra.donfundy.dto.DonationResponse;
import hr.algebra.donfundy.service.DonationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/donations")
@RequiredArgsConstructor
@Tag(name = "Donations", description = "Donation management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class DonationController {

    private final DonationService donationService;

    @Operation(summary = "Get all donations", description = "Retrieve all donations, optionally filtered by campaign or donor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of donations retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DonationResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<List<DonationResponse>> getAllDonations(
            @Parameter(description = "Filter by campaign ID") @RequestParam(required = false) Long campaignId,
            @Parameter(description = "Filter by donor ID") @RequestParam(required = false) Long donorId
    ) {
        if (campaignId != null) {
            return ResponseEntity.ok(donationService.findByCampaignId(campaignId));
        }
        if (donorId != null) {
            return ResponseEntity.ok(donationService.findByDonorId(donorId));
        }
        return ResponseEntity.ok(donationService.findAll());
    }

    @Operation(summary = "Get donation by ID", description = "Retrieve a specific donation by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Donation found",
                    content = @Content(schema = @Schema(implementation = DonationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Donation not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DonationResponse> getDonationById(
            @Parameter(description = "Donation ID") @PathVariable Long id) {
        return ResponseEntity.ok(donationService.findById(id));
    }

    @Operation(summary = "Create donation", description = "Create a new donation for a campaign")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Donation created successfully",
                    content = @Content(schema = @Schema(implementation = DonationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Campaign or donor not found")
    })
    @PostMapping
    public ResponseEntity<DonationResponse> createDonation(@Valid @RequestBody DonationRequest request) {
        DonationResponse created = donationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Delete donation", description = "Delete a donation by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Donation deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Donation not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDonation(
            @Parameter(description = "Donation ID") @PathVariable Long id) {
        donationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
