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
        BigDecimal convertedAmount = amount;
        if (currency != account.getCurrency()) {
            BigDecimal usdAmount = currency.toUSD(amount);
            convertedAmount = account.getCurrency().fromUSD(usdAmount).setScale(Currency.SCALE, Currency.ROUNDING_MODE);
        }
        account.credit(convertedAmount);
    }

    public void withdraw(String id, BigDecimal amount, Currency currency) {
        Account account = requireAccount(id);
        if (currency != account.getCurrency()) {
            throw new IllegalArgumentException("cannot withdraw with different currency");
        }
        account.debit(amount);
    }

    public void transfer(String fromId, String toId, BigDecimal amount) {
        Account sender = requireAccount(fromId);
        Account recipient = requireAccount(toId);

        if (fromId.equals(toId)) {
            throw new IllegalArgumentException("currency mismatch");
        }

        if (sender.getCurrency() != recipient.getCurrency()) {
            throw new IllegalStateException("currency mismatch");
        }

        sender.debit(amount);
        recipient.credit(amount);
    }


}
