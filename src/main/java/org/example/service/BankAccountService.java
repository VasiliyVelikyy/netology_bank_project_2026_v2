package org.example.service;


import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.BankAccount;
import org.example.repo.BankAccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;

import static org.example.util.TaskSimulateWork.simulateCpuWork;

@Slf4j
@Service

public class BankAccountService {
    private final BankAccountRepository bankAccountRepository;

    public BankAccountService(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }

    public List<BankAccount> getAllAccounts() {
        return bankAccountRepository.findAll();
    }

    public BankAccount getAccount(String accountNumber) {
        Optional<BankAccount> account = bankAccountRepository.findById(accountNumber);
        return account.orElseThrow(() ->
                                           new RuntimeException("Счёт не найден: " + accountNumber));
    }

    public BankAccount saveAccount(BankAccount account) {
        return bankAccountRepository.save(account);
    }

    public void transfer(String accountFrom, String accountTo, double amount) {
        BankAccount from = getAccount(accountFrom);
        BankAccount to = getAccount(accountTo);

        if (from.getBalance() < amount) {
            throw new RuntimeException("Недостаточно средств " + accountFrom);
        }

        from.setBalance(from.getBalance() - amount);
        to.setBalance(to.getBalance() + amount);

        bankAccountRepository.save(from);
        bankAccountRepository.save(to);
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

            simulateCpuWork(threadName, 5000);

            from.setBalance(from.getBalance() - amount);
            to.setBalance(to.getBalance() + amount);

            bankAccountRepository.save(from);
            bankAccountRepository.save(to);

            log.info(threadName + " перевод " + amount + "выполне с " + accountFrom + " на " + accountTo);
        }
    }


    public void transferWithWait(String accountFrom, String accountTo, double amount, Object monitor, boolean shouldWait) {

        synchronized (monitor) {
            String threadName = Thread.currentThread().getName();

            log.info(threadName + " захватил монитор");
            BankAccount from = getAccount(accountFrom);
            BankAccount to = getAccount(accountTo);

            if (from.getBalance() < amount) {
                throw new RuntimeException("Недостаточно средств " + accountFrom);
            }
            if (shouldWait) {
                log.info(threadName + " захватил монитор , теперб жду через wait");
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(" Операция прервана");
                }
                log.info(threadName + " проснулся после notify");
            } else {
                log.info(threadName + " захватил монитор , вызываю notify и уходит");
                monitor.notify();
            }

            simulateCpuWork(threadName, 5000);

            from.setBalance(from.getBalance() - amount);
            to.setBalance(to.getBalance() + amount);

            bankAccountRepository.save(from);
            bankAccountRepository.save(to);

            log.info(threadName + " перевод " + amount + "выполне с " + accountFrom + " на " + accountTo);
        }
    }

    @Transactional
    public void transferWithPark(String fromNum, String toNum, Lock lock, double amount, boolean shouldHoldLock) {
        log.info(Thread.currentThread().getName() + ": пытается захватить lock");

        lock.lock();
        try {
            log.info(Thread.currentThread().getName() + ": ЗАХВАТИЛ lock.");

            BankAccount fromAcc = getAccount(fromNum);
            BankAccount toAcc = getAccount(toNum);

            if (fromAcc.getBalance() < amount) {
                throw new RuntimeException("Недостаточно средств: " + fromNum);
            }

//            if (shouldHoldLock) {
//
//            }
            log.info(Thread.currentThread().getName() + ": удерживаю lock несколько секунд (имитация долгой операции)");
            simulateCpuWork(Thread.currentThread().getName(), 10000);

            fromAcc.setBalance(fromAcc.getBalance() - amount);
            toAcc.setBalance(toAcc.getBalance() + amount);

            log.info(Thread.currentThread().getName() + ": перевод " + amount + " с " + fromNum + " на " + toNum + " выполнен.");
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


    public void transferWithStream(String fromAcc, String toAcc, double amount) {

        String first = fromAcc.compareTo(toAcc) < 0 ? fromAcc : toAcc;
        String second = fromAcc.compareTo(toAcc) < 0 ? toAcc : fromAcc;

        synchronized (first.intern()) {
            synchronized (second.intern()) {
                BankAccount from = getAccount(fromAcc);
                BankAccount to = getAccount(toAcc);

                if (from.getBalance() < amount) {
                    throw new RuntimeException("Недостаточно средств " + from);
                }

                from.setBalance(from.getBalance() - amount);
                to.setBalance(to.getBalance() + amount);

                bankAccountRepository.save(from);
                bankAccountRepository.save(to);
            }
        }
    }
}
