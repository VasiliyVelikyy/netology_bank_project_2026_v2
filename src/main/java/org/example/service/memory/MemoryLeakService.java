package org.example.service.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MemoryLeakService {
    private static final List<byte[]> MEMORY_HOG = new ArrayList<>();

    public void demonstrateMemoryLeak() {

        long counter = 0;

        while (true) {
            MEMORY_HOG.add(new byte[1024*1024]);

            counter++;

            if (counter % 10 == 0) {
                log.info("Добавлено {} блоков по 10мб. Всего {}", counter,
                        counter * 10L);
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }


    }
}
