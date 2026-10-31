package dev.extramix;

import java.util.HashMap;
import java.util.Map;

public class Bank {

    private final Map<String, Account> accounts = new HashMap<>();

    public Account openAccount(String id, Currency currency) {

        Account newAccount = new Account(id, currency);
        Account existing = accounts.putIfAbsent(id, newAccount);
        if (existing != null) {
            throw new IllegalStateException("account with this id already exists");
        }
        return newAccount;
    }

}
