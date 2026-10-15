package org.example.task;

import lombok.RequiredArgsConstructor;
import org.example.domain.BankAccount;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

import static org.example.util.Constants.BATCH_FOR_FORK_JOIN;

@RequiredArgsConstructor
public class InterestAccrualUpdateTask extends RecursiveTask<List<BankAccount>> {

    private final List<BankAccount> accounts;
    private final int start;
    private final int end;

    public InterestAccrualUpdateTask(List<BankAccount> accounts) {
        this(accounts, 0, accounts.size());
    }

    @Override
    protected List<BankAccount> compute() {
        if (end - start <= BATCH_FOR_FORK_JOIN) {
            return accounts.subList(start, end)
                    .stream()
                    .map(acc -> new BankAccount(acc.getAccountNumber(), (double) Math.round(acc.getBalance() * 1.1 * 100) / 100))
                    .toList();
        }
        else {
            int mid = (start + end) >>> 1;
            var left = new InterestAccrualUpdateTask(accounts,start,mid);
            var right = new InterestAccrualUpdateTask(accounts,mid,end);
            invokeAll(left,right);
            var result = new ArrayList<BankAccount>();
            result.addAll(left.join());
            result.addAll(right.join());
            return result;
        }
    }
}
