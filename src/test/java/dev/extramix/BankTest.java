package dev.extramix;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class BankTest {

    private void assertBalance(String expected, Account account) {
        BigDecimal expectedAmount = new BigDecimal(expected);
        assertEquals(0, account.getBalance().compareTo(expectedAmount),
                () -> "expected balance " + expectedAmount + " but was " + account.getBalance());
    }

    private record Outcome(int successCount, int rejectCount) {
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

        // A USD account starts with 100.
        // Depositing 1.005 USD is rejected.
        //Its balance remains 100.
        @Test
        void depositFractionAmountOverScaleIsRejectedAndBalanceUnchanged() {
            Bank bank = new Bank();
            Account account = bank.openAccount("a1", Currency.USD);
            assertThrows(IllegalArgumentException.class, () -> bank.deposit(account.getId(), new BigDecimal("1.005"), Currency.USD));
            assertBalance("0", account);
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

    @Nested
    class Transfer {
//        Bank.transfer(fromId, toId, amount)
//        same currency only: reject if different
//        exact amount leaves and go in target account
//        unknown accounts transfer should reject and the balance shouldn't move
//        one pass-through check with non-negative amount
//        self transfer is rejected

        @Test
        void transferToAnotherAccountWithSameCurrency() {
            Bank bank = new Bank();
            Account a1 = bank.openAccount("a1", Currency.USD);
            Account a2 = bank.openAccount("a2", Currency.USD);
            bank.deposit(a1.getId(), new BigDecimal("100"), Currency.USD);

            bank.transfer("a1", "a2", new BigDecimal("100"));
            assertBalance("100", a2);
        }

        @Test
        void transferDifferentCurrency() {
            Bank bank = new Bank();
            Account a1 = bank.openAccount("a1", Currency.USD);
            Account a2 = bank.openAccount("a2", Currency.THB);
            bank.deposit(a1.getId(), new BigDecimal("100"), Currency.USD);

            // use source currency for the amount
            bank.transfer("a1", "a2", new BigDecimal("10")); // 10 / 0.029 = 344.83
            assertBalance("90", a1);
            assertBalance("344.83", a2);
        }

        @Test
        void transferFractionAmountThatConvertsToZeroIsRejectedAndBalancesUnchanged() {
            Bank bank = new Bank();
            Account a1 = bank.openAccount("a1", Currency.THB);
            Account a2 = bank.openAccount("a2", Currency.USD);
            bank.deposit(a1.getId(), new BigDecimal("100"), Currency.THB);

            assertThrows(IllegalArgumentException.class, () -> bank.transfer("a1", "a2", new BigDecimal("0.01")));
            assertBalance("100", a1);
            assertBalance("0", a2);
        }

        @Test
        void transferToUnknownAccountIsRejectedAndBalancesAreUnchanged() {
            Bank bank = new Bank();
            Account a1 = bank.openAccount("a1", Currency.USD);
            bank.deposit(a1.getId(), new BigDecimal("100"), Currency.USD);

            assertThrows(IllegalArgumentException.class, () -> bank.transfer("a1", "a99", new BigDecimal("100")));
            assertBalance("100", a1);
        }

        @Test
        void transferFromUnknownAccountIsRejectedAndBalancesAreUnchanged() {
            Bank bank = new Bank();
            Account a2 = bank.openAccount("a2", Currency.USD);
            bank.deposit(a2.getId(), new BigDecimal("100"), Currency.USD);

            assertThrows(IllegalArgumentException.class, () -> bank.transfer("a99", "a2", new BigDecimal("100")));
            assertBalance("100", a2);
        }

        @Test
        void nonPositiveAmountIsRejected() {
            Bank bank = new Bank();
            Account a1 = bank.openAccount("a1", Currency.USD);
            Account a2 = bank.openAccount("a2", Currency.USD);
            bank.deposit(a1.getId(), new BigDecimal("100"), Currency.USD);

            assertThrows(IllegalArgumentException.class, () -> bank.transfer("a1", "a2", new BigDecimal("-1")));
            assertBalance("100", a1);
        }

        @Test
        void selfTransferIsRejected() {
            Bank bank = new Bank();
            Account a1 = bank.openAccount("a1", Currency.USD);
            bank.deposit(a1.getId(), new BigDecimal("100"), Currency.USD);

            assertThrows(IllegalArgumentException.class, () -> bank.transfer("a1", "a1", new BigDecimal("70")));
            assertBalance("100", a1);
        }

        @Test
        void insufficientFundTransferIsRejected() {
            Bank bank = new Bank();
            Account a1 = bank.openAccount("a1", Currency.USD);
            Account a2 = bank.openAccount("a2", Currency.USD);
            bank.deposit(a1.getId(), new BigDecimal("100"), Currency.USD);

            assertThrows(IllegalStateException.class, () -> bank.transfer("a1", "a2", new BigDecimal("101")));
            assertBalance("100", a1);
            assertBalance("0", a2);
        }

        Outcome runConcurrently(List<Runnable> operations, Consumer<Throwable> assertExpectedFailures)
                throws InterruptedException, TimeoutException {

            try (ExecutorService executor = Executors.newFixedThreadPool(operations.size())) {
                CountDownLatch ready = new CountDownLatch(operations.size());
                CountDownLatch start = new CountDownLatch(1);
                List<Future<?>> futures = new ArrayList<>();
                for (Runnable operation : operations) {
                    futures.add(executor.submit(() -> {
                        ready.countDown();
                        try {

                            start.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException(e);
                        }
                        operation.run();
                    }));
                }

                try {
                    assertTrue(ready.await(5, TimeUnit.SECONDS));
                } finally {
                    start.countDown();
                }
                int successCount = 0;
                int rejectCount = 0;
                for (Future<?> future : futures) {
                    try {
                        future.get(5, TimeUnit.SECONDS);
                        successCount++;
                    } catch (ExecutionException e) {
                        Throwable cause = e.getCause();
                        assertExpectedFailures.accept(cause);
                        rejectCount++;
                    }
                }

                return new Outcome(successCount, rejectCount);
            }
        }

        @Test
        void shouldOnlyAllowOneTransferForConcurrentRequests() throws InterruptedException, ExecutionException, TimeoutException {
            Bank bank = new Bank();

            Account a1 = bank.openAccount("a1", Currency.USD);
            Account a2 = bank.openAccount("a2", Currency.USD);
            Account a3 = bank.openAccount("a3", Currency.USD);

            bank.deposit(a1.getId(), new BigDecimal("100"), Currency.USD);
            List<Runnable> operations = List.of(
                    () -> bank.transfer("a1", "a2", new BigDecimal("80")),
                    () -> bank.transfer("a1", "a3", new BigDecimal("80")));

            Outcome outcome = runConcurrently(operations, cause -> assertInstanceOf(IllegalStateException.class, cause));

            assertEquals(1, outcome.successCount);
            assertEquals(1, outcome.rejectCount);
            assertBalance("20", a1);
            BigDecimal received = a2.getBalance().add(a3.getBalance());
            assertEquals(0, received.compareTo(new BigDecimal("80")));

        }
    }

}


