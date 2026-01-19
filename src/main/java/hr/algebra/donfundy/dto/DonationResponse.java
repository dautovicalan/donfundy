package hr.algebra.donfundy.dto;

import hr.algebra.donfundy.domain.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "Donation response with full details")
public class DonationResponse {

    @Schema(description = "Donation unique identifier", example = "1")
    private Long id;

    @Schema(description = "ID of the campaign donated to", example = "1")
    private Long campaignId;

    @Schema(description = "Name of the campaign donated to", example = "Help Build a School")
    private String campaignName;

    @Schema(description = "ID of the donor", example = "1")
    private Long donorId;

    @Schema(description = "Full name of the donor", example = "John Doe")
    private String donorName;

    @Schema(description = "Donation amount", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "Date when donation was made", example = "2024-03-15")
    private LocalDate donationDate;

    @Schema(description = "Message from the donor", example = "Keep up the great work!")
    private String message;

    @Schema(description = "Payment method used", example = "CARD")
    private PaymentMethod paymentMethod;
}
