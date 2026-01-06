package hr.algebra.donfundy.controller;

import hr.algebra.donfundy.dto.DonorRequest;
import hr.algebra.donfundy.dto.DonorResponse;
import hr.algebra.donfundy.service.DonorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/donors")
@RequiredArgsConstructor
public class DonorController {

    private final DonorService donorService;

    @GetMapping
    public ResponseEntity<List<DonorResponse>> getAllDonors() {
        return ResponseEntity.ok(donorService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DonorResponse> getDonorById(@PathVariable Long id) {
        return ResponseEntity.ok(donorService.findById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<DonorResponse> getDonorByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(donorService.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<DonorResponse> createDonor(@Valid @RequestBody DonorRequest request) {
        DonorResponse created = donorService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DonorResponse> updateDonor(
            @PathVariable Long id,
            @Valid @RequestBody DonorRequest request
    ) {
        return ResponseEntity.ok(donorService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDonor(@PathVariable Long id) {
        donorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
