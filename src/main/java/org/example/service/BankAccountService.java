package org.example.service;


import org.example.domain.BankAccount;
import org.example.repo.BankAccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service

public class BankAccountService {
    private final BankAccountRepository bankAccountRepository;

    public BankAccountService(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }

    public List<BankAccount> getAll() {
        return bankAccountRepository.findAll();
    }

    public BankAccount getByAccountNumber(String accountNumber) {
        var accountOptional = bankAccountRepository.findById(accountNumber);
        return accountOptional.orElseThrow(() -> new RuntimeException("Счет не найден" + accountNumber));
    }

    public BankAccount saveAccount(BankAccount account) {
        return bankAccountRepository.save(account);
    }

    public void transfer(String accountFrom, String accountTo, double amount) {
        BankAccount from =getByAccountNumber(accountFrom);
        BankAccount to =getByAccountNumber(accountTo);

        if(from.getBalance()<amount){
            throw new RuntimeException("Недостаточно средств "+accountFrom);
        }

        from.setBalance(from.getBalance()-amount);
        to.setBalance(to.getBalance()+amount);

        bankAccountRepository.save(from);
        bankAccountRepository.save(to);
    }

}
