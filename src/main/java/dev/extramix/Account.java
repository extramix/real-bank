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
        BigDecimal trimmedAmount = amount.stripTrailingZeros();
        requirePrecision(trimmedAmount);
        balance = balance.add(trimmedAmount);
    }

    public void debit(BigDecimal amount) {
        requirePositive(amount);
        BigDecimal trimmedAmount = amount.stripTrailingZeros();
        requirePrecision(trimmedAmount);
        // TODO: this only works in one thread because the balance could move between the read and act time.
        if (trimmedAmount.compareTo(amount) < 0) {
            throw new IllegalStateException("insufficient funds");
        }
        balance = balance.subtract(trimmedAmount);
    }

    private void requirePositive(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount is required");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }

    private void requirePrecision(BigDecimal amount) {
        if (amount.scale() > Currency.SCALE) {
            throw new IllegalArgumentException("too many precisions");
        }
    }
}
