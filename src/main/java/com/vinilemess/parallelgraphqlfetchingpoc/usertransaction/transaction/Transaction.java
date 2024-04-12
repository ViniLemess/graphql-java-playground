package com.vinilemess.parallelgraphqlfetchingpoc.usertransaction.transaction;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document("transactions")
public record Transaction(
        @Id
        String id,
        BigDecimal amount
) {
}
