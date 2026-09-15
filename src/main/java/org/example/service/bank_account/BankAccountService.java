package org.example.service.bank_account;


import lombok.extern.slf4j.Slf4j;
import org.example.domain.BankAccount;
import org.example.repo.BankAccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static org.example.util.LoggingUtils.loggingMoneyTransfer;

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
        return account.orElseThrow(() -> new RuntimeException("Счёт не найден: " + accountNumber));
    }


    public void transfer(String accountFrom, String accountTo, double amount) {
        BankAccount from = getAccount(accountFrom);
        BankAccount to = getAccount(accountTo);

        if (from.getBalance() < amount) {
            throw new RuntimeException("Недостаточно средств " + accountFrom);
        }

        setTransferAmountAndSave(amount, from, to);
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


    public void transferWithDoubleSync(String fromAcc, String toAcc, double amount) {

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
        loggingMoneyTransfer(fromAcc,toAcc,amount);
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


    public void transferWithDeadlock(String fromAcc, String toAcc, double amount) throws InterruptedException {

        synchronized (fromAcc.intern()) {
            log.info("{} ,захватил {}",Thread.currentThread().getName(),fromAcc);

            Thread.sleep(100);

            log.info("{} ,пытается захватить {}",Thread.currentThread().getName(),toAcc);

            synchronized (toAcc.intern()) {
                BankAccount from = getAccount(fromAcc);
                BankAccount to = getAccount(toAcc);

                if (from.getBalance() < amount) {
                    throw new RuntimeException("Недостаточно средств " + from);
                }

                setTransferAmountAndSave(amount, from, to);

                loggingMoneyTransfer(fromAcc,toAcc,amount);
            }
        }
    }
}
