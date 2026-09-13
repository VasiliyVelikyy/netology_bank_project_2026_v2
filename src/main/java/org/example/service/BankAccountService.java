package org.example.service;


import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.BankAccount;
import org.example.repo.BankAccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;

import static org.example.util.LoggingUtils.loggingMoneyTransfer;
import static org.example.util.TaskSimulateWork.simulateCpuWork;

@Slf4j
@Service

public class BankAccountService {
    private final BankAccountRepository bankAccountRepository;

    private final Object monitor = new Object();

    public BankAccountService(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }

    public BankAccount getAccount(String accountNumber) {
        Optional<BankAccount> account = bankAccountRepository.findById(accountNumber);
        return account.orElseThrow(() ->
                                           new RuntimeException("Счёт не найден: " + accountNumber));
    }

    public void transfer(String accountFrom, String accountTo, double amount) {
        BankAccount from = getAccount(accountFrom);
        BankAccount to = getAccount(accountTo);

        if (from.getBalance() < amount) {
            throw new RuntimeException("Недостаточно средств " + accountFrom);
        }

        setTransferAmountAndSave(amount, from, to);
    }

    public void transferWithBlock(String accountFrom, String accountTo, double amount, Object monitor) {

        synchronized (monitor) {
            String threadName = Thread.currentThread().getName();

            log.info(threadName + " захватил монитор");
            BankAccount from = getAccount(accountFrom);
            BankAccount to = getAccount(accountTo);

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
            BankAccount from = getAccount(accountFrom);
            BankAccount to = getAccount(accountTo);

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
    public void transferWithPark(String fromNum, String toNum, Lock lock, double amount, boolean shouldHoldLock) {
        log.info( "{} : пытается захватить lock",Thread.currentThread().getName());

        lock.lock();
        try {
            log.info("{} : ЗАХВАТИЛ lock.",Thread.currentThread().getName());

            BankAccount fromAcc = getAccount(fromNum);
            BankAccount toAcc = getAccount(toNum);

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

    public long count() {
        return bankAccountRepository.count();
    }

    public void saveAll(List<BankAccount> bankAccounts) {
        bankAccountRepository.saveAll(bankAccounts);
    }

    public List<BankAccount> findAll() {
        return bankAccountRepository.findAll();
    }


    public void transferForStream(String fromAcc, String toAcc, double amount) {

        String first = fromAcc.compareTo(toAcc) < 0 ? fromAcc : toAcc;
        String second = fromAcc.compareTo(toAcc) < 0 ? toAcc : fromAcc;

        synchronized (first.intern()) {
            synchronized (second.intern()) {
                BankAccount from = getAccount(fromAcc);
                BankAccount to = getAccount(toAcc);

                if (from.getBalance() < amount) {
                    throw new RuntimeException("Недостаточно средств " + from);
                }

                setTransferAmountAndSave(amount, from, to);
            }
        }
    }

    public void transferForStreamBlockOneMonitor(String from, String to, double amount) {
        synchronized (monitor) {

            BankAccount fromAcc = getAccount(from);
            BankAccount toAcc = getAccount(to);

            if (fromAcc.getBalance() < amount) {
                throw new RuntimeException("Недостаточно средств: " + from);
            }

            setTransferAmountAndSave(amount, fromAcc, toAcc);

            loggingMoneyTransfer(from, to, amount);
        }
    }

    private void setTransferAmountAndSave(double amount, BankAccount from, BankAccount to) {
        double newFromBalance = Math.round((from.getBalance() - amount) * 100.0) / 100.0;
        double newToBalance = Math.round((to.getBalance() + amount) * 100.0) / 100.0;

        from.setBalance(newFromBalance);
        to.setBalance(newToBalance);

        bankAccountRepository.save(from);
        bankAccountRepository.save(to);
    }


}
