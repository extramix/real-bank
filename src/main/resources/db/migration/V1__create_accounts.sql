CREATE TABLE accounts (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          account_number TEXT NOT NULL UNIQUE,
                          currency VARCHAR(3) NOT NULL,
                          balance NUMERIC NOT NULL DEFAULT 0,
                          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT account_number_not_blank
                              CHECK (account_number ~ '[^[:space:]]'),
    CONSTRAINT account_currency_supported
        CHECK (currency IN ('USD', 'JPY', 'THB', 'HKD')),
    CONSTRAINT account_balance_valid
        CHECK (
            balance >= 0
            AND balance < 'Infinity'::NUMERIC
            AND balance = round(balance, 2)
        )
);