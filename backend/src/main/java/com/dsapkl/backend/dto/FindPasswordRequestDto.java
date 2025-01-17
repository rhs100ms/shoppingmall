package com.dsapkl.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindPasswordRequestDto {

    @NotEmpty(message = "Email is required.")
    @Email(message = "Invalid email format.")
    private String email;

    @NotEmpty(message = "Phone number is required.")
    @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", message = "Invalid phone number format.")
    private String phoneNumber;
}
