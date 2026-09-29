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

    public String getPhoneNumber(String accountNum) {
        String url = HOST + "/account/" + accountNum + "/phone";

        try {
            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "UKNOWN";
        }
    }
}
