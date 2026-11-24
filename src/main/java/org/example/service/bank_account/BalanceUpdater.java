package org.example.service.bank_account;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.BankAccount;
import org.example.dto.BankAccountState;
import org.example.service.websocket.LiveBalanceWebSocketHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class BalanceUpdater {
    private final BankAccountService bankAccountService;
    private final LiveBalanceWebSocketHandler liveBalanceWebSocketHandler;

    private final List<String> accountsId = Arrays.asList("ACC002", "ACC003", "ACC004", "ACC005", "ACC006", "ACC007", "ACC008");
    private final Random random = new Random();

    @Scheduled(fixedDelay = 3000)
    public void balanceChange() {

        var accId = accountsId.get(random.nextInt(accountsId.size()));

        double rawAmount = 1 + random.nextDouble() + 9; //1 -10
        double newBalance = Math.round(rawAmount * 100.0) / 100.0;

        Optional<BankAccount> accountOpt = bankAccountService.getAccountOpt(accId);

        if (accountOpt.isPresent()) {
            BankAccount bankAccount = accountOpt.get();
            updateBalance(bankAccount, newBalance);

            //todo send List< >
            liveBalanceWebSocketHandler.sendUpdate(new BankAccountState(bankAccount.getAccountNumber(), bankAccount.getBalance()));
        } else {
            log.info("Account {} not found, skip update", accId);
        }
    }

    private void updateBalance(BankAccount bankAccount, double newBalance) {
        synchronized (bankAccount.getAccountNumber()) {
            bankAccount.setBalance(newBalance);
            bankAccountService.save(bankAccount);
        }
    }
}
