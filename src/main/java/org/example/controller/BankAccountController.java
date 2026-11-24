package org.example.controller;


import lombok.RequiredArgsConstructor;
import org.example.dto.BankAccountState;
import org.example.service.bank_account.BankAccountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class BankAccountController {
    private final BankAccountService bankAccountService;

    @GetMapping("/clients-balance")
    public List<BankAccountState> getClientBalance() {
        return bankAccountService.findAll()
                .stream()
                .map(acc -> new BankAccountState(
                        acc.getAccountNumber(),
                        acc.getBalance()))
                .toList();
    }
}
