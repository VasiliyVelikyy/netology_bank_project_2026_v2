package org.example.task;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.example.service.BankAccountService;
import org.springframework.stereotype.Component;

import static org.example.util.LoggingUtils.loggingThreadError;

@Component
@Slf4j
public class LoggerTask implements Runnable {
    private final BankAccountService bankAccountService;
    private Thread thread;

    private boolean running = true;

    public LoggerTask(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @PostConstruct
    public void start() {
        thread = new Thread(this, "LoggerTask-Daemon");
        thread.setDaemon(true);
        thread.start();
    }

    @PreDestroy
    public void stop() {
        running = false;
        if (thread != null && !thread.isInterrupted()) {
            thread.interrupt();
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                long count = bankAccountService.count();
                log.info("[ДЕМОН] Количество счетов: {}", count);
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                log.error("Демон поток остановлен");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                loggingThreadError(e);
            }
        }

    }
}
