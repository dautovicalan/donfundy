package hr.algebra.donfundy.dto;

import hr.algebra.donfundy.domain.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Donation creation request")
public class DonationRequest {

    @Schema(description = "ID of the campaign to donate to", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Campaign ID is required")
    private Long campaignId;

    @Schema(description = "ID of the donor making the donation", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Donor ID is required")
    private Long donorId;

    @Schema(description = "Donation amount", example = "100.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @Schema(description = "Optional message from the donor", example = "Keep up the great work!")
    private String message;

    @Schema(description = "Payment method used", example = "CARD", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}
