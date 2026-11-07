# Real Bank — Stage 1 specification

Build a small banking domain to practice Java, test design, and debugging on code you write yourself. Use Java 21, Maven, and JUnit 5 (`junit-jupiter`). Store accounts in memory.

Work in roughly 45-minute sessions. A feature can span sessions; commit its tests with it. When debugging, read the failure, state one hypothesis, set a breakpoint where the behavior diverges, and predict what will happen before stepping.

## Accounts and money

- A bank holds accounts identified by unique, nonblank IDs. Each account has one currency and starts with a zero balance.
- Use `BigDecimal` built from strings. Compare amounts numerically with `compareTo` when scale is irrelevant.
- An amount posted to an account must be positive and exactly representable with two decimal places. `10.120` is valid because it equals `10.12`; `10.123` is invalid. Balances do not have to be stored with exactly two digits after the decimal point.
- Use fixed exercise rates. Convert through USD, retaining full precision on the intermediate leg. Round only the final converted amount, to two decimal places with `HALF_UP`. Reject conversions that round to zero.

| Currency | USD per unit |
|---|---:|
| USD | 1 |
| JPY | 0.0067 |
| THB | 0.029 |
| HKD | 0.13 |

The original outline also lists IDR and SGD. Their exercise rates are still to be decided.

## S0–S3: Open, deposit, withdraw, and convert

- **S0 — Setup:** Confirm Maven discovers and executes tests.
- **S1 — Open and deposit:** Reject invalid IDs, missing currency, and duplicate IDs. A same-currency deposit credits the exact valid amount. Reject zero, negative amounts, and amounts that would require rounding. A rejection leaves the balance unchanged.
- **S2 — Withdraw:** Require the withdrawal currency to match the account. Debit the exact valid amount. Reject insufficient funds with `IllegalStateException` and preserve the balance. Withdrawing the exact balance leaves zero.
- **S3 — Foreign deposits:** Resolve the destination account, validate that the incoming amount is positive, convert, and credit the rounded result. A foreign deposit can accept finer source precision because it does not debit a source account. Reject a conversion to zero. `Account` receives an amount in its own currency; `Bank` and `Currency` handle conversion.

Test exact decimal addition, rejected operations with unchanged balances, conversions into USD for each supported currency, and a rounding-sensitive conversion out of USD. For example, `0.25 THB` lands as `0.01 USD`, and `100 USD` lands as `14,925.37 JPY` using these rates.

## S4: Same-currency transfers

Use `Bank.transfer(fromId, toId, amount)`. The accounts supply the currencies. Resolve both accounts before changing either balance. Reject unknown IDs, self-transfers, and, until S5a, currency mismatches with `IllegalArgumentException`.

Debit and credit the same valid amount. Expected validation failures must leave both balances unchanged. The insufficient-funds check must happen before the debit updates the balance.

- A full-balance transfer leaves the sender at zero and credits the recipient by the exact amount. Assert both balances.
- A sender with `100` cannot transfer `101`. Expect `IllegalStateException` and assert both balances are unchanged.
- Cover unknown source and destination, self-transfer, mismatched currency, and a representative non-positive amount. For an unknown destination, verify that the sender's balance remains unchanged.

### S4b: Optional concurrency exercise

Two transfers of `80` competing for a sender balance of `100` must produce one success and one insufficient-funds rejection. Assert that the sender has `20` and the recipients collectively have `80`.

The simple version serializes transfers on the same bank. Keep the funds check and both balance updates within the protected operation. This guarantee covers competing transfers; concurrent deposits, withdrawals, direct account access, and changes to the account map need their own shared locking rules.

## S5a: Cross-currency transfers

Keep the same transfer signature. **The amount is denominated in the sender's currency.** Debit that exact amount and credit its rounded equivalent in the recipient's currency.

- Validate the amount against the sender's precision rule. Calculate and validate the recipient's amount before debiting the sender.
- Use the existing conversion policy. A same-currency transfer still moves the exact amount with no conversion.
- If the result rounds to zero, reject the transfer before either balance changes.
- Preserve the existing rejection behavior for unknown accounts, self-transfers, invalid amounts, and insufficient funds.

| First test | Sender (USD) | Recipient (THB) |
|---|---:|---:|
| Starting balance | 100.00 | 0.00 |
| Transfer 10.00 USD | −10.00 | +344.83 |
| Expected balance | 90.00 | 344.83 |

The recipient amount is `10 ÷ 0.029`, rounded to `344.83 THB`. Replace the S4 currency-mismatch rejection test with the successful conversion test. Then test a transfer of `0.01 THB` to USD: the destination amount rounds to zero, so both balances must remain unchanged.

## S5b: Transaction history

Each successful balance change appends one entry to the affected account's history, recording its type, amount, currency, and resulting balance. A successful transfer adds one entry to each account. A failed operation adds nothing. A statement returns entries in order without exposing the bank's mutable history list.

## S6: Finish Stage 1

- Group related tests with JUnit `@Nested`; use parameterized tests for the same behavior across different inputs. Keep setup specific to a test visible in that test.
- Run `mvn clean test` and confirm the expected tests were executed.
- Explain the choice of `BigDecimal`, the rounding policy, and the transfer guarantees and their limits in the README.
- Be able to reproduce and investigate a test failure in the debugger.

Stage 2 can build on this domain and its behavioral tests with Spring Boot, an API, and persistent storage.
