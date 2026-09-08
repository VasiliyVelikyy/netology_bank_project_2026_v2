package org.example.task;

import lombok.extern.slf4j.Slf4j;
import org.example.service.BankAccountService;

@Slf4j
public class LoggerTask implements Runnable {
    private final BankAccountService bankAccountService;

    private boolean running = true;

    public LoggerTask(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @Override
    public void run() {
        while (running) {
            try {
                long count = bankAccountService.count();
                log.info("[Демон] , Количество счетов = " + count);
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.error("Демон поток остановлен");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("[Демон] ошибка " + e.getMessage());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

    }
}
