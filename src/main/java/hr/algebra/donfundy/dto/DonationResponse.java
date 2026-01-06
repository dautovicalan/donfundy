package hr.algebra.donfundy.dto;

import hr.algebra.donfundy.domain.enums.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DonationResponse {
    private Long id;
    private Long campaignId;
    private String campaignName;
    private Long donorId;
    private String donorName;
    private BigDecimal amount;
    private LocalDate donationDate;
    private String message;
    private PaymentMethod paymentMethod;
}
