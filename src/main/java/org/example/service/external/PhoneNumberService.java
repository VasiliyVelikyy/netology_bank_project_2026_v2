package org.example.service.external;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.example.util.Constants.ACCOUNT_COUNT;
import static org.example.util.Constants.GEN_PREFIX;
import static org.example.util.TimeUtil.evaluateExecutionTime;

@Slf4j
@Service
public class PhoneNumberService {
    private static Map<String, String> phoneDb;

    public PhoneNumberService() {
        long startTime = System.nanoTime();
        phoneDb = generatePhones();
        log.info("Сгенерировано счетов {}", phoneDb.size());
        evaluateExecutionTime(startTime);
    }


    public String findPhoneNumberByAccountNumber(String accountNumber) throws InterruptedException {
        Thread.sleep(10);
        var elem = phoneDb.get(accountNumber);
        if (elem == null) {
            return ""; //todo demonstrate exception
        }
        return elem;
    }

    private Map<String, String> generatePhones() {
        Map<String, String> map = new HashMap<>(ACCOUNT_COUNT);

        for (int i = 1; i <= 8; i++) {
            String key = String.format("ACC%03d", i);
            map.put(key, generateRandomPhoneNumber());
        }

        for (int i = 9; i <= ACCOUNT_COUNT; i++) {
            map.put(GEN_PREFIX + i, generateRandomPhoneNumber());

        }
        return Collections.unmodifiableMap(map);
    }

    private String generateRandomPhoneNumber() {
        int length = ThreadLocalRandom.current().nextInt(10, 13);
        StringBuilder sb = new StringBuilder("+");
        for (int i = 0; i < length; i++) {
            sb.append(ThreadLocalRandom.current().nextInt(10));

        }
        return sb.toString();
    }
}
