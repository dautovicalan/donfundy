package hr.algebra.donfundy.dto;

import hr.algebra.donfundy.domain.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Campaign creation/update request")
public class CampaignRequest {

    @Schema(description = "Campaign name", example = "Help Build a School", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Campaign name is required")
    private String name;

    @Schema(description = "Campaign description", example = "We are raising funds to build a school in a rural area")
    private String description;

    @Schema(description = "Fundraising goal amount", example = "10000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Goal amount is required")
    @Positive(message = "Goal amount must be positive")
    private Double goalAmount;

    @Schema(description = "Campaign start date", example = "2024-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @Schema(description = "Campaign end date (optional)", example = "2024-12-31")
    private LocalDate endDate;

    @Schema(description = "Campaign status", example = "ACTIVE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Status is required")
    private Status status;
}
