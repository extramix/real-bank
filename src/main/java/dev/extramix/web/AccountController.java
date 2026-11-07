package dev.extramix.web;

import dev.extramix.bank.Account;
import dev.extramix.bank.Bank;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController {
    private final Bank bank;

    public AccountController(Bank bank) {
        this.bank = bank;
    }

    @PostMapping("/accounts")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody OpenAccountRequest openAccountRequest) {
        Account account = bank.openAccount(openAccountRequest.id(),
                openAccountRequest.currency());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new AccountResponse(account.getId(),
                        account.getCurrency(),
                        account.getBalance()));
    }

}
