package org.example.service.bank_account;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.BankAccount;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;

import static org.example.util.LoggingUtils.loggingMoneyTransfer;
import static org.example.util.TaskSimulateWork.simulateCpuWork;

@Service
@Slf4j
@RequiredArgsConstructor
public class BankAccountProfilingExampleService {
    private final BankAccountService bankAccountService;

    public void transferWithBlock(String accountFrom, String accountTo, double amount, Object monitor) {

        synchronized (monitor) {
            String threadName = Thread.currentThread().getName();

            log.info(threadName + " захватил монитор");
            BankAccount from = bankAccountService.getAccount(accountFrom);
            BankAccount to = bankAccountService.getAccount(accountTo);

            if (from.getBalance() < amount) {
                throw new RuntimeException("Недостаточно средств " + accountFrom);
            }

            simulateCpuWork(5000);

            setTransferAmountAndSave(amount, from, to);

            loggingMoneyTransfer(accountFrom, accountTo, amount);
        }
    }


    public void transferWithWait(String accountFrom, String accountTo, double amount, Object monitor, boolean shouldWait) {

        synchronized (monitor) {
            String threadName = Thread.currentThread().getName();

            log.info( "{} захватил монитор",threadName);
            BankAccount from = bankAccountService.getAccount(accountFrom);
            BankAccount to = bankAccountService.getAccount(accountTo);

            if (from.getBalance() < amount) {
                throw new RuntimeException("Недостаточно средств " + accountFrom);
            }
            if (shouldWait) {
                log.info("{}, захватил монитор , теперб жду через wait",threadName);
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(" Операция прервана");
                }
                log.info("{} проснулся после notify",threadName);
            } else {
                log.info("{} захватил монитор , вызываю notify и уходит ",threadName);
                monitor.notify();
            }

            simulateCpuWork(5000);

            setTransferAmountAndSave(amount, from, to);

            loggingMoneyTransfer(accountFrom, accountTo, amount);
        }
    }

    @Transactional
    public void transferWithPark(String fromNum, String toNum, Lock lock, double amount) {
        log.info( "{} : пытается захватить lock",Thread.currentThread().getName());

        lock.lock();
        try {
            log.info("{} : ЗАХВАТИЛ lock.",Thread.currentThread().getName());

            BankAccount fromAcc = bankAccountService.getAccount(fromNum);
            BankAccount toAcc = bankAccountService.getAccount(toNum);

            if (fromAcc.getBalance() < amount) {
                throw new RuntimeException("Недостаточно средств: " + fromNum);
            }

            log.info("{}: удерживаю lock несколько секунд (имитация долгой операции)",Thread.currentThread().getName());
            simulateCpuWork(10000);

            fromAcc.setBalance(fromAcc.getBalance() - amount);
            toAcc.setBalance(toAcc.getBalance() + amount);

            loggingMoneyTransfer(fromNum, toNum, amount);
        } finally {
            lock.unlock();
        }
    }

    private void setTransferAmountAndSave(double amount, BankAccount from, BankAccount to) {
        double newFromBalance = Math.round((from.getBalance() - amount) * 100.0) / 100.0;
        double newToBalance = Math.round((to.getBalance() + amount) * 100.0) / 100.0;

        from.setBalance(newFromBalance);
        to.setBalance(newToBalance);
    }
}
