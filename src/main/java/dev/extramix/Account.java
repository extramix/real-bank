package dev.extramix;

import java.math.BigDecimal;
import java.util.Objects;

public class Account {
    private final String id;
    private final Currency currency;
    private BigDecimal balance;

    public Account(String id, Currency currency) {
        this.id = id;
        this.currency = currency;
        this.balance = BigDecimal.ZERO;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void credit(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount is required");
        requirePositive(amount);
        balance = this.balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount is required");
        requirePositive(amount);
        if(balance.compareTo(amount) < 0) {
            throw new IllegalStateException("insufficient funds");
        }
        balance = balance.subtract(amount);
    }

    private void requirePositive(BigDecimal amount) {
        if(amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }
}
