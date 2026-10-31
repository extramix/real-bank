package dev.extramix;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class Bank {

    private final Map<String, Account> accounts = new HashMap<>();

    private Account requireAccount(String id) {
        Account account = accounts.get(id);
        if (account == null) {
            throw new IllegalArgumentException("unknown account: " + id);
        }

        return account;
    }


    public Account openAccount(String id, Currency currency) {

        Account newAccount = new Account(id, currency);
        Account existing = accounts.putIfAbsent(id, newAccount);
        if (existing != null) {
            throw new IllegalStateException("account with this id already exists");
        }
        return newAccount;
    }

    public void deposit(String id, BigDecimal amount, Currency currency) {
        Account account = requireAccount(id);
        if (currency != account.getCurrency()) {
            throw new IllegalArgumentException("currency mismatch");
        }
        account.credit(amount);
    }

    public void withdraw(String id, BigDecimal amount, Currency currency) {
        Account account = requireAccount(id);
        if (currency != account.getCurrency()) {
            throw new IllegalArgumentException("currency mismatch");
        }
        account.debit(amount);
    }


}
