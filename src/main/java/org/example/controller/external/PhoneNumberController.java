package org.example.controller.external;


import lombok.RequiredArgsConstructor;
import org.example.service.external.PhoneNumberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
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
}
