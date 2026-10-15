package org.example.service.fork_join;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.BankAccount;
import org.example.service.bank_account.BankAccountService;
import org.example.task.InterestAccrualUpdateTask;
import org.example.task.ScoringSumTask;
import org.example.util.Scoring;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

import static org.example.util.TimeUtil.evaluateExecutionTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForkJoinBalanceService {

    private final ForkJoinPool forkJoinPool = new ForkJoinPool(10);
    private final BankAccountService bankAccountService;

    public Long getBalanceScoringRecursive() {
        log.info("Рекурсивное суммирование");

        long startTime = System.nanoTime();

        //todo отрефакторить findAll в управлении производительностью
        var accounts = bankAccountService.findAll();

        ScoringSumTask task = new ScoringSumTask(accounts);
        long totalScore = forkJoinPool.invoke(task);

        log.info("Общий score по аккаунтам ={} кол во аккаунтов ={}", totalScore, accounts.size());
        evaluateExecutionTime(startTime);
        return totalScore;
    }

    public Long getBalanceScoringSequential() {
        log.info("Последовательное суммирование");

        long startTime = System.nanoTime();
        var accounts = bankAccountService.findAll();

        long totalScore = 0;
        for (var account : accounts) {
            totalScore = Scoring.score(account.getBalance());
        }

        log.info("Общий score по аккаунтам ={} кол во аккаунтов ={}", totalScore, accounts.size());
        evaluateExecutionTime(startTime);
        return totalScore;
    }

    public void calculateInterestRecursive() {
        List<BankAccount> bankAccounts = bankAccountService.findAll();

        List<BankAccount> updated = forkJoinPool.invoke(new InterestAccrualUpdateTask(bankAccounts));

        bankAccountService.saveAll(updated);


    }
}
