package hr.algebra.donfundy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkDonationResult {
    private int totalRows;
    private int successCount;
    private int failureCount;
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
