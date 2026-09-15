package org.example.data;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.BankAccount;
import org.example.service.bank_account.BankAccountService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.IntStream;

import static org.example.util.Constants.GEN_PREFIX;
import static org.example.util.Constants.TRANSFER_COUNT;
import static org.example.util.TimeUtil.evaluateExecutionTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {
    private final BankAccountService bankAccountService;

    @PostConstruct
    public void init() {
        long startTime = System.nanoTime();

        List<BankAccount> bankAccounts = IntStream.rangeClosed(9, TRANSFER_COUNT)
                                                  .mapToObj(i -> new BankAccount(GEN_PREFIX + i, 100000))
                                                  .toList();
        bankAccountService.saveAll(bankAccounts);

        evaluateExecutionTime(startTime);
    }
}
