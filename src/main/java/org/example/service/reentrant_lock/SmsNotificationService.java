package org.example.service.reentrant_lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static org.example.util.LoggingUtils.loggingThreadError;

@Slf4j
@Service
public class SmsNotificationService {
    private static final int LOCK_COUNT = 1024;
    private final ReentrantLock[] locks = new ReentrantLock[LOCK_COUNT];

    //todo Concurent Hash Map

    public SmsNotificationService() {
        for (int i = 0; i < locks.length; i++) {
            locks[i] = new ReentrantLock();
        }
    }

    public void trySendSms(String phoneNumber, String message) {
        int lockIndex = Math.abs(phoneNumber.hashCode()) % LOCK_COUNT;
        var lock = locks[lockIndex];

        boolean lockAcquired = false;

        try {
            lockAcquired = lock.tryLock(1500, TimeUnit.MILLISECONDS);
            if (lockAcquired) {
                try {
                    log.info("Начало отправки SMS: '{}' на номер", message, phoneNumber);
                    Thread.sleep(1000);

                    log.info("SMS успешно отправлено на номер {}", phoneNumber);
                } finally {
                    lock.unlock();
                }
            } else {
                log.warn("Отправка SMS пропущена : не удалось получить блокировку для номера {}" +
                                " Клиент получет уже уводмление",
                        phoneNumber);
            }
        } catch (Exception e) {
            loggingThreadError(e);

        }
    }
}

