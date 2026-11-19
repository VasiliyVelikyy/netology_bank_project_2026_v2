package org.example.service.degradation;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.BankAccount;
import org.example.service.bank_account.BankAccountService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.example.util.TimeUtil.evaluateExecutionTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class DegradationDemonstrateService {
    private final BankAccountService bankAccountService;

    private static final Path SLOW_LOG_FILE = Paths.get("slow_io_sync.log").toAbsolutePath();
    private static final Path FAST_LOG_FILE = Paths.get("fast_io_sync.log").toAbsolutePath();


    private Map<String, BankAccount> clientMap;

    @EventListener(ApplicationReadyEvent.class)
    public void initCache() {
        var accounts = bankAccountService.findAll();
        clientMap = accounts.stream().collect(Collectors.toConcurrentMap(
                BankAccount::getAccountNumber,
                Function.identity(),
                (a, b) -> a
        ));

    }

    public String burnCpu() {
        long startTime = System.nanoTime();

        evaluateExecutionTime(startTime);
        long sum = 0;
        for (int i = 0; i < 1_000_000_000L; i++) {
            sum += Math.sqrt(i) * i;

        }
        evaluateExecutionTime(startTime);
        return "done=" + sum;
    }

    public String slowIo() {

        try (RandomAccessFile raf = new RandomAccessFile(SLOW_LOG_FILE.toFile(), "rw")) {
            raf.seek(raf.length());
            String entry = "Sync log entry at " + System.currentTimeMillis() + "\n";
            raf.write(entry.getBytes());

            raf.getChannel().force(true);
        } catch (Exception e) {
            return "ERROR";
        }

        return "OK";
    }

    public String fastIo() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FAST_LOG_FILE.toFile(), true))) {
            bw.write("Fast log at " + System.currentTimeMillis() + "\n");
        } catch (Exception e) {
            return "ERROR";
        }
        return "OK";

    }


    public BankAccount slowAlg(Map<String, String> request) {
        String accNum = request.get("accountNumber");
        var accounts = bankAccountService.findAll();

        for (var account : accounts) {
            if (account.getAccountNumber().equals(accNum)) {
                return account;
            }
        }
        return null;
    }


    public BankAccount fastAlg(Map<String, String> request) {
        String accNum = request.get("accountNumber");
        var account = clientMap.get(accNum);
        return account;

    }
}
