package hr.algebra.donfundy.controller;

import hr.algebra.donfundy.dto.DonationRequest;
import hr.algebra.donfundy.dto.DonationResponse;
import hr.algebra.donfundy.service.DonationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/donations")
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;

    @GetMapping
    public ResponseEntity<List<DonationResponse>> getAllDonations(
            @RequestParam(required = false) Long campaignId,
            @RequestParam(required = false) Long donorId
    ) {
        if (campaignId != null) {
            return ResponseEntity.ok(donationService.findByCampaignId(campaignId));
        }
        if (donorId != null) {
            return ResponseEntity.ok(donationService.findByDonorId(donorId));
        }
        return ResponseEntity.ok(donationService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DonationResponse> getDonationById(@PathVariable Long id) {
        return ResponseEntity.ok(donationService.findById(id));
    }

    @PostMapping
    public ResponseEntity<DonationResponse> createDonation(@Valid @RequestBody DonationRequest request) {
        DonationResponse created = donationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDonation(@PathVariable Long id) {
        donationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
