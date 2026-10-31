package dev.extramix;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {


    @Test
    void EmptyParamsAccountIsRejected() {
        assertThrows(NullPointerException.class, () -> new Account(null, Currency.USD));
        assertThrows(NullPointerException.class, () -> new Account("a1", null));
    }

    @Test
    void newAccountStartsAtZero() {
        Account account = new Account("a1", Currency.USD);

        assertEquals(0, account.getBalance().compareTo(BigDecimal.ZERO));
    }


    // Credit

    @Test
    void creditIncreasesBalance(){
        Account account = new Account("a1", Currency.USD);
        account.credit(new BigDecimal("100"));
        account.credit(new BigDecimal("150"));


        assertEquals(0, account.getBalance().compareTo(new BigDecimal("250")));
    }

    @Test
    void creditZeroIsRejected(){
        Account account = new Account("a1", Currency.USD);
        account.credit(new BigDecimal("100"));

        assertThrows(IllegalArgumentException.class, () -> account.credit(new BigDecimal("0")));
        assertEquals(0, account.getBalance().compareTo(new BigDecimal("100")));
    }

    @Test
    void creditNegativeValuesIsRejected(){
        Account account = new Account("a1", Currency.USD);
        account.credit(new BigDecimal("100"));

        assertThrows(IllegalArgumentException.class, () -> account.credit(new BigDecimal("-1")));
        assertEquals(0, account.getBalance().compareTo(new BigDecimal("100")));
    }

    @Test
    void creditNullIsRejectedAndBalanceUnchanged() {
        Account account = new Account("a1", Currency.USD);
        account.credit(new BigDecimal("100"));

        assertThrows(NullPointerException.class, () -> account.credit(null));
        assertEquals(0, account.getBalance().compareTo(new BigDecimal("100")));
    }


    // Debit

    @Test
    void debitReducesBalance(){
        Account account = new Account("a1", Currency.USD);
        account.credit(new BigDecimal("100"));

        account.debit(new BigDecimal("30"));

        assertEquals(0, account.getBalance().compareTo(new BigDecimal("70")));

    }

    @Test
    void debitExactBalanceLeavesZero() {
        Account account = new Account("a1", Currency.USD);
        account.credit(new BigDecimal("100"));

        account.debit(new BigDecimal("100"));

        assertEquals(0, account.getBalance().compareTo(new BigDecimal("0")));
    }

    @Test
    void debitMoreThanBalanceAndChangesNothing(){
        Account account = new Account("a1", Currency.USD);
        account.credit(new BigDecimal("100"));

        assertThrows(IllegalStateException.class, () -> account.debit(new BigDecimal("101")));

        assertEquals(0, account.getBalance().compareTo(new BigDecimal("100")));

    }

    @Test
    void debitLessThanZeroIsRejected() {
        Account account = new Account("a1", Currency.USD);
        account.credit(new BigDecimal("100"));

        assertThrows(IllegalArgumentException.class, () -> account.debit(new BigDecimal("-1")));

        assertEquals(0, account.getBalance().compareTo(new BigDecimal("100")));
    }

    @Test
    void debitZeroAmountIsRejected(){
        Account account = new Account("a1", Currency.USD);
        account.credit(new BigDecimal("100"));

        assertThrows(IllegalArgumentException.class, () -> account.debit(new BigDecimal("0")));

        assertEquals(0, account.getBalance().compareTo(new BigDecimal("100")));
    }

    @Test
    void debitWithDecimal(){
        Account account = new Account("a1", Currency.USD);
        account.credit(new BigDecimal("100"));

        account.debit(new BigDecimal("50.50"));

        assertEquals(0, account.getBalance().compareTo(new BigDecimal("49.50")));
    }

    @Test
    void debitNullIsRejectedAndBalanceUnchanged() {
        Account account = new Account("a1", Currency.USD);
        account.credit(new BigDecimal("100"));

        assertThrows(NullPointerException.class, () -> account.debit(null));
        assertEquals(0, account.getBalance().compareTo(new BigDecimal("100")));
    }
}