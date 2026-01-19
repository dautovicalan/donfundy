package hr.algebra.donfundy.controller;

import hr.algebra.donfundy.domain.enums.Status;
import hr.algebra.donfundy.dto.CampaignRequest;
import hr.algebra.donfundy.dto.CampaignResponse;
import hr.algebra.donfundy.service.CampaignService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/campaigns")
@RequiredArgsConstructor
@Tag(name = "Campaigns", description = "Campaign management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class CampaignController {

    private final CampaignService campaignService;

    @Operation(summary = "Get all campaigns", description = "Retrieve all campaigns, optionally filtered by status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of campaigns retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CampaignResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<List<CampaignResponse>> getAllCampaigns(
            @Parameter(description = "Filter by campaign status") @RequestParam(required = false) Status status
    ) {
        if (status != null) {
            return ResponseEntity.ok(campaignService.findByStatus(status));
        }
        return ResponseEntity.ok(campaignService.findAll());
    }

    @Operation(summary = "Get my campaigns", description = "Retrieve campaigns created by the current user")
    @ApiResponse(responseCode = "200", description = "List of user's campaigns",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CampaignResponse.class))))
    @GetMapping("/my-campaigns")
    public ResponseEntity<List<CampaignResponse>> getMyCampaigns() {
        return ResponseEntity.ok(campaignService.findByCurrentUser());
    }

    @Operation(summary = "Get campaign by ID", description = "Retrieve a specific campaign by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Campaign found",
                    content = @Content(schema = @Schema(implementation = CampaignResponse.class))),
            @ApiResponse(responseCode = "404", description = "Campaign not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CampaignResponse> getCampaignById(
            @Parameter(description = "Campaign ID") @PathVariable Long id) {
        return ResponseEntity.ok(campaignService.findById(id));
    }

    @Operation(summary = "Create campaign", description = "Create a new campaign (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Campaign created successfully",
                    content = @Content(schema = @Schema(implementation = CampaignResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CampaignResponse> createCampaign(@Valid @RequestBody CampaignRequest request) {
        CampaignResponse created = campaignService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update campaign", description = "Update an existing campaign (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Campaign updated successfully",
                    content = @Content(schema = @Schema(implementation = CampaignResponse.class))),
            @ApiResponse(responseCode = "404", description = "Campaign not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CampaignResponse> updateCampaign(
            @Parameter(description = "Campaign ID") @PathVariable Long id,
            @Valid @RequestBody CampaignRequest request
    ) {
        return ResponseEntity.ok(campaignService.update(id, request));
    }

    @Operation(summary = "Delete campaign", description = "Delete a campaign (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Campaign deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Campaign not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCampaign(
            @Parameter(description = "Campaign ID") @PathVariable Long id) {
        campaignService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
