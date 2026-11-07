package dev.extramix;


import java.math.BigDecimal;

public record AccountResponse(String id, Currency currency, BigDecimal balance) {

}