package org.example.service.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FillEdenService {
    private static volatile byte[] sink;

    public void demonstrateFillEden() {
        log.info("Заполняем. Eden Space");

        long counter = 0;

        while (true) {
            byte[] temp = new byte[1024 * 1024];
            sink = temp;

            counter++;

            if (counter % 10 == 0) {
                log.info("Создано обьектов {}", counter);
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
