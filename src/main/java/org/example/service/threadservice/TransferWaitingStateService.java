package org.example.service.threadservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.BankAccountService;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferWaitingStateService {
    private final BankAccountService bankAccountService;

    public String startWaiting() {
        Object monitor=new Object();

        Thread waiter = new Thread(() -> {
            log.info("поток " + Thread.currentThread().getName() + " стартовал");
            bankAccountService.transferWithWait("ACC005", "ACC006", 20.0, monitor, true);
        }, "Trasnfer-waiter");


        Thread notifier = new Thread(() -> {
            log.info("поток " + Thread.currentThread().getName() + " стартовал");
            // Thread.sleep(5000);
            bankAccountService.transferWithWait("ACC006", "ACC007", 20.0, monitor, false);
        }, "Trasnfer-notifier");

        waiter.start();
        notifier.start();
        return "ok";
    }

    public String startPark() {
        Lock lock = new ReentrantLock();

        Thread parkHolder = new Thread(() -> {
            try {
                log.info("[" + Thread.currentThread().getName() + "] Стартовал.");
                bankAccountService.transferWithPark("ACC005", "ACC006", lock, 20.0, false);
            } catch (Exception e) {
                System.err.println("Ошибка в parkHolder: " + e.getMessage());
            }
        }, "Transfer-Park-Holder");


        Thread parkWaiter = new Thread(() -> {
            try {
                log.info("[" + Thread.currentThread().getName() + "] Стартовал.");
                //Thread.sleep(5000);
                // Запускаем сразу — чтобы parkWaiter попал в ожидание (park)
                bankAccountService.transferWithPark("ACC006", "ACC007", lock, 30.0, false);
            } catch (Exception e) {
                System.err.println("Ошибка в parkWaiter: " + e.getMessage());
            }
        }, "Transfer-Park-Waiter");

        parkHolder.start();
        parkWaiter.start();
        return "ok";
    }
}
