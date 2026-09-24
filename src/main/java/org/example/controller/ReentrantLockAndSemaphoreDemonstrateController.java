package org.example.controller;


import lombok.RequiredArgsConstructor;
import org.example.service.reentrant_lock.ProcessReentrantLockService;
import org.example.service.semaphore.WithdrawalSemaphoreService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReentrantLockAndSemaphoreDemonstrateController {
    private final ProcessReentrantLockService processReentrantLockService;
    private final WithdrawalSemaphoreService withdrawalSemaphoreService;

    @GetMapping("/try-lock")
    public String processTryLock() throws InterruptedException {
        return processReentrantLockService.requestSmsNotification();
    }

    @GetMapping("/process-producer-consumer")
    public String processProducerConsumer() throws InterruptedException {
        return processReentrantLockService.processProducerConsumer();
    }

    @GetMapping("/process-semaphore")
    public String processSemaphore() throws InterruptedException {
        return withdrawalSemaphoreService.requestSemaphoreWithdrawal();
    }

    @GetMapping("/process-read-write-lock")
    public String processReentrantReadWriteLock() throws InterruptedException {
       return processReentrantLockService.processReentrantReadWriteLock();
    }

    @GetMapping("/process-read-write-lock-downgrade")
    public String processReentrantReadWriteLockDowngrade() throws InterruptedException {
        return processReentrantLockService.processReentrantReadWriteLockDowngrade();
    }

}
