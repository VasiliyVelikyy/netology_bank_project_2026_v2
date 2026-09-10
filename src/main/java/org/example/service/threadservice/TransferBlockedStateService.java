package org.example.service.threadservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.BankAccountService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransferBlockedStateService {
    private final BankAccountService bankAccountService;

    public String startBlocked() {
        Object monitor = new Object();
        Thread blocker = new Thread(() -> {
            log.info("поток " + Thread.currentThread().getName() + " стартовал");
            bankAccountService.transferWithBlock("ACC001", "ACC002", 20.0, monitor);
        }, "Trasnfer-blocker");


        Thread blockee = new Thread(() -> {
            log.info("поток " + Thread.currentThread().getName() + " стартовал");
            bankAccountService.transferWithBlock("ACC002", "ACC003", 20.0, monitor);
        }, "Transfer-blockee");

        blocker.start();
        blockee.start();
        return "ok";
    }
}
