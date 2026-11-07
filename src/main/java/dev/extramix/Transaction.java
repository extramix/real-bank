package dev.extramix;

import java.math.BigDecimal;

public record Transaction(BigDecimal amount, Currency currency, BigDecimal balance, TransactionType type) {
}
