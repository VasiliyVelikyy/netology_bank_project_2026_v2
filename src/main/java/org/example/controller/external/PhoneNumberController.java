package org.example.controller.external;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.external.PhoneNumberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class PhoneNumberController {
    public final PhoneNumberService phoneNumberService;

    @GetMapping("/account/{accountNumber}/phone")
    public ResponseEntity<String> getPhoneNumber(@PathVariable String accountNumber) throws InterruptedException {
        String phone = phoneNumberService.findPhoneNumberByAccountNumber(accountNumber);

        if (phone == null || phone.isBlank()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(phone);
    }

    @GetMapping("/account/{accountNumber}/phone/delay")
    public ResponseEntity<String> getPhoneNumberDelay(@PathVariable String accountNumber) throws InterruptedException {
        String phone = phoneNumberService.findPhoneNumberByAccountNumber(accountNumber);

        if (phone != null) {
            long delay = 1L + (long) (Math.random() * 1000);//0 до 999
            log.info("PhoneNumberController accNum={}, delay={}", accountNumber, delay);
            Thread.sleep(delay);

            return ResponseEntity.ok(phone);
        }
        return ResponseEntity.notFound().build();
    }
}
