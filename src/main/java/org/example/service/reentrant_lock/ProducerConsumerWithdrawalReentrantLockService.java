package org.example.service.reentrant_lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.BankAccount;
import org.example.service.bank_account.BankAccountService;
import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProducerConsumerWithdrawalReentrantLockService {
    private final BankAccountService bankAccountService;
    private final Queue<WithdrawalRequest> pendingRequest = new ConcurrentLinkedQueue<>();
    private final Lock lock = new ReentrantLock();
    private final Condition newRequestArrived = lock.newCondition();
    private final Condition withdrawalApproved = lock.newCondition();


    public void requestLargeWithdrawal(String accountNumber, double amount) throws InterruptedException {
        WithdrawalRequest request = new WithdrawalRequest(accountNumber, amount);

        lock.lock();
        try {
            log.info("Клиент {} запросил снятие {} . Ожидает подверждения менеджера", accountNumber, amount);
            pendingRequest.add(request);

            newRequestArrived.signal();

            withdrawalApproved.await();

            log.info("Запрос клиента {} одобрен . Выполняем снятие ", accountNumber);
            executeWithdrawal(accountNumber, amount);
        } finally {
            lock.unlock();
        }
    }

    public void processNestWithdrawal() throws InterruptedException {
        WithdrawalRequest next;

        lock.lock();
        try {
            while (pendingRequest.isEmpty()) {
                log.info("[{}] ожидает поступления заявок", Thread.currentThread().getName());
                newRequestArrived.await();
            }
            next = pendingRequest.poll();
        } finally {
            lock.unlock();
        }

        log.info("[{}] проверяет заявку на сумму {} для счета", Thread.currentThread().getName(),
                next.amount, next.accountNumber);
        Thread.sleep(500);
        log.info("[{}] одобрил заявку для счета {}", Thread.currentThread().getName(), next.accountNumber);

        lock.lock();
        try {
            withdrawalApproved.signal();
        } finally {
            lock.unlock();
        }

    }


    private static class WithdrawalRequest {
        String accountNumber;
        double amount;

        public WithdrawalRequest(String accountNumber, double amount) {
            this.accountNumber = accountNumber;
            this.amount = amount;
        }
    }

    private void executeWithdrawal(String accountNum, double amount) {
        BankAccount account = bankAccountService.getAccount(accountNum);
        if (account.getBalance() < amount) {
            throw new RuntimeException("Недостаточно средств на счёте " + accountNum);
        }
        account.setBalance(account.getBalance() - amount);
        bankAccountService.save(account);
        log.info("Снято {} со счёта {}. Текущий баланс: {}", amount, accountNum, account.getBalance());
    }

}
