package org.example.service.threadservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.bank_account.BankAccountService;
import org.example.task.LoggerTask;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransferDaemonService {

    private final BankAccountService bankAccountService;

    public String startDemon() {
        var loggedTask = new LoggerTask(bankAccountService);
        Thread loggedthread = new Thread(loggedTask);
        loggedthread.setDaemon(true);
        loggedthread.start();
        return "ok";
    }
}
