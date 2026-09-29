package org.example.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.example.util.Constants.BALANCE_THRESHOLD;
import static org.example.util.TaskSimulateWork.simulateCpuWork;

@Slf4j
@RequiredArgsConstructor
public class LowBalanceCheckTask implements Runnable {
    private final double balance;
    private final String accountNumber;

    @Override
    public void run() {
        checkLowBalance();

        if (Thread.currentThread().getName().contains("1")) {
            simulateCpuWork(5000);
        }
    }

    private void checkLowBalance() {
        if (balance < BALANCE_THRESHOLD) {
            String notifyMessage = String.format("Внимание, на счете %s  низкий баланс %s",
                    accountNumber, balance);
            log.warn(notifyMessage);
        } else {
            log.debug("Счет {}, в порядке, баланс {}", accountNumber, balance);
        }
    }
}
