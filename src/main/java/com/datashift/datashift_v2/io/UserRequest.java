package com.datashift.datashift_v2.io;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequest {

    @NotBlank(message = "Name cannot be blank")
    @Size(min=2, message = "Name must not be less than 2 characters")
    private String name;

    @NotNull(message = "Email cannot be null")
    @Email(message = "Provide valid email address")
    private String email;

    @NotNull(message = "Password cannot be null")
    @Size(min=6, message = "Password must be at least 6 characters")
    private String password;

}
