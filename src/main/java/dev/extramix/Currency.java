package dev.extramix;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum Currency {
    USD("1"),
    JPY("0.0067"),
    THB("0.029"),
    HKD("0.13");

    final static int SCALE = 2;
    final static RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private final BigDecimal rate;

    Currency(String rate) {
        this.rate = new BigDecimal(rate);
    }

    public static BigDecimal round(BigDecimal amount) {
        return amount.setScale(SCALE, ROUNDING_MODE);
    }

    public BigDecimal rateToUsd() {
        return rate;
    }

    public BigDecimal toUSD(BigDecimal amount) {
        return amount.multiply(this.rateToUsd());
    }

    public BigDecimal fromUSD(BigDecimal amount) {
        return amount.divide(this.rateToUsd(), SCALE, ROUNDING_MODE);
    }
}