package com.dsapkl.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginForm {

    @NotEmpty(message = "Email address is required.")
    @Email
    private String email;

    @NotEmpty(message = "Password is required.")
    private String password;
}
