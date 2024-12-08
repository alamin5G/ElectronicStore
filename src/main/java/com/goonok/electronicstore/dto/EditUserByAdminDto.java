package com.goonok.electronicstore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class EditUserByAdminDto {

    private int id;



    @NotEmpty(message = "email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotEmpty(message = "phone is required")
    @Size(min = 11, max = 11, message = "Phone number can't be less or more than 11 digits")
    @Pattern(regexp = "^\\+?[0-9. ()-]{11}$", message = "Phone number is invalid")
    private String phone;

    private boolean enabled;
    // No password field, as we are not updating the password here
}
