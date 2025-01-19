package com.dsapkl.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberForm {

    @NotEmpty(message = "Name is required.")
    private String name;

    @NotEmpty(message = "Email is required.")
    @Email
    private String email;

    @NotEmpty(message = "Password is required.")
    private String password;

    private String city;
    private String street;
    private String zipcode;

    private String role;

    @NotEmpty(message = "Birth date is required.")
    @Pattern(regexp = "\\d{8}", message = "Please enter the birth date in 8-digit format (YYYYMMDD).")
    private String birthDate;

    @NotEmpty(message = "Phone number is required.")
    @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$",
            message = "Invalid phone number format. (e.g., 010-1234-5678)")
    private String phoneNumber;
}