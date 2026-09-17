package org.example.service.semaphore;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.Semaphore;

import static org.example.util.LoggingUtils.loggingThreadError;

@Service
@Slf4j
public class WithdrawalSemaphoreService {
    private final Semaphore caschierSemaphore = new Semaphore(3);

    public String requestSemaphoreWithdrawal() throws InterruptedException {
        Runnable clientTask = () -> {
            String name = Thread.currentThread().getName();
            withdrawCash(name, 10_000);
        };

        Thread t1 = new Thread(clientTask, "Клиент-1");
        Thread t2 = new Thread(clientTask, "Клиент-2");
        Thread t3 = new Thread(clientTask, "Клиент-3");
        Thread t4 = new Thread(clientTask, "Клиент-4");
        Thread t5 = new Thread(clientTask, "Клиент-5");
        Thread t6 = new Thread(clientTask, "Клиент-6");

        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        t6.start();

        t1.join();
        t2.join();
        t3.join();
        t4.join();
        t5.join();
        t6.join();

        var message = "Тест завершен";
        log.info(message);
        return message;
    }

    private void withdrawCash(String clientName, double amount) {
        boolean permitAcquired = false;

        try {
            permitAcquired = caschierSemaphore.tryAcquire();
            if (permitAcquired) {
                try {
                    log.info("Клиент {} начинает свое обслуживание в банкомате. Сумма санятия {}",
                            clientName, amount);

                    Thread.sleep(6000);
                    log.info("Клиент {} успешно завершил снятие {}", clientName, amount);
                } finally {
                    caschierSemaphore.release();
                    log.debug("Банкомат освободился после обслуживание клиента {}", clientName);

                }
            } else {
                log.warn("Клиент {} ушел без обслуживания. Не удалось получить доступ к банкомату", clientName);
            }

        } catch (Exception e) {
            loggingThreadError(e);

            caschierSemaphore.release();
        }
    }


}
