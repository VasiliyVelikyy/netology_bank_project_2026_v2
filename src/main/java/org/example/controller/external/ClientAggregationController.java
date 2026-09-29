package org.example.controller.external;


import lombok.RequiredArgsConstructor;
import org.example.dto.ClientBalanceAndPhoneInfo;
import org.example.service.executor_services.ClientAggregationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ClientAggregationController {
    private final ClientAggregationService clientAggregationService;

    @GetMapping("/clients-balance-and-email-async")
    public ResponseEntity<List<ClientBalanceAndPhoneInfo>> getClientBalanceAndPhoneInfoAsync(){
        List<ClientBalanceAndPhoneInfo> result =  clientAggregationService.getClientBalanceAndPhoneInfoAsync();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/clients-balance-and-email-sync")
    public ResponseEntity<List<ClientBalanceAndPhoneInfo>> getClientBalanceAndPhoneInfoSync(){
        List<ClientBalanceAndPhoneInfo> result =  clientAggregationService.getClientBalanceAndPhoneInfoSync();
        return ResponseEntity.ok(result);
    }
}
