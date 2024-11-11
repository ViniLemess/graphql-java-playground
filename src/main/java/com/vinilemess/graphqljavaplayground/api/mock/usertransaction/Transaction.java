package com.vinilemess.graphqljavaplayground.api.mock.usertransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Transaction(
        String id,
        LocalDateTime dateTime,
        BigDecimal amount
) {
}
