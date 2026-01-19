package hr.algebra.donfundy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Donor response with full details")
public class DonorResponse {

    @Schema(description = "Donor unique identifier", example = "1")
    private Long id;

    @Schema(description = "Associated user ID", example = "1")
    private Long userId;

    @Schema(description = "Donor's first name", example = "John")
    private String firstName;

    @Schema(description = "Donor's last name", example = "Doe")
    private String lastName;

    @Schema(description = "Donor's email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Donor's phone number", example = "+1234567890")
    private String phoneNumber;
}
