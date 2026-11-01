package dev.extramix;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BankTest {

    private void assertBalance(String expected, Account account) {
        BigDecimal expectedAmount = new BigDecimal(expected);
        assertEquals(0, account.getBalance().compareTo(expectedAmount),
                () -> "expected balance " + expectedAmount + " but was " + account.getBalance());
    }

    @Nested
    class OpenAccount {

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
    }

    @Nested
    class Deposit {

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
        void depositGoesToTheNamedAccountOnly() {
            Bank bank = new Bank();
            Account account1 = bank.openAccount("a1", Currency.USD);
            Account account2 = bank.openAccount("a2", Currency.USD);

            bank.deposit("a2", new BigDecimal("100"), Currency.USD);

            assertEquals(0, account1.getBalance().compareTo(BigDecimal.ZERO));
            assertEquals(0, account2.getBalance().compareTo(new BigDecimal("100")));
        }
    }

    @Nested
    class DepositWithConversion {

        @Test
        void depositWithDifferentCurrencyConvertsToUSDBeforeIncreasingBalance() {
            Bank bank = new Bank();
            Account account = bank.openAccount("a1", Currency.USD);
            bank.deposit(account.getId(), new BigDecimal("10000"), Currency.JPY); // 10,000 * 0.0067 = 67 USD

            assertBalance("67", account);
        }

        @Test
        void depositTHBIntoJPYAccountConvertsThroughUSD() {
            Bank bank = new Bank();
            Account account = bank.openAccount("a1", Currency.JPY);
            bank.deposit(account.getId(), new BigDecimal("100"), Currency.THB); // 100 * 0.029 / 0.0067 = 432.8358208955 -> 432.84

            assertBalance("432.84", account);
        }

        @Test
        void depositNegativeAmountWithForeignCurrencyIsRejected() {
            Bank bank = new Bank();
            Account account = bank.openAccount("a1", Currency.USD);

            assertThrows(IllegalArgumentException.class, () -> bank.deposit(account.getId(), new BigDecimal("-100"), Currency.THB));
        }

        @Test
        void depositWithForeignCurrencyToUnknownAccountIsRejected() {
            Bank bank = new Bank();
            Account account = bank.openAccount("a1", Currency.USD);

            assertThrows(IllegalArgumentException.class, () -> bank.deposit("a2", new BigDecimal("100"), Currency.THB));
        }

        @Test
        void depositFractionAmountConvertsToZeroAndIsRejected() {
            Bank bank = new Bank();
            Account account = bank.openAccount("a1", Currency.USD);

            assertThrows(IllegalArgumentException.class, () -> bank.deposit(account.getId(), new BigDecimal("0.01"), Currency.THB));
            assertBalance("0", account);
        }


        @ParameterizedTest
        @CsvSource({"JPY, 10000, 67.00", "THB, 10000, 290", "HKD, 777, 101.01", "USD, 10000, 10000", "THB, 0.25, 0.01"})
        void convertsToUsd(Currency source, String amount, String expected) {
            Bank bank = new Bank();
            Account account = bank.openAccount("a1", Currency.USD);
            bank.deposit(account.getId(), new BigDecimal(amount), source);

            assertBalance(expected, account);
        }

        @Test
        void convertsUSDToJPY() {
            Bank bank = new Bank();
            Account account = bank.openAccount("a1", Currency.JPY);
            bank.deposit(account.getId(), new BigDecimal("100"), Currency.USD);

            assertBalance("14925.37", account);
        }
    }

    @Nested
    class Withdraw {

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
}
