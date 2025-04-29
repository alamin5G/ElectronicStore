package com.goonok.electronicstore.dto;

import com.goonok.electronicstore.enums.ContactMessageStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ContactMessageDto {

    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;


    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Subject is required")
    @Size(min = 5, max = 200, message = "Subject must be between 5 and 200 characters")
    private String subject;

    @NotBlank(message = "Message is required")
    @Size(min = 10, max = 2000, message = "Message must be between 10 and 2000 characters")
    private String message;




    private boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    // Additional fields for admin tracking
    private String adminNotes;
    private Long assignedAdminId;
    private String assignedAdminName;
    private LocalDateTime lastUpdatedAt;
    private LocalDateTime resolvedAt;
    private ContactMessageStatus status = ContactMessageStatus.NEW; // Default value
}