package org.example.service.threadservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.BankAccountService;
import org.springframework.stereotype.Service;

import static org.example.util.LoggingUtils.loggingStartThread;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransferBlockedStateService {
    private final BankAccountService bankAccountService;

    public String startBlocked() {
        Object monitor = new Object();
        Thread blocker = new Thread(() -> {
            loggingStartThread();
            bankAccountService.transferWithBlock("ACC001", "ACC002", 20.0, monitor);
        }, "Trasnfer-blocker");


        Thread blockee = new Thread(() -> {
            loggingStartThread();
            bankAccountService.transferWithBlock("ACC002", "ACC003", 20.0, monitor);
        }, "Transfer-blockee");

        blocker.start();
        blockee.start();
        return "ok";
    }
}
