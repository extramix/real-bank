package dev.extramix.web;


import dev.extramix.bank.Currency;

import java.math.BigDecimal;

public record AccountResponse(String id, Currency currency, BigDecimal balance) {

}