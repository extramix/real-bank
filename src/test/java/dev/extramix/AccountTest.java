package dev.extramix;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void newAccountStartsAtZero() {
        //Arrange
        Account account = new Account("a1", Currency.USD);

        //Act

        //Asset
        assertEquals(0, account.getBalance().compareTo(BigDecimal.ZERO));
    }
}