package org.example.service.threadservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.bank_account.BankAccountProfilingExampleService;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.example.util.LoggingUtils.loggingStartThread;
import static org.example.util.LoggingUtils.loggingThreadError;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferWaitingStateService {
    private final BankAccountProfilingExampleService profilingExampleService;

    public String startWaiting() {
        Object monitor=new Object();

        Thread waiter = new Thread(() -> {
            loggingStartThread();
            profilingExampleService.transferWithWait("ACC005", "ACC006", 20.0, monitor, true);
        }, "Trasnfer-waiter");


        Thread notifier = new Thread(() -> {
            loggingStartThread();
            // Thread.sleep(5000);
            profilingExampleService.transferWithWait("ACC006", "ACC007", 20.0, monitor, false);
        }, "Trasnfer-notifier");

        waiter.start();
        notifier.start();
        return "ok";
    }

    public String startPark() {
        Lock lock = new ReentrantLock();

        Thread parkHolder = new Thread(() -> {
            try {
                loggingStartThread();
                profilingExampleService.transferWithPark("ACC005", "ACC006", lock, 20.0);
            } catch (Exception e) {
                loggingThreadError(e);
            }
        }, "Transfer-Park-Holder");


        Thread parkWaiter = new Thread(() -> {
            try {
                loggingStartThread();
                //Thread.sleep(5000);
                // Запускаем сразу — чтобы parkWaiter попал в ожидание (park)
                profilingExampleService.transferWithPark("ACC006", "ACC007", lock, 30.0);
            } catch (Exception e) {
                loggingThreadError(e);
            }
        }, "Transfer-Park-Waiter");

        parkHolder.start();
        parkWaiter.start();
        return "ok";
    }
}
