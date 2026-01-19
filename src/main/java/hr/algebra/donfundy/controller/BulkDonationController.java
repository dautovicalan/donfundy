package hr.algebra.donfundy.controller;

import hr.algebra.donfundy.dto.BulkDonationResult;
import hr.algebra.donfundy.service.BulkDonationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/bulk-donations")
@RequiredArgsConstructor
@Tag(name = "Bulk Donations", description = "Bulk donation import endpoints (Admin only)")
@SecurityRequirement(name = "bearerAuth")
public class BulkDonationController {

    private final BulkDonationService bulkDonationService;

    @Operation(summary = "Upload bulk donations CSV",
            description = "Upload a CSV file containing multiple donations. CSV format: campaignId,amount,donorEmail,donorFirstName,donorLastName,paymentMethod,message")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "All donations processed successfully",
                    content = @Content(schema = @Schema(implementation = BulkDonationResult.class))),
            @ApiResponse(responseCode = "206", description = "Partial success - some donations failed",
                    content = @Content(schema = @Schema(implementation = BulkDonationResult.class))),
            @ApiResponse(responseCode = "400", description = "All donations failed or invalid file",
                    content = @Content(schema = @Schema(implementation = BulkDonationResult.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BulkDonationResult> uploadBulkDonations(
            @Parameter(description = "CSV file with donations", required = true)
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            BulkDonationResult result = new BulkDonationResult();
            result.addError(0, "File is empty");
            return ResponseEntity.badRequest().body(result);
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            BulkDonationResult result = new BulkDonationResult();
            result.addError(0, "Only CSV files are allowed");
            return ResponseEntity.badRequest().body(result);
        }

        BulkDonationResult result = bulkDonationService.processBulkDonations(file);

        if (result.getFailureCount() > 0 && result.getSuccessCount() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        } else if (result.getFailureCount() > 0) {
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(result);
        } else {
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        }
    }
}
