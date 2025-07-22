package com.datashift.datashift_v2.io;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    
    @NotBlank
    @Size(max = 20, message = "Username must be less than or equal to 20 characters")
    private String name;

    @Email
    @NotBlank
    @Size(max = 50, message = "Email must be less than or equal to 50 characters")
    private String email;

    @NotBlank
    @Size(min = 6, max = 70)
    private String password;

    @NotBlank(message = "Role cannot be blank")
    @Size(max = 20, message = "Role must be less than or equal to 20 characters") 
    private String role;

}
