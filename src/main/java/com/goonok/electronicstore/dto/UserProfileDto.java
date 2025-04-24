package com.goonok.electronicstore.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List; // Import List

@Data
public class UserProfileDto {

    private Long userId;
    private String name;
    private String email; // Usually read-only on profile display
    private String phoneNumber; // Can be editable
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isVerified; // Useful to show verification status

    // We might include a summary or list of AddressDtos here later
    // private List<AddressDto> addresses;

    // We might include a summary or list of recent OrderDtos here later
    // private List<OrderSummaryDto> recentOrders;

}
