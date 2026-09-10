package org.example.service.streams;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.data.TransferGeneratorService;
import org.example.service.BankAccountService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.example.task.LoggingUtils.loggingCommonPool;
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
    public void run(ApplicationArguments args) throws Exception {
        operations = transferGeneratorService.generateTransfer(TRANSFER_COUNT);
    }

    public String startStream() {
        long start = System.nanoTime();

        operations.forEach(op ->
                                   bankAccountService.transferWithStream(op.from(),
                                                                         op.to(), op.amount()));
        return evaluateExecutionTime(start);
    }

    public String startParallelStream() {
        long start = System.nanoTime();

        operations.parallelStream().forEach(op ->
                                                    bankAccountService.transferWithStream(op.from(),
                                                                                          op.to(),
                                                                                          op.amount()));

        loggingCommonPool();
        return evaluateExecutionTime(start);
    }
}
