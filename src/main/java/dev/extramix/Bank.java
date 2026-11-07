package dev.extramix;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class Bank {

    private final Map<String, Account> accounts = new HashMap<>();
    private final Map<String, List<Transaction>> history = new HashMap<>();

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
        history.computeIfAbsent(id, k -> new ArrayList<>());
        return newAccount;
    }

    public void deposit(String id, BigDecimal amount, Currency currency) {
        Account account = requireAccount(id);
        BigDecimal convertedAmount = amount;
        if (currency != account.getCurrency()) {
            convertedAmount = convert(amount, currency, account.getCurrency());
        }
        account.credit(convertedAmount);
        appendHistory(id, new Transaction(amount, currency, account.getBalance(), TransactionType.DEPOSIT));
    }

    public void withdraw(String id, BigDecimal amount, Currency currency) {
        Account account = requireAccount(id);
        if (currency != account.getCurrency()) {
            throw new IllegalArgumentException("cannot withdraw with different currency");
        }
        account.debit(amount);
        appendHistory(id, new Transaction(amount, currency, account.getBalance(), TransactionType.WITHDRAW));

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
            appendHistory(sender.getId(), new Transaction(amount, sender.getCurrency(), sender.getBalance(), TransactionType.TRANSFER_OUT));
            appendHistory(recipient.getId(), new Transaction(toBeTransferredAmount, recipient.getCurrency(), recipient.getBalance(), TransactionType.TRANSFER_IN));
        }
    }

    public List<Transaction> getHistory(String id) {
        if (!history.containsKey(id)) {
            throw new IllegalArgumentException("account not found");
        }
        return List.copyOf(history.get(id));
    }

    private void appendHistory(String id, Transaction transaction) {
        history.computeIfAbsent(id, k -> new ArrayList<>()).add(transaction);
    }


}
