package org.example.task;

import lombok.RequiredArgsConstructor;
import org.example.domain.BankAccount;
import org.example.util.Scoring;

import java.util.List;
import java.util.concurrent.RecursiveTask;

import static org.example.util.Constants.BATCH_FOR_FORK_JOIN;


@RequiredArgsConstructor
public class ScoringSumTask extends RecursiveTask<Long> {
    private final List<BankAccount> accounts;
    private final int start;
    private final int end;

    public ScoringSumTask(List<BankAccount> accounts) {
        this(accounts, 0, accounts.size());
    }

    @Override
    protected Long compute() {
        int size = end - start;

        if (size <= BATCH_FOR_FORK_JOIN) {
            long sum = 0;

            for (int i = start; i < end; i++) {
               // sum = (long) (sum + curr.getBalance());
                sum = Scoring.score(accounts.get(i).getBalance());
            }
            return sum;
        } else {
            int mid = (start + end) >>> 1; //int mid = (start +end) / 2;

            ScoringSumTask left = new ScoringSumTask(accounts, start, mid);
            ScoringSumTask right = new ScoringSumTask(accounts, mid, end);

            left.fork();

            long rightResult = right.compute();
            long leftResult = left.join();

            return rightResult + leftResult;

        }
    }
}
