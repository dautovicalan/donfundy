package hr.algebra.donfundy.dto;

import hr.algebra.donfundy.domain.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Campaign response with full details")
public class CampaignResponse {

    @Schema(description = "Campaign unique identifier", example = "1")
    private Long id;

    @Schema(description = "Campaign name", example = "Help Build a School")
    private String name;

    @Schema(description = "Campaign description", example = "We are raising funds to build a school")
    private String description;

    @Schema(description = "Fundraising goal amount", example = "10000.00")
    private Double goalAmount;

    @Schema(description = "Total amount raised so far", example = "5500.00")
    private Double raisedAmount;

    @Schema(description = "Campaign start date", example = "2024-01-01")
    private LocalDate startDate;

    @Schema(description = "Campaign end date", example = "2024-12-31")
    private LocalDate endDate;

    @Schema(description = "Campaign status", example = "ACTIVE")
    private Status status;

    @Schema(description = "Progress towards goal as percentage", example = "55.0")
    private Double progressPercentage;

    @Schema(description = "ID of the user who created the campaign", example = "1")
    private Long createdById;

    @Schema(description = "Name of the user who created the campaign", example = "John Doe")
    private String createdByName;

    @Schema(description = "Email of the user who created the campaign", example = "john@example.com")
    private String createdByEmail;
}
