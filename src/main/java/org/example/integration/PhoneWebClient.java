package org.example.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import static org.example.util.Constants.HOST;

@Slf4j
@Component
@RequiredArgsConstructor
public class PhoneWebClient {
    private final WebClient webClient;

//    public String getPhoneNumber(String accountNum, String pathEndpoint) {
//        String url = HOST + "/account/" + accountNum + pathEndpoint;
//
//        try {
//            return webClient.get()
//                    .uri(url)
//                    .retrieve()
//                    .bodyToMono(String.class)
//                    .block();
//        } catch (Exception e) {
//            if (e.getCause() instanceof InterruptedException ||
//                    Thread.currentThread().isInterrupted()) {
//                log.warn("Запрос для {} был прерван из-за таймаута ", accountNum);
//            } else {
//                log.error("Реальная ошибка сети для {}: {}", accountNum, e.getMessage());
//            }
//            return "UNKNOWN";
//
//        }
//    }

    public String getPhoneNumber(String accountNumber,  String endpointPath) {

        String url = HOST + "/account/" + accountNumber + endpointPath;

        try {
            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // блокирующий вызов для совместимости с синхронным кодом
        } catch (Exception e) {
            // Логирование ошибки (опционально)
             //log.warn("Ошибка при получении номера для аккаунта {}: {}", accountNumber, e.getMessage());
            return "UNKNOWN";
        }
    }
}
