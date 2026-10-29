package org.example.service.conurrent_queue;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.CreditApplication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class LinkedCreditProcessingService implements CreditProcessingService {
    private final BlockingQueue<CreditApplication> queue = new LinkedBlockingQueue<>(50);
    private final ExecutorService producerExecutor = Executors.newFixedThreadPool(10);

    private final AtomicInteger submittedCount = new AtomicInteger(0);
    private final AtomicInteger processedCount = new AtomicInteger(0);


    @Override
    public String submit(List<CreditApplication> applications) {
        int total = applications.size();
        int chunkSize = (int) Math.ceil((double) total / 10);

        for (int i = 0; i < 10; i++) {
            int start = i * chunkSize;
            int end = Math.min(start + chunkSize, total);
            if (start >= total) break;

            List<CreditApplication> chunk = applications.subList(start, end);
            producerExecutor.submit(() -> produceApplications(chunk));

        }
        return String.format("Заявки размещены %s в ArrayBlockingQueue", total);
    }


    @Override
    public CreditApplication take() {
        try {
            CreditApplication application = queue.take();
            processedCount.incrementAndGet();
            return application;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private void produceApplications(List<CreditApplication> chunk) {
        for (var app : chunk) {
            try {
                queue.put(app);
                submittedCount.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        try {
            if (!producerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                producerExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            producerExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public String getState() {
        return String.format("Обработанные %s , размещенные %s , queuesize = %s", processedCount.get(), submittedCount.get(), queue.size());
    }
}
