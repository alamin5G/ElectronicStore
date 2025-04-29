package com.goonok.electronicstore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketTrackingForm {

    @NotNull(message = "Ticket ID is required")
    private Long ticketId;

    @NotEmpty(message = "Email address is required")
    @Email(message = "Please provide a valid email address")
    private String email;
}