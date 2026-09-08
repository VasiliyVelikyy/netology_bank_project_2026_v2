package org.example.task;

import lombok.extern.slf4j.Slf4j;
import org.example.service.BankAccountService;

@Slf4j
public class TransferTask implements Runnable {
    private final String fromAccountNumber;
    private final String toAccountNumber;
    private final double amount;
    private final BankAccountService bankAccountService;

    public TransferTask(String fromAccountNumber,
                        String toAccountNumber,
                        double amount,
                        BankAccountService bankAccountService) {
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountNumber = toAccountNumber;
        this.amount = amount;
        this.bankAccountService = bankAccountService;
    }

    @Override
    public void run() {
       // try {
            log.info("Поток " + Thread.currentThread().getName()
                    + " Стартовал. Приоритет: " + Thread.currentThread().getPriority());

            bankAccountService.transfer(fromAccountNumber, toAccountNumber, amount);
//        } catch (RuntimeException e) {
//            log.error("Ошибка в потоке " + Thread.currentThread().getName() + " errorMessage " + e.getMessage());
//        }

    }
}
