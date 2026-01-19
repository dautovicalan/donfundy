package hr.algebra.donfundy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Result of bulk donation CSV upload")
public class BulkDonationResult {

    @Schema(description = "Total number of rows processed", example = "10")
    private int totalRows;

    @Schema(description = "Number of successfully imported donations", example = "8")
    private int successCount;

    @Schema(description = "Number of failed donations", example = "2")
    private int failureCount;

    @Schema(description = "List of error messages for failed rows", example = "[\"Row 3: Invalid campaign ID\", \"Row 7: Amount must be positive\"]")
    private List<String> errors = new ArrayList<>();

    public void addError(int rowNumber, String error) {
        errors.add(String.format("Row %d: %s", rowNumber, error));
    }

    public void incrementSuccess() {
        successCount++;
    }

    public void incrementFailure() {
        failureCount++;
    }
}
