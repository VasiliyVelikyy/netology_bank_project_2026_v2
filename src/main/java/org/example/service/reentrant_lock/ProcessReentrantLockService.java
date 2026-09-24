package org.example.service.reentrant_lock;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.BankAccount;
import org.example.service.bank_account.BankAccountService;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.example.util.Constants.ACC_1;
import static org.example.util.LoggingUtils.loggingThreadError;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessReentrantLockService {
    private final SmsNotificationService smsNotificationService;
    private final ProducerConsumerWithdrawalReentrantLockService producerConsumerWithdrawalReentrantLockService;
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final BankAccountService bankAccountService;

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

    public String processReentrantReadWriteLock() throws InterruptedException {
        int readerThreads = 5;
        Thread writer1 = new Thread(() -> deposit(ACC_1, 5000), "Deposit thread 1");
        Thread writer2 = new Thread(() -> withdraw(ACC_1, 200), "Withdrawal thread 1");
        Thread writer3 = new Thread(() -> withdraw(ACC_1, 100), "Withdrawal thread 2");


        Thread[] readers = new Thread[readerThreads];

        for (int i = 0; i < readerThreads; i++) {
            int id = i + 1;
            readers[i] = new Thread(() -> printBalance(ACC_1), "Reader " + id);
            readers[i].start();
        }

        writer1.start();
        Thread.sleep(400);

        writer2.start();
        writer3.start();

        writer1.join();
        writer2.join();
        writer3.join();

        for (Thread reader : readers) {
            reader.join();
        }

        return "ok";

    }

    private void printBalance(String accNum) {
        readWriteLock.readLock().lock();
        try {
            BankAccount acc = bankAccountService.getAccount(accNum);
            log.info("{} захватил readlock. Чтение баланса {} со счета{}", Thread.currentThread().getName(),
                    acc.getBalance(), accNum);

            Thread.sleep(10000);

        } catch (Exception e) {
            loggingThreadError(e);
        } finally {
            readWriteLock.readLock().unlock();
            log.info("{}, освободил readlock для счета {}",
                    Thread.currentThread().getName(),
                    accNum);
        }
    }

    private void withdraw(String accNum, int amount) {
        readWriteLock.writeLock().lock();
        try {
            BankAccount acc = bankAccountService.getAccount(accNum);
            log.info("{} захватил writelock. Начало списания со счета {} , {}", Thread.currentThread().getName(),
                    accNum, amount);

            double balance = acc.getBalance();

            if (balance >= amount) {
                acc.setBalance(acc.getBalance() - amount);
                Thread.sleep(800);
                bankAccountService.save(acc);
                log.info("Списание с Баланса счета {} проведено, новый баланс {}", accNum, acc.getBalance());
            } else {
                log.info("Операция отклонена за недостатком средств. {} ,{} ,{}", Thread.currentThread().getName(),
                        accNum, amount);
            }

        } catch (Exception e) {
            loggingThreadError(e);
        } finally {
            readWriteLock.writeLock().unlock();
            printUnlockOperation(accNum);
        }
    }

    private void deposit(String accNum, double amount) {
        readWriteLock.writeLock().lock();
        try {
            BankAccount acc = bankAccountService.getAccount(accNum);
            log.info("{} захватил writelock. Начало пополнения счета {} , {}", Thread.currentThread().getName(),
                    accNum, amount);
            acc.setBalance(acc.getBalance() + amount);

            Thread.sleep(800);
            bankAccountService.save(acc);
            log.info("Баланс счета {} успешно пополнен, новый баланс {}", accNum, acc.getBalance());

        } catch (Exception e) {
            loggingThreadError(e);
        } finally {
            readWriteLock.writeLock().unlock();
            printUnlockOperation(accNum);
        }
    }

    private static void printUnlockOperation(String accNum) {
        log.info("{} освободил writelock для счета {}", Thread.currentThread().getName(),
                accNum);
    }

    public String processReentrantReadWriteLockDowngrade() throws InterruptedException {
        String acc1 = ACC_1;
        BankAccount acc = bankAccountService.getAccount(acc1);
        log.info("{} Начальный баланс {} ", acc1, acc.getBalance());

        Thread downgradeWrite = new Thread(() -> updateAndGenerateReport(acc1, 500), "Поток -downgrade Обновление отчета");
        downgradeWrite.start();


        Thread.sleep(200);
        Thread reader = new Thread(() -> printBalance(acc1), "Поток- читатель (проверка)");
        reader.start();

        Thread.sleep(200);
        Thread interferierWrite = new Thread(() -> withdraw(acc1, 200), "Поток помеха (списание)");
        interferierWrite.start();


        downgradeWrite.join();
        interferierWrite.join();
        reader.join();

        return "ok";
    }

    private void updateAndGenerateReport(String accNum, int amount) {

        BankAccount acc = bankAccountService.getAccount(accNum);
        var balance = acc.getBalance();

        readWriteLock.writeLock().lock();
        try {
            log.info("{} захватил writelock начинаю изменение баланса счета {}", Thread.currentThread().getName(),
                    accNum);

            balance += amount;
            log.info("{} баланс {} изменен , Новое значение", Thread.currentThread().getName(), amount, balance);

            readWriteLock.readLock().lock();
        } finally {
            readWriteLock.writeLock().unlock();
            log.info("{} освободил write lock , но все еще держу readlock. Другие писатели теперь ждут", Thread.currentThread().getName());
        }

        try {
            log.info("{} Начинаю операцию генерации отчета. Баланс зафиксирован на {}",
                    Thread.currentThread().getName(), balance);
            Thread.sleep(2000);
            log.info("{} Отчет сформирован", Thread.currentThread().getName());
        } catch (Exception e) {
            loggingThreadError(e);
        } finally {
            readWriteLock.readLock().unlock();
        }

    }
}
