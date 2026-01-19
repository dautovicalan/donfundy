package hr.algebra.donfundy.controller;

import hr.algebra.donfundy.dto.DonorRequest;
import hr.algebra.donfundy.dto.DonorResponse;
import hr.algebra.donfundy.service.DonorService;
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
@RequestMapping("/donors")
@RequiredArgsConstructor
@Tag(name = "Donors", description = "Donor management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class DonorController {

    private final DonorService donorService;

    @Operation(summary = "Get all donors", description = "Retrieve all donors in the system")
    @ApiResponse(responseCode = "200", description = "List of donors retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = DonorResponse.class))))
    @GetMapping
    public ResponseEntity<List<DonorResponse>> getAllDonors() {
        return ResponseEntity.ok(donorService.findAll());
    }

    @Operation(summary = "Get current user's donor profile", description = "Retrieve the donor profile of the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Donor profile found",
                    content = @Content(schema = @Schema(implementation = DonorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Donor profile not found")
    })
    @GetMapping("/me")
    public ResponseEntity<DonorResponse> getCurrentUserDonor() {
        return ResponseEntity.ok(donorService.findCurrentUserDonor());
    }

    @Operation(summary = "Get donor by ID", description = "Retrieve a specific donor by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Donor found",
                    content = @Content(schema = @Schema(implementation = DonorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Donor not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DonorResponse> getDonorById(
            @Parameter(description = "Donor ID") @PathVariable Long id) {
        return ResponseEntity.ok(donorService.findById(id));
    }

    @Operation(summary = "Get donor by user ID", description = "Retrieve a donor by their associated user ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Donor found",
                    content = @Content(schema = @Schema(implementation = DonorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Donor not found")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<DonorResponse> getDonorByUserId(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        return ResponseEntity.ok(donorService.findByUserId(userId));
    }

    @Operation(summary = "Create donor", description = "Create a new donor profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Donor created successfully",
                    content = @Content(schema = @Schema(implementation = DonorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping
    public ResponseEntity<DonorResponse> createDonor(@Valid @RequestBody DonorRequest request) {
        DonorResponse created = donorService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update donor", description = "Update an existing donor profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Donor updated successfully",
                    content = @Content(schema = @Schema(implementation = DonorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Donor not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PutMapping("/{id}")
    public ResponseEntity<DonorResponse> updateDonor(
            @Parameter(description = "Donor ID") @PathVariable Long id,
            @Valid @RequestBody DonorRequest request
    ) {
        return ResponseEntity.ok(donorService.update(id, request));
    }

    @Operation(summary = "Delete donor", description = "Delete a donor profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Donor deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Donor not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDonor(
            @Parameter(description = "Donor ID") @PathVariable Long id) {
        donorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
