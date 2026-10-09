package org.example.service.executor_services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.BankAccount;
import org.example.dto.ClientBalanceAndPhoneInfo;
import org.example.integration.PhoneWebClient;
import org.example.service.bank_account.BankAccountService;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.example.util.Constants.GEN_PREFIX;
import static org.example.util.Constants.PATH_PHONE_WITH_DELAY;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompletableFutureAggregationService {
    private final BankAccountService bankAccountService;
    private final PhoneWebClient phoneWebClient;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public void demonstrateSimpleCompletableFutureWithBlockClient() {
        log.info("Демонстрация простой задачи completable");

        BankAccount account = bankAccountService.findAll().get(0);

        CompletableFuture<ClientBalanceAndPhoneInfo> futureInfo =
                CompletableFuture.supplyAsync(() -> {
                    String phone = phoneWebClient.getPhoneNumberSync(account.getAccountNumber(),
                            PATH_PHONE_WITH_DELAY);
                    return new ClientBalanceAndPhoneInfo(account.getAccountNumber(),
                            account.getBalance(),
                            phone);
                }, executorService);

        futureInfo.thenAccept(info -> {
            log.info("Результат получен в простой задаче completable {}", info);
        }).exceptionally(t -> {
            log.error("Ошибка в простой задаче ", t);
            return null;
        });

        log.info("Основной поток не ждет завершение он идет дальше");
    }

    public void demonstrateSimpleCompletableFutureWithAsyncClient() {
        log.info("Демонстрация действительно асинхронной задачи completable");
        String targetAccountNumber = GEN_PREFIX + 9;

        CompletableFuture<BankAccount> accountFuture = CompletableFuture.supplyAsync(() ->
                        bankAccountService.getAccountOpt(targetAccountNumber).orElseThrow(),
                executorService);


        CompletableFuture<ClientBalanceAndPhoneInfo> chainedFuture = accountFuture
                .thenCompose(account -> phoneWebClient.getPhoneNumberAsync(account.getAccountNumber(), PATH_PHONE_WITH_DELAY)
                        .thenApply(phone -> new ClientBalanceAndPhoneInfo(account.getAccountNumber(),
                                account.getBalance(),
                                phone))
                )
                .exceptionally(t -> {
                    log.error("Не удалось обработать аккаунт {} {}", targetAccountNumber, t.getMessage());
                    return new ClientBalanceAndPhoneInfo(targetAccountNumber, 0.0, "ERROR");
                });

        ClientBalanceAndPhoneInfo result = chainedFuture.join();

        log.info("Результат цепочки {}", result);
    }

    public void demonstrateCombine() {
        log.info("Демонстрация простой задачи combinedFuture");

        BankAccount account = bankAccountService.findAll().get(0);

        CompletableFuture<Double> balanceFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.info("Запрос в сторонний сервис за балансом");
            return account.getBalance();
        }, executorService);

        CompletableFuture<String> phoneFuture = phoneWebClient.getPhoneNumberAsync(account.getAccountNumber(),
                PATH_PHONE_WITH_DELAY);

        CompletableFuture<ClientBalanceAndPhoneInfo> combinedFuture
                = balanceFuture.thenCombine(phoneFuture, (balance, phone) -> {
            log.info("Оба потока завершены, объединяем данные для {}", account.getAccountNumber());
            return new ClientBalanceAndPhoneInfo(account.getAccountNumber(), balance, phone);
        });

        log.info("Результат комбинирования {}", combinedFuture.join());
    }
}
