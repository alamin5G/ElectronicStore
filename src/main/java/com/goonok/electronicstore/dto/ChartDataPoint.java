package com.goonok.electronicstore.dto;

import lombok.Data;
import lombok.NoArgsConstructor; // Keep if needed for JSON binding etc.

import java.math.BigDecimal;

@Data
@NoArgsConstructor // Keep if needed for other frameworks (like Jackson)
public class ChartDataPoint {
    private String label;
    private BigDecimal value;

    // Constructor for JPQL query with SUM returning BigDecimal (Ideal case)
    public ChartDataPoint(String label, BigDecimal value) {
        this.label = label;
        // Handle potential null from COALESCE if SUM was null and COALESCE didn't work as expected (unlikely)
        this.value = (value != null) ? value : BigDecimal.ZERO;
    }

    // Constructor for JPQL query if SUM returns Double
    public ChartDataPoint(String label, Double value) {
        this.label = label;
        this.value = (value != null) ? BigDecimal.valueOf(value) : BigDecimal.ZERO;
    }

    // Constructor for JPQL query with COUNT (which returns Long)
    // Used by your getLast7DaysOrderStats query before casting
    // Or if SUM unexpectedly returns Long
    public ChartDataPoint(String label, Long value) {
        this.label = label;
        this.value = (value != null) ? BigDecimal.valueOf(value) : BigDecimal.ZERO;
    }
}