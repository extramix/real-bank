package dev.extramix;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BankTest {
    @Test
    void openAccountCreatesAccount() {
        Bank bank = new Bank();

        Account account = bank.openAccount("a1", Currency.USD);

        assertEquals("a1", account.getId());
        assertEquals(0, account.getBalance().compareTo(BigDecimal.ZERO));
        assertEquals(Currency.USD, account.getCurrency());
    }

    @Test
    void openAccountThenAccountIsUsable() {
        Bank bank = new Bank();
        Account account = bank.openAccount("a1", Currency.USD);
        bank.deposit(account.getId(), new BigDecimal("100"), Currency.USD);


        assertEquals(0, account.getBalance().compareTo(new BigDecimal("100")));

    }

    @Test
    void openAccountWithDuplicatedIdIsRejected() {
        Bank bank = new Bank();
        Account account = bank.openAccount("a1", Currency.USD);
        bank.deposit(account.getId(), new BigDecimal("100"), Currency.USD);

        assertThrows(IllegalStateException.class, () -> bank.openAccount("a1", Currency.THB));
        assertEquals(0, account.getBalance().compareTo(new BigDecimal("100")));
        assertEquals(Currency.USD, account.getCurrency());
    }

    @Test
    void depositToUnknownAccountsIsRejected() {
        Bank bank = new Bank();
        bank.openAccount("a1", Currency.USD);

        assertThrows(IllegalArgumentException.class, () -> bank.deposit("a2", new BigDecimal("100"), Currency.USD));
    }

    @Test
    void depositNegativeValuesIsRejected() {
        Bank bank = new Bank();
        Account account = bank.openAccount("a1", Currency.USD);

        assertThrows(IllegalArgumentException.class, () -> bank.deposit(account.getId(), new BigDecimal("-1"), account.getCurrency()));
    }

    @Test
    void depositWithDifferentCurrencyIsRejected() {
        Bank bank = new Bank();
        Account account = bank.openAccount("a1", Currency.USD);
        bank.deposit(account.getId(), new BigDecimal("100"), Currency.USD);

        assertThrows(IllegalArgumentException.class, () -> bank.deposit(account.getId(), new BigDecimal("100"), Currency.THB));
    }

    @Test
    void depositGoesToTheNamedAccountOnly() {
        Bank bank = new Bank();
        Account account1 = bank.openAccount("a1", Currency.USD);
        Account account2 = bank.openAccount("a2", Currency.USD);

        bank.deposit("a2", new BigDecimal("100"), Currency.USD);

        assertEquals(0, account1.getBalance().compareTo(BigDecimal.ZERO));
        assertEquals(0, account2.getBalance().compareTo(new BigDecimal("100")));
    }

    @Test
    void withdrawFromAccountDecreasesBalance() {
        Bank bank = new Bank();
        Account account = bank.openAccount("a1", Currency.USD);

        bank.deposit(account.getId(), new BigDecimal("100"), Currency.USD);
        bank.withdraw(account.getId(), new BigDecimal("40"), Currency.USD);

        assertEquals(0, account.getBalance().compareTo(new BigDecimal("60")));
    }

    @Test
    void withdrawFromUnknownAccountIsRejected() {
        Bank bank = new Bank();
        Account account = bank.openAccount("a1", Currency.USD);
        bank.deposit(account.getId(), new BigDecimal("100"), Currency.USD);


        assertThrows(IllegalArgumentException.class, () -> bank.withdraw("a2", new BigDecimal("50"), Currency.USD));
    }

    @Test
    void withdrawNegativeValuesIsRejected() {
        Bank bank = new Bank();
        Account account = bank.openAccount("a1", Currency.USD);
        bank.deposit(account.getId(), new BigDecimal("100"), Currency.USD);

        assertThrows(IllegalArgumentException.class, () -> bank.withdraw(account.getId(), new BigDecimal("-1"), account.getCurrency()));
    }

    @Test
    void withdrawWithDifferentCurrencyIsRejected() {
        Bank bank = new Bank();
        Account account = bank.openAccount("a1", Currency.USD);
        bank.deposit(account.getId(), new BigDecimal("100"), Currency.USD);

        assertThrows(IllegalArgumentException.class, () -> bank.withdraw(account.getId(), new BigDecimal("100"), Currency.THB));
    }

    @Test
    void withdrawMoreThanBalanceThroughBankIsRejectedAndChangesNothing() {
        Bank bank = new Bank();
        Account account = bank.openAccount("a1", Currency.USD);
        bank.deposit(account.getId(), new BigDecimal("100"), Currency.USD);

        assertThrows(IllegalStateException.class, () -> bank.withdraw(account.getId(), new BigDecimal("101"), Currency.USD));
        assertEquals(0, account.getBalance().compareTo(new BigDecimal("100")));
    }
}