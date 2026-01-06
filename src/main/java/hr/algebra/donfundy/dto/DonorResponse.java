package hr.algebra.donfundy.dto;

import lombok.Data;

@Data
public class DonorResponse {
    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
}
