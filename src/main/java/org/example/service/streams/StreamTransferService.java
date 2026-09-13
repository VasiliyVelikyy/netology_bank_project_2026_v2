package org.example.service.streams;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.data.TransferGeneratorService;
import org.example.service.BankAccountService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

import static org.example.util.LoggingUtils.loggingCommonPool;
import static org.example.util.LoggingUtils.loggingCustomPoolStats;
import static org.example.util.Constants.TRANSFER_COUNT;
import static org.example.util.TimeUtil.evaluateExecutionTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class StreamTransferService implements ApplicationRunner {
    private final TransferGeneratorService transferGeneratorService;
    private final BankAccountService bankAccountService;

    private List<TransferGeneratorService.TransferOperation> operations;

    @Override
    public void run(ApplicationArguments args) {
        operations = transferGeneratorService.generateTransfer(TRANSFER_COUNT);
    }

    public String startStream() {
        long start = System.nanoTime();

        operations.forEach(op ->
                                   bankAccountService.transferForStream(op.from(),
                                                                         op.to(), op.amount()));
        return evaluateExecutionTime(start);
    }

    public String startParallelStream() {
        long start = System.nanoTime();

        operations.parallelStream().forEach(op ->
                                                    bankAccountService.transferForStream(op.from(),
                                                                                          op.to(),
                                                                                          op.amount()));

        loggingCommonPool();
        return evaluateExecutionTime(start);
    }

    public String startParallelStreamBlock() {
        long start = System.nanoTime();

        operations.parallelStream()
                .forEach(op -> bankAccountService.transferForStreamBlockOneMonitor(op.from(), op.to(), op.amount()));
        loggingCommonPool();

        return evaluateExecutionTime(start);
    }

    public String startForkJoinPoolParallelStream() {
        long start = System.nanoTime();

        ForkJoinPool customPool = new ForkJoinPool(4);

        customPool.submit(() ->
                operations.parallelStream()
                        .forEach(op -> bankAccountService.transferForStream(op.from(), op.to(), op.amount()))
        ).join();

        loggingCustomPoolStats(customPool);

        customPool.shutdown();

        return evaluateExecutionTime(start);
    }


}
