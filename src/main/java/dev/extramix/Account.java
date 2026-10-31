package dev.extramix;

import java.math.BigDecimal;
import java.util.Objects;

public class Account {
    private final String id;
    private final Currency currency;
    private BigDecimal balance;

    public Account(String id, Currency currency) {
        Objects.requireNonNull(id, "id is required");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        Objects.requireNonNull(currency, "currency is required");
        this.id = id;
        this.currency = currency;
        this.balance = BigDecimal.ZERO;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getId() {
        return id;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void credit(BigDecimal amount) {
        // TODO: this only works in one thread because the balance could move between the read and act time.
        requirePositive(amount);
        balance = balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        requirePositive(amount);
        // TODO: this only works in one thread because the balance could move between the read and act time.
        if(balance.compareTo(amount) < 0) {
            throw new IllegalStateException("insufficient funds");
        }
        balance = balance.subtract(amount);
    }

    private void requirePositive(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount is required");
        if(amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }
}
