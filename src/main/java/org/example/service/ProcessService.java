package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.task.LoggerTask;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessService {
    private final BankAccountService bankAccountService;
    private final ConfigurableApplicationContext context;

    public String runThread() {
//        var context = SpringApplication.run(Main.class, args);
//        bankAccountService = context.getBean(BankAccountService.class);
//
//        Thread.setDefaultUncaughtExceptionHandler((thread, trowable) -> {
//            log.error("Неперехваченная ошибка в потоке " + thread.getName() + " : " + trowable.getMessage());
//            context.close();
//        });
//
//        Thread thread1 = new Thread(
//                new TransferTask("ACC001", "ACC002", 1.0,
//                        bankAccountService),
//                "Transfer-standard"
//        );
//
//        Thread thread2 = new Thread(new TransferTask("ACC003", "ACC004", 500.0,
//                bankAccountService),
//                "Transfer-urgent"
//        );
//
//        // thread2.setUncaughtExceptionHandler(new ErrorHandler());
//
//        thread1.setPriority(Thread.MAX_PRIORITY);
//        thread2.setPriority(Thread.MIN_PRIORITY);
//
//        thread2.start();
//        thread1.start();
//
        var loggedTask = new LoggerTask(bankAccountService);
        Thread loggedthread = new Thread(loggedTask);
        loggedthread.setDaemon(true);
        loggedthread.start();


        Thread threadSleep = new Thread(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                log.error("error " + e.getMessage());
            }
        },
                "sleepthread");
        //threadSleep.start();


        Object monitor = new Object();
        Thread blocker = new Thread(() -> {
            log.info("поток " + Thread.currentThread().getName() + " стартовал");
            bankAccountService.transferWithBlock("ACC001", "ACC002", 20.0, monitor);
        }, "Trasnfer-blocker");


        Thread blockee = new Thread(() -> {
            log.info("поток " + Thread.currentThread().getName() + " стартовал");
            bankAccountService.transferWithBlock("ACC002", "ACC003", 20.0, monitor);
        }, "Transfer-blockee");

//        blocker.start();
//        blockee.start();


        Thread waiter = new Thread(() -> {
            log.info("поток " + Thread.currentThread().getName() + " стартовал");
            bankAccountService.transferWithWait("ACC005", "ACC006", 20.0, monitor, true);
        }, "Trasnfer-waiter");


        Thread notifier = new Thread(() -> {
            log.info("поток " + Thread.currentThread().getName() + " стартовал");
           // Thread.sleep(5000);
            bankAccountService.transferWithWait("ACC006", "ACC007", 20.0, monitor, false);
        }, "Trasnfer-notifier");

//        waiter.start();
//        notifier.start();


        Lock lock = new ReentrantLock();

        Thread parkHolder = new Thread(() -> {
            try {
                log.info("[" + Thread.currentThread().getName() + "] Стартовал.");
                bankAccountService.transferWithPark("ACC005", "ACC006", lock, 20.0, true);
            } catch (Exception e) {
                System.err.println("Ошибка в parkHolder: " + e.getMessage());
            }
        }, "Transfer-Park-Holder");


        Thread parkWaiter = new Thread(() -> {
            try {
                log.info("[" + Thread.currentThread().getName() + "] Стартовал.");
                Thread.sleep(5000);
                // Запускаем сразу — чтобы parkWaiter попал в ожидание (park)
                bankAccountService.transferWithPark("ACC006", "ACC007", lock, 30.0, true);
            } catch (Exception e) {
                System.err.println("Ошибка в parkWaiter: " + e.getMessage());
            }
        }, "Transfer-Park-Waiter");

          parkHolder.start();
         parkWaiter.start();


        return "ok";
    }
}
