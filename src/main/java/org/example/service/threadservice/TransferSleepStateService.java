package org.example.service.threadservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static org.example.util.LoggingUtils.loggingThreadError;

@Service
@Slf4j
public class TransferSleepStateService {

    public String startSleep() {
        Thread threadSleep = new Thread(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                loggingThreadError(e);
            }
        },
                "sleepthread");
        threadSleep.start();
        return "ok";
    }
}
