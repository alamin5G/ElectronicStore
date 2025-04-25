package com.goonok.electronicstore.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_activity_logs")
public class UserActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String activityType; // LOGIN, LOGOUT, PASSWORD_CHANGE, PROFILE_UPDATE, etc.

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 100)
    private String userAgent;

    @Column
    private String details; // Additional activity-specific details

    @Column
    private boolean success; // Whether the activity was successful
}