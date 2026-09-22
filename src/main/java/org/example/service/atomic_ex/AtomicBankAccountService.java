package org.example.service.atomic_ex;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.BankAccount;
import org.example.dto.BankAccountState;
import org.example.service.bank_account.BankAccountService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class AtomicBankAccountService {
    private final BankAccountService bankAccountService;
    private final Map<String, AtomicReference<BankAccountState>> accountCache = new ConcurrentHashMap<>();

    public void deposit(String accountNumber, double amount) {
        AtomicReference<BankAccountState> accountRef = getAccountRef(accountNumber);

        while (true) {
            BankAccountState current = accountRef.get();
            BankAccountState updated = new BankAccountState(current.accountNumber(), current.balance() + amount);

            if (accountRef.compareAndSet(current, updated)) {
                log.info("{}  Успешно обновил {}. Новый баланс: {}",
                        Thread.currentThread().getName(), accountNumber, updated.balance());
                return;
            }

            log.warn("{}️ КОНФЛИКТ на {}. Ожидал: {}, но там уже: {}. Повторяю попытку",
                    Thread.currentThread().getName(), accountNumber, current.balance(), accountRef.get().balance());
        }
    }

    public AtomicReference<BankAccountState> getAccountRef(String accountNumber) {
        return accountCache.computeIfAbsent(accountNumber, key -> {
            BankAccount fromDb = bankAccountService.getAccount(key);
            if (fromDb == null) {
                throw new IllegalArgumentException("Аккаунт не найден: " + key);
            }
            return new AtomicReference<>(new BankAccountState(
                    fromDb.getAccountNumber(),
                    fromDb.getBalance()));
        });
    }
}
