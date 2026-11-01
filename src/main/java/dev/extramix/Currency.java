package dev.extramix;

import java.math.BigDecimal;

public enum Currency {
    USD("1"),
    JPY("0.0067"),
    THB("0.029"),
    HKD("0.13");

    private final BigDecimal rate;

    Currency(String rate) {
        this.rate = new BigDecimal(rate);
    }

    public BigDecimal rateToUsd() {
        return rate;
    }
}