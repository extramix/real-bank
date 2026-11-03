package dev.extramix;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountTest {

    @Nested
    class Construction {

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

        @Test
        void BlankIdIsRejected() {
            assertThrows(IllegalArgumentException.class, () -> new Account("", Currency.USD));
            assertThrows(IllegalArgumentException.class, () -> new Account(" ", Currency.USD));

        }
    }

    @Nested
    class Credit {

        @Test
        void creditIncreasesBalance() {
            Account account = new Account("a1", Currency.USD);
            account.credit(new BigDecimal("100"));
            account.credit(new BigDecimal("150"));


            assertEquals(0, account.getBalance().compareTo(new BigDecimal("250")));
        }

        @Test
        void creditZeroIsRejected() {
            Account account = new Account("a1", Currency.USD);
            account.credit(new BigDecimal("100"));

            assertThrows(IllegalArgumentException.class, () -> account.credit(new BigDecimal("0")));
            assertEquals(0, account.getBalance().compareTo(new BigDecimal("100")));
        }

        @Test
        void creditNegativeValuesIsRejected() {
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

        @Test
        void creditRejectsFractionalCentsAndPreservesBalance() {
            Account account = new Account("a1", Currency.USD);

            assertThrows(IllegalArgumentException.class, () -> account.credit(new BigDecimal("0.001")));
            assertEquals(0, account.getBalance().compareTo(new BigDecimal("0")));
        }

        @ParameterizedTest
        @CsvSource({"100.1, 100.1", "100.11, 100.11", "100.120, 100.12"})
        void creditAcceptsWholeCentAmounts(String amount, String expected) {
            Account account = new Account("a1", Currency.USD);
            account.credit(new BigDecimal(amount));
            assertEquals(0, account.getBalance().compareTo(new BigDecimal(expected)));
        }
    }

    @Nested
    class Debit {

        @Test
        void debitReducesBalance() {
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
        void debitMoreThanBalanceAndChangesNothing() {
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
        void debitZeroAmountIsRejected() {
            Account account = new Account("a1", Currency.USD);
            account.credit(new BigDecimal("100"));

            assertThrows(IllegalArgumentException.class, () -> account.debit(new BigDecimal("0")));

            assertEquals(0, account.getBalance().compareTo(new BigDecimal("100")));
        }

        @Test
        void debitWithDecimal() {
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

        @Test
        void debitRejectsFractionalCentsAndPreservesBalance() {
            Account account = new Account("a1", Currency.USD);
            account.credit(new BigDecimal("100"));


            assertThrows(IllegalArgumentException.class, () -> account.debit(new BigDecimal("0.001")));
            assertEquals(0, account.getBalance().compareTo(new BigDecimal("100")));
        }

        @ParameterizedTest
        @CsvSource({"0.1, 99.9", "0.11, 99.89", "0.120, 99.88"})
        void debitAcceptsWholeCentAmounts(String amount, String expected) {
            Account account = new Account("a1", Currency.USD);
            account.credit(new BigDecimal("100"));
            account.debit(new BigDecimal(amount));

            assertEquals(0, account.getBalance().compareTo(new BigDecimal(expected)));
        }
    }
}
