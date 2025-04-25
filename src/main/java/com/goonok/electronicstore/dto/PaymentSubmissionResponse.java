package com.goonok.electronicstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentSubmissionResponse {
    private boolean success;
    private String message;
    private String orderStatus;
}