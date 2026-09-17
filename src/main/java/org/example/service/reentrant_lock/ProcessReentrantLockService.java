package org.example.service.reentrant_lock;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static org.example.util.LoggingUtils.loggingThreadError;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessReentrantLockService {
    private final SmsNotificationService smsNotificationService;
    private final ProducerConsumerWithdrawalReentrantLockService producerConsumerWithdrawalReentrantLockService;

    public String requestSmsNotification() throws InterruptedException {
        String phone = "+7800_____";
        Thread t1 = new Thread(() -> smsNotificationService.trySendSms(phone, "SMS1 : ваш баланс изменился"));


        Thread t2 = new Thread(() -> {
            try {
                Thread.sleep(100);
                smsNotificationService.trySendSms(phone, "SMS2 : У вас новое уведомление");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();
        var message = "Тест завершен";
        log.info(message);
        return message;
    }

    public String processProducerConsumer() throws InterruptedException {
        Thread clientGenerator = new Thread(() -> {
            try {
                for (int i = 1; i <= 8; i++) {
                    String account = "ACC0" + String.format("%02d", i);
                    double amount = Math.round(1000 + (Math.random() * 1000));

                    log.info("[Генератор] создает заявку #{} снятия со счета{} сумма {}", i, account, amount);

                    try {
                        producerConsumerWithdrawalReentrantLockService.requestLargeWithdrawal(account, amount);
                        log.info("[Генератор]  Заявка #{} для  счета{} обработана", i, account);
                    } catch (Exception e) {
                        loggingThreadError(e);
                    }
                    Thread.sleep(300);

                }

            } catch (Exception e) {
                loggingThreadError(e);
            }
        }, "Client reetrant lock generator");


        Thread manager1 = new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    producerConsumerWithdrawalReentrantLockService.processNestWithdrawal();
                }

            } catch (Exception e) {
                loggingThreadError(e);
            }
        }, "Manager reentrant lock 1");


        Thread manager2 = new Thread(() -> {
            try {
                for (int i = 0; i < 3; i++) {
                    producerConsumerWithdrawalReentrantLockService.processNestWithdrawal();
                }

            } catch (Exception e) {
                loggingThreadError(e);
            }
        }, "Manager reentrant lock 2");

        clientGenerator.start();
        manager1.start();
        manager2.start();


        clientGenerator.join();
        manager1.join();
        manager2.join();


        var message = "Тест завершен";
        log.info(message);
        return message;
    }
}
