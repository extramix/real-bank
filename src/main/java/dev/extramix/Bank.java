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
            convertedAmount = convert(amount, currency, account.getCurrency());
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

    private BigDecimal convert(BigDecimal sourceAmount, Currency sourceCurrency, Currency destinationCurrency) {
        BigDecimal usdAmount = sourceCurrency.toUSD(sourceAmount);
        return destinationCurrency.fromUSD(usdAmount).setScale(Currency.SCALE, Currency.ROUNDING_MODE);
    }

    public void transfer(String fromId, String toId, BigDecimal amount) {
        Account sender = requireAccount(fromId);
        Account recipient = requireAccount(toId);

        if (fromId.equals(toId)) {
            throw new IllegalArgumentException("self transfer not allowed");
        }
        BigDecimal toBeTransferredAmount = amount;
        if (sender.getCurrency() != recipient.getCurrency()) {
            toBeTransferredAmount = convert(amount, sender.getCurrency(), recipient.getCurrency());
        }
        if (toBeTransferredAmount.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("amount is zero after conversion");
        }

        synchronized (this) {
            if (sender.getBalance().compareTo(amount) < 0) {
                throw new IllegalStateException("insufficient balance");
            }
            sender.debit(amount);
            recipient.credit(toBeTransferredAmount);
        }
    }


}
