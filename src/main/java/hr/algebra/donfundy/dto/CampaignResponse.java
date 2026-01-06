package hr.algebra.donfundy.dto;

import hr.algebra.donfundy.domain.enums.Status;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CampaignResponse {
    private Long id;
    private String name;
    private String description;
    private Double goalAmount;
    private Double raisedAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Status status;
    private Double progressPercentage;
}
