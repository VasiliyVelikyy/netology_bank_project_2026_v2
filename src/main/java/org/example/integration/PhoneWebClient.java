package org.example.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.CompletableFuture;

import static org.example.util.Constants.HOST;

@Slf4j
@Component
@RequiredArgsConstructor
public class PhoneWebClient {
    private final WebClient webClient;

    public String getPhoneNumberSync(String accountNumber, String endpointPath) {

        String url = HOST + "/account/" + accountNumber + endpointPath;

        try {
            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // блокирующий вызов для совместимости с синхронным кодом
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    public CompletableFuture<String> getPhoneNumberAsync(String accountNumber, String endpointPath) {
        String url = HOST + "/account/" + accountNumber + endpointPath;
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .toFuture();
    }

    public String getPhoneNumberSyncStrict(String accountNumber, String endpointPath) {
        String url = HOST + "/account/" + accountNumber + endpointPath;

        try {
            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

        } catch (Exception e) {
            if (Thread.currentThread().isInterrupted()) {
                log.info("Запрос для {} прерван из-за отмены Scope", accountNumber);
                throw new RuntimeException("Task interrupted by Scope", e);
            }

            log.warn("Сетевая ошибка для аккаунта {}: {}", accountNumber, e.getMessage());
            throw new RuntimeException("Failed to fetch phone", e);
        }
    }
}
