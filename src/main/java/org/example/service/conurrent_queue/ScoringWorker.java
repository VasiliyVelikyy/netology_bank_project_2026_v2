package org.example.service.conurrent_queue;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class ScoringWorker {
    public static final int THREAD_COUNT = 10;
    private final CreditProcessingService creditProcessingService;
    private final ExecutorService consumerExecutor;

    public ScoringWorker(CreditProcessingService linkedCreditProcessingService) {
        this.creditProcessingService = linkedCreditProcessingService;
        this.consumerExecutor = Executors.newFixedThreadPool(THREAD_COUNT);
    }

    @PostConstruct
    public void startWorker() {
        for (int i = 0; i < THREAD_COUNT; i++) {
            log.info("Waiting applications");
            consumerExecutor.submit(this::processApplications);
        }

        log.info("Старт 10 потоков потребителей");
    }

    private void processApplications() {
        while (!Thread.currentThread().isInterrupted()) {
            var app = creditProcessingService.take();
            double riskScore = app.amount() / 10_000.0;
            boolean approve = riskScore < 5.0;
            log.info("Proccessed app {} amount {}, approve {}", app.accountNumber(), app.amount(), approve);
        }
    }
}
