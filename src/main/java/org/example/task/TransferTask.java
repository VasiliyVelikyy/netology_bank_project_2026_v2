package org.example.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.BankAccountService;

@Slf4j
@RequiredArgsConstructor
public class TransferTask implements Runnable {
    private final String fromAccNumber;
    private final String toAccNumber;
    private final double amount;
    private final BankAccountService bankAccountService;


    @Override
    public void run() {
        try {
            log.error("Поток " + Thread.currentThread().getName()
                    + " Стартовал. Приоритет: " + Thread.currentThread().getPriority());

            bankAccountService.transfer(fromAccNumber, toAccNumber, amount);
        } catch (RuntimeException e) {
            log.error("Ошибка в потоке " + Thread.currentThread().getName() + "errorMessage " + e.getMessage());
        }

    }
}
