package org.example.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.BankAccount;
import org.example.repo.BankAccountRepository;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountCacheService {
    private final BankAccountRepository bankAccountRepository;

    private final Map<String, BankAccount> concurrentCache = new ConcurrentHashMap<>();

    private final Map<String, BankAccount> synchronizedCache = Collections.synchronizedMap(new HashMap<>());

    public BankAccount getConcurrentNaive(String accountNumber) {
        return concurrentCache.computeIfAbsent(accountNumber, bankAccountRepository::findByAccountNumber);
    }

    public BankAccount getConcurrentOptimize(String accountNumber) {
        BankAccount acc = concurrentCache.get(accountNumber);
        if (acc != null) {
            return acc;
        }
        acc = bankAccountRepository.findByAccountNumber(accountNumber);

        return concurrentCache.putIfAbsent(accountNumber, acc);
    }

    public BankAccount getSynchronized(String accountNumber) {
        synchronized (synchronizedCache) {
            BankAccount acc = synchronizedCache.get(accountNumber);
            if (acc == null) {
                acc = bankAccountRepository.findByAccountNumber(accountNumber);
                synchronizedCache.put(accountNumber, bankAccountRepository.findByAccountNumber(accountNumber));
            }
            return acc;
        }
    }

    public void clear() {
        concurrentCache.clear();
        synchronizedCache.clear();
    }

    public void evict(String accountNumber) {
        concurrentCache.remove(accountNumber);
        synchronizedCache.remove(accountNumber);
    }

    public int concurrentCacheSize() {
        return concurrentCache.size();
    }

    public int syncCacheSize() {
        return synchronizedCache.size();
    }
}
