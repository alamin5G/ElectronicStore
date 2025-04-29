package com.goonok.electronicstore.model;

import com.goonok.electronicstore.enums.ContactMessageStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "contact_messages")
public class ContactMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Name is required")
    private String name;

    @NotEmpty(message = "Subject is required")
    private String subject;

    @NotEmpty(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotEmpty(message = "Message is required")
    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "read_status")
    private boolean read = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ContactMessageStatus status = ContactMessageStatus.NEW;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_admin_id")
    private Admin assignedAdmin;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime readAt;
    private LocalDateTime lastUpdatedAt;
    private LocalDateTime resolvedAt;
}