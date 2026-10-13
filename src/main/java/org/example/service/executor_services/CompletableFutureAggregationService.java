package org.example.service.executor_services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.BankAccount;
import org.example.dto.ClientBalanceAndPhoneInfo;
import org.example.integration.PhoneWebClient;
import org.example.service.bank_account.BankAccountService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;

import static org.example.util.Constants.GEN_PREFIX;
import static org.example.util.Constants.PATH_PHONE_WITH_DELAY;
import static org.example.util.TimeUtil.evaluateExecutionTime;
import static org.example.util.TimeUtil.getRandomDelay;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompletableFutureAggregationService {
    private final BankAccountService bankAccountService;
    private final PhoneWebClient phoneWebClient;

    private final ExecutorService executorService = createCustomPoolExecutor();  //Executors.newFixedThreadPool(10);

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

        futureInfo.thenAccept(info -> log.info("Результат получен в простой задаче completable {}", info)).exceptionally(t -> {
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

    public List<ClientBalanceAndPhoneInfo> getFullClientInfoCompFuture() {
        long startTime = System.nanoTime();
        log.info("Агрегация данных клиента по completable future");

        List<BankAccount> accounts = bankAccountService.findAll();

        List<CompletableFuture<ClientBalanceAndPhoneInfo>> futures = accounts.stream()
                .map(acc ->
                                phoneWebClient.getPhoneNumberAsync(acc.getAccountNumber(), PATH_PHONE_WITH_DELAY)
                                        .thenApplyAsync(phone -> getNewClientInfo(acc, phone), executorService)
                                        //.orTimeout(1000, TimeUnit.MILLISECONDS)
//                                .exceptionally(t ->
//                                       getClientWithDefaultInfo(acc, t))
                                        .completeOnTimeout(
                                                new ClientBalanceAndPhoneInfo(
                                                        acc.getAccountNumber(),
                                                        acc.getBalance(),
                                                        "TIMEOUT_ERROR"),
                                                1000, TimeUnit.MILLISECONDS)
                ).toList();


        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<ClientBalanceAndPhoneInfo> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        evaluateExecutionTime(startTime);
        return results;
    }

    private ClientBalanceAndPhoneInfo getNewClientInfo(BankAccount acc, String phone) {
        return new ClientBalanceAndPhoneInfo(
                acc.getAccountNumber(),
                acc.getBalance(),
                phone);
    }

    private ClientBalanceAndPhoneInfo getClientWithDefaultInfo(BankAccount acc, Throwable t) {
        if (t instanceof TimeoutException) {
            log.warn("Таймаут для счета {}, используем fallback",
                    acc.getAccountNumber());
            return new ClientBalanceAndPhoneInfo(acc.getAccountNumber(),
                    acc.getBalance(),
                    "TIMEOUT_ERROR");
        }
        log.error("Критическая ошибка для счета {}", acc.getAccountNumber());
        throw new CompletionException("Критическая ошибка", t);
    }

    private static ThreadPoolExecutor createCustomPoolExecutor() {
        return new ThreadPoolExecutor(10, 20, 60L, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(1000),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    public void demonstrateRaceCondition() {
        log.info(" Демонстрация выбора наиболее быстрого выполнения");

        CompletableFuture<String> systemOne = CompletableFuture.supplyAsync(() -> {
            log.info("Система 1. Начало запроса");

            long delay = getRandomDelay();
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                log.info("Система 1. Поток был прерван. (Задача отменена победителем)");
                Thread.currentThread().interrupt();
            }
            return "DATA_FROM_SESTEM_1";
        });


        CompletableFuture<String> systemTwo = CompletableFuture.supplyAsync(() -> {
            log.info("Система 2. Начало запроса");

            long delay = getRandomDelay();
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                log.info("Система 2. Поток был прерван. (Задача отменена победителем)");
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            return "DATA_FROM_SESTEM_2";
        });

        CompletableFuture<Object> raceResult = CompletableFuture.anyOf(systemOne, systemTwo)
                .handle((result, ex) -> {
                    log.info("Итоговый результат {}", result);
                    if (ex != null) {
                        log.error(" Кто то упал с ошибкой {}", ex.getMessage());
                        return "FALLBACK_DATA";
                    }
                    return result;
                });

        raceResult.thenAccept(result -> {
            log.info("raceResult log ");
            boolean system1Cancel = systemOne.cancel(true);
            boolean system2Cancel = systemTwo.cancel(true);

            log.info("Диагностика. Проверка отмены системы 1 {} , (false = уже завершена/отменена", system1Cancel);
            log.info("Диагностика. Проверка отмены системы 2 {} , (false = уже завершена/отменена", system2Cancel);

            log.info("Итоговый результат {}", result);
        });

        raceResult.join();
    }

    public void demonstrateAllOf() {

        CompletableFuture<String> systemOne = CompletableFuture.supplyAsync(() -> "DATA_FROM_SYSTEM_1");

        CompletableFuture<String> systemTwo = CompletableFuture.supplyAsync(() -> "DATA_FROM_SYSTEM_2");

        CompletableFuture<Void> allOf = CompletableFuture.allOf(systemOne, systemTwo);

        allOf.thenAccept(v -> {
            String result1 = systemOne.join();
            String result2 = systemTwo.join();

            log.info("Результат системы 1: {}", result1);
            log.info("Результат системы 2: {}", result2);

            String combined = result1 + " + " + result2;
            log.info("Объединенный результат: {}", combined);
        }).exceptionally(ex -> {
            log.error("Одна из задач упала с ошибкой", ex);
            return null;
        });

        allOf.join();
    }
}
