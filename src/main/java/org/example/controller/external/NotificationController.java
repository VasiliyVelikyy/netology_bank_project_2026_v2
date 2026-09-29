package org.example.controller.external;


import lombok.RequiredArgsConstructor;
import org.example.service.executor_services.NotificationLowBalanceCheckService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationLowBalanceCheckService notificationService;

    @PostMapping("/check-low-balance")
    public ResponseEntity<String> triggerLowBalanceCheck() {
        notificationService.triggerLowBalanceCheck();
        return ResponseEntity.accepted()
                .body("Проверка низкого баланса запущена в фоново режиме. Следите за логами");
    }

}
