package hr.algebra.donfundy.controller;

import hr.algebra.donfundy.dto.BulkDonationResult;
import hr.algebra.donfundy.service.BulkDonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/bulk-donations")
@RequiredArgsConstructor
public class BulkDonationController {

    private final BulkDonationService bulkDonationService;


    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BulkDonationResult> uploadBulkDonations(
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
