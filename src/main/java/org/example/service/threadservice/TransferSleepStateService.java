package org.example.service.threadservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TransferSleepStateService {

    public String startSleep() {
        Thread threadSleep = new Thread(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                log.error("error " + e.getMessage());
            }
        },
                                        "sleepthread");
        threadSleep.start();
        return "ok";
    }
}
