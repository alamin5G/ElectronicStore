package com.goonok.electronicstore.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordByAdminDto {


    @NotEmpty(message = "New password is required")
    @Size(min = 6, message = "New password must be at least 6 characters")
    private String password;


}
