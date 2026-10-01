package org.example.service.executor_services;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.BankAccount;
import org.example.dto.ClientBalanceAndPhoneInfo;
import org.example.integration.PhoneWebClient;
import org.example.service.bank_account.BankAccountService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.example.util.Constants.PATH_PHONE_WITHOUT_DELAY;
import static org.example.util.Constants.PATH_PHONE_WITH_DELAY;
import static org.example.util.TimeUtil.evaluateExecutionTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientAggregationService {
    private final BankAccountService bankAccountService;
    private final PhoneWebClient phoneWebClient;

    private final ExecutorService phoneNumFetcher = Executors.newFixedThreadPool(10);

    @PreDestroy
    public void shutdown() {
        phoneNumFetcher.shutdown();
    }

    public List<ClientBalanceAndPhoneInfo> getClientBalanceAndPhoneInfoAsync() {
        long startTime = System.nanoTime();
        log.info("Начинаем асинхронную агрегацию данных по всем счетам");

        List<BankAccount> accounts = bankAccountService.findAll();
        log.info("Найдено счетов для обработки {}", accounts.size());

        List<Future<ClientBalanceAndPhoneInfo>> futures = new ArrayList<>();

        for (var acc : accounts) {
            Future<ClientBalanceAndPhoneInfo> future = phoneNumFetcher.submit(
                    () -> aggregatePhoneAndBalance(acc.getAccountNumber(), acc.getBalance(), PATH_PHONE_WITHOUT_DELAY));
            futures.add(future);
        }

        List<ClientBalanceAndPhoneInfo> result = new ArrayList<>();

        try {
            for (var future : futures) {
                var clientInfo = future.get();
                logResult(clientInfo);
                result.add(clientInfo);
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage(), e);
        }
        evaluateExecutionTime(startTime);
        return result;
    }

    public List<ClientBalanceAndPhoneInfo> getClientBalanceAndPhoneInvokeByTimeout() throws InterruptedException {
        long timeout = 1000;
        long startTime = System.nanoTime();

        log.info("Начинаем асинхронную агрегацию данных по всем счетам");

        List<BankAccount> accounts = bankAccountService.findAll();
        log.info("Найдено счетов для обработки {}", accounts.size());

        List<Callable<ClientBalanceAndPhoneInfo>> tasks = accounts
                .stream()
                .map(acc -> (Callable<ClientBalanceAndPhoneInfo>)
                        () -> aggregatePhoneAndBalance(
                                acc.getAccountNumber(),
                                acc.getBalance(),
                                PATH_PHONE_WITH_DELAY))
                .toList();

        List<Future<ClientBalanceAndPhoneInfo>> futures = phoneNumFetcher.invokeAll(
                tasks,
                timeout,
                TimeUnit.MILLISECONDS
        );

        List<ClientBalanceAndPhoneInfo> results = new ArrayList<>();
        for (int i = 0; i < futures.size(); i++) {
            Future<ClientBalanceAndPhoneInfo> future = futures.get(i);
            String accountNumber = accounts.get(i).getAccountNumber();
            checkDoneFuture(future, accountNumber, results);
        }

        evaluateExecutionTime(startTime);
        return results;
    }

    public List<ClientBalanceAndPhoneInfo> getClientBalanceAndPhoneInfoSync() {
        long startTime = System.nanoTime();
        log.info("Начинаем асинхронную агрегацию данных по всем счетам");

        List<BankAccount> accounts = bankAccountService.findAll();
        log.info("Найдено счетов для обработки {}", accounts.size());


        List<ClientBalanceAndPhoneInfo> result = new ArrayList<>();
        for (var acc : accounts) {
            var clientInfo = aggregatePhoneAndBalance(
                    acc.getAccountNumber(),
                    acc.getBalance(),
                    PATH_PHONE_WITHOUT_DELAY);
            logResult(clientInfo);
            result.add(clientInfo);
        }

        evaluateExecutionTime(startTime);
        return result;
    }

    private void logResult(ClientBalanceAndPhoneInfo clientInfo) {
        log.info("Получен результат для счета= {} ,баланс={}  телефон ={}", clientInfo.accountNumber(),
                clientInfo.balance(), clientInfo.phoneNumber());
    }

    private ClientBalanceAndPhoneInfo aggregatePhoneAndBalance(String accountNumber, double balance, String pathEndpoint) {
        String phone = phoneWebClient.getPhoneNumber(accountNumber, pathEndpoint);
        return new ClientBalanceAndPhoneInfo(accountNumber, balance, phone);
    }

    private void checkDoneFuture(Future<ClientBalanceAndPhoneInfo> future,
                                 String accountNumber,
                                 List<ClientBalanceAndPhoneInfo> results) {
        if (future.isCancelled()) {
            log.warn("Задача для счета {} была отменена", accountNumber);
        } else if (future.isDone()) {
            try {
                ClientBalanceAndPhoneInfo info = future.get();
                results.add(info);
                log.info("Успешно получены данные для счета {}", accountNumber);
            } catch (Exception e) {
                log.error("Ошибка при получении данных со счета {}", accountNumber);
            }
        } else {
            log.error("Не знаем что произошло с задачей и почему она не isCancelled и не isDone");
        }
    }
}
