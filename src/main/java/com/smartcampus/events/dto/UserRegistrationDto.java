package com.smartcampus.events.dto;

import com.smartcampus.events.model.Department;
import com.smartcampus.events.model.Role;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegistrationDto {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be 2–100 characters")
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @Size(min = 3, max = 30, message = "Username must be 3–30 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can contain letters, numbers, dot, underscore, and hyphen only")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;

    private Department department;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    private Role role; // set by the controller based on the endpoint

    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }
}
