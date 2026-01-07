package hr.algebra.donfundy.service;

import hr.algebra.donfundy.domain.Campaign;
import hr.algebra.donfundy.domain.Donor;
import hr.algebra.donfundy.domain.enums.PaymentMethod;
import hr.algebra.donfundy.domain.enums.Status;
import hr.algebra.donfundy.dto.BulkDonationResult;
import hr.algebra.donfundy.repository.CampaignRepository;
import hr.algebra.donfundy.repository.DonorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class BulkDonationService {

    private final JdbcTemplate jdbcTemplate;
    private final DonorRepository donorRepository;
    private final CampaignRepository campaignRepository;

    private static final String ANONYMOUS_EMAIL = "anonymous@donfundy.com";
    private static final String INSERT_DONATION_SQL =
            "INSERT INTO donation (campaign_id, donor_id, amount, donation_date, message, payment_method) " +
            "VALUES (?, ?, ?, ?, ?, ?)";


    @Transactional
    public BulkDonationResult processBulkDonations(MultipartFile file) {
        BulkDonationResult result = new BulkDonationResult();

        try {
            List<DonationRecord> donations = parseCsvFile(file, result);
            result.setTotalRows(donations.size());

            if (donations.isEmpty()) {
                log.warn("No valid donations found in CSV file");
                return result;
            }

            Donor anonymousDonor = getOrCreateAnonymousDonor();

            Map<String, Donor> donorCache = new HashMap<>();
            donorCache.put(ANONYMOUS_EMAIL, anonymousDonor);

            for (DonationRecord record : donations) {
                if (!donorCache.containsKey(record.email)) {
                    Donor donor = getOrCreateDonor(record);
                    donorCache.put(record.email, donor);
                }
            }

            insertDonationsBatch(donations, donorCache);

            updateCampaignAmounts(donations);

            result.setSuccessCount(donations.size());
            log.info("Successfully processed {} donations", donations.size());

        } catch (Exception e) {
            log.error("Error processing bulk donations", e);
            result.addError(0, "Failed to process file: " + e.getMessage());
            result.incrementFailure();
        }

        return result;
    }


    private List<DonationRecord> parseCsvFile(MultipartFile file, BulkDonationResult result) throws Exception {
        List<DonationRecord> donations = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            CSVParser csvParser = CSVFormat.DEFAULT
                    .builder()
                    .setHeader("campaignId", "amount", "donorEmail", "donorFirstName", "donorLastName",
                              "paymentMethod", "message")
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .build()
                    .parse(reader);

            int rowNumber = 1;
            for (CSVRecord record : csvParser) {
                rowNumber++;
                try {
                    DonationRecord donation = parseDonationRecord(record);
                    donations.add(donation);
                } catch (Exception e) {
                    result.addError(rowNumber, e.getMessage());
                    result.incrementFailure();
                }
            }
        }

        return donations;
    }


    private DonationRecord parseDonationRecord(CSVRecord record) {
        DonationRecord donation = new DonationRecord();

        String campaignIdStr = record.get("campaignId");
        if (campaignIdStr == null || campaignIdStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Campaign ID is required");
        }
        donation.campaignId = Long.parseLong(campaignIdStr.trim());

        Campaign campaign = campaignRepository.findById(donation.campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found: " + donation.campaignId));

        if (campaign.getStatus() != Status.ACTIVE) {
            throw new IllegalArgumentException("Campaign is not active: " + donation.campaignId);
        }

        String amountStr = record.get("amount");
        if (amountStr == null || amountStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Amount is required");
        }
        donation.amount = new BigDecimal(amountStr.trim());
        if (donation.amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        donation.email = record.get("donorEmail");
        if (donation.email == null || donation.email.trim().isEmpty() ||
            donation.email.trim().equalsIgnoreCase("anonymous")) {
            donation.email = ANONYMOUS_EMAIL;
            donation.firstName = "Anonymous";
            donation.lastName = "Donor";
        } else {
            donation.email = donation.email.trim().toLowerCase();
            donation.firstName = record.get("donorFirstName");
            donation.lastName = record.get("donorLastName");

            if (donation.firstName == null || donation.firstName.trim().isEmpty()) {
                donation.firstName = "Unknown";
            }
            if (donation.lastName == null || donation.lastName.trim().isEmpty()) {
                donation.lastName = "Donor";
            }
        }

        String paymentMethodStr = record.get("paymentMethod");
        if (paymentMethodStr == null || paymentMethodStr.trim().isEmpty()) {
            donation.paymentMethod = PaymentMethod.CARD;
        } else {
            try {
                donation.paymentMethod = PaymentMethod.valueOf(paymentMethodStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid payment method: " + paymentMethodStr);
            }
        }

        donation.message = record.get("message");
        if (donation.message != null && donation.message.trim().isEmpty()) {
            donation.message = null;
        }

        donation.donationDate = LocalDate.now();

        return donation;
    }


    private Donor getOrCreateAnonymousDonor() {
        return donorRepository.findByEmail(ANONYMOUS_EMAIL)
                .orElseGet(() -> {
                    Donor anonymous = new Donor();
                    anonymous.setEmail(ANONYMOUS_EMAIL);
                    anonymous.setFirstName("Anonymous");
                    anonymous.setLastName("Donor");
                    anonymous.setUser(null);
                    Donor saved = donorRepository.save(anonymous);
                    log.info("Created anonymous donor with ID: {}", saved.getId());
                    return saved;
                });
    }


    private Donor getOrCreateDonor(DonationRecord record) {
        return donorRepository.findByEmail(record.email)
                .orElseGet(() -> {
                    Donor donor = new Donor();
                    donor.setEmail(record.email);
                    donor.setFirstName(record.firstName);
                    donor.setLastName(record.lastName);
                    donor.setUser(null);
                    Donor saved = donorRepository.save(donor);
                    log.info("Created new donor: {} with ID: {}", record.email, saved.getId());
                    return saved;
                });
    }


    private void insertDonationsBatch(List<DonationRecord> donations, Map<String, Donor> donorCache) {
        jdbcTemplate.batchUpdate(INSERT_DONATION_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                DonationRecord donation = donations.get(i);
                Donor donor = donorCache.get(donation.email);

                ps.setLong(1, donation.campaignId);
                ps.setLong(2, donor.getId());
                ps.setBigDecimal(3, donation.amount);
                ps.setObject(4, donation.donationDate);
                ps.setString(5, donation.message);
                ps.setString(6, donation.paymentMethod.name());
            }

            @Override
            public int getBatchSize() {
                return donations.size();
            }
        });

        log.info("Batch inserted {} donations", donations.size());
    }


    private void updateCampaignAmounts(List<DonationRecord> donations) {
        Map<Long, BigDecimal> campaignTotals = new HashMap<>();
        for (DonationRecord donation : donations) {
            campaignTotals.merge(donation.campaignId, donation.amount, BigDecimal::add);
        }

        for (Map.Entry<Long, BigDecimal> entry : campaignTotals.entrySet()) {
            Campaign campaign = campaignRepository.findById(entry.getKey()).orElse(null);
            if (campaign != null) {
                double currentRaised = campaign.getRaisedAmount() != null ? campaign.getRaisedAmount() : 0.0;
                double newRaised = currentRaised + entry.getValue().doubleValue();
                campaign.setRaisedAmount(newRaised);

                if (newRaised >= campaign.getGoalAmount() && campaign.getStatus() == Status.ACTIVE) {
                    campaign.setStatus(Status.COMPLETED);
                }

                campaignRepository.save(campaign);
                log.info("Updated campaign {} raised amount to {}", entry.getKey(), newRaised);
            }
        }
    }

    private static class DonationRecord {
        Long campaignId;
        BigDecimal amount;
        String email;
        String firstName;
        String lastName;
        PaymentMethod paymentMethod;
        String message;
        LocalDate donationDate;
    }
}
