package com.example.user_service.vo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RequestLogin {

    @NotNull(message = "Email cannot be null")
    @Size(min = 2, message = "Email not be less than to characters")
    private String email;
    @NotNull(message = "Password cannot be null")
    @Size(min = 8, message = "")
    private String password;
}
