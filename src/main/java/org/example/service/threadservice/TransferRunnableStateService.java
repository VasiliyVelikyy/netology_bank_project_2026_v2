package org.example.service.threadservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.util.ErrorHandler;
import org.example.service.bank_account.BankAccountService;
import org.example.task.TransferTask;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferRunnableStateService {
    private final BankAccountService bankAccountService;
    private final ConfigurableApplicationContext context;

    public String startRunnable() {
        Thread.setDefaultUncaughtExceptionHandler((thread, trowable) -> {
            log.error("Неперехваченная ошибка в потоке " + thread.getName() + " : " + trowable.getMessage());
            context.close();
        });

        Thread thread1 = new Thread(
                new TransferTask("ACC001", "ACC002", 1.0,
                                 bankAccountService),
                "Transfer-standard"
        );

        Thread thread2 = new Thread(new TransferTask("ACC003", "ACC004", 500.0,
                                                     bankAccountService),
                                    "Transfer-urgent"
        );

         thread2.setUncaughtExceptionHandler(new ErrorHandler());

        thread1.setPriority(Thread.MAX_PRIORITY);
        thread2.setPriority(Thread.MIN_PRIORITY);

        thread2.start();
        thread1.start();
        return "ok";
    }
}
