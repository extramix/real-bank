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
        account.credit(new BigDecimal("100"));

        assertEquals(0, account.getBalance().compareTo(new BigDecimal("100")));

    }

    @Test
    void openAccountWithDuplicatedIdIsRejected() {
        Bank bank = new Bank();
        Account account = bank.openAccount("a1", Currency.USD);
        account.credit(new BigDecimal("100"));

        assertThrows(IllegalStateException.class, () -> bank.openAccount("a1", Currency.THB));
        assertEquals(0, account.getBalance().compareTo(new BigDecimal("100")));
        assertEquals(Currency.USD, account.getCurrency());
    }
}