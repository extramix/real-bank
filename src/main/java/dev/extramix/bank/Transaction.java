package dev.extramix.bank;

import java.math.BigDecimal;

public record Transaction(BigDecimal amount, Currency currency, BigDecimal balance, TransactionType type) {
}
