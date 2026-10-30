package dev.extramix;

import java.math.BigDecimal;

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
}
