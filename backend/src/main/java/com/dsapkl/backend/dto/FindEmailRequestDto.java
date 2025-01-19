package com.dsapkl.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindEmailRequestDto {
    @NotEmpty(message = "Birth date is required.")
    @Pattern(regexp = "\\d{8}", message = "Please enter the birth date in 8-digit format (YYYYMMDD).")
    private String birthDate;

    @NotEmpty(message = "Phone number is required.")
    @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$",
            message = "Invalid phone number format.")
    private String phoneNumber;
}
