package org.example.controller.virtual_treads;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.ClientBalanceAndPhoneInfo;
import org.example.service.virtual_thread.VirtualThreadClientAggregationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VirtualThreadClientAggregationController {
    private final VirtualThreadClientAggregationService aggregationService;

    @GetMapping("/full-clients-virtual-threads")
    public ResponseEntity<List<ClientBalanceAndPhoneInfo>> getFullClientInfoByVirtualThreads() {
        var result = aggregationService.getFullClientInfoByVirtualThreads();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/benchmark-ex-vt-100")
    public String benchMark100k() {
        return aggregationService.benchMark100k();
    }

    @GetMapping("/scope-test")
    public ResponseEntity<List<ClientBalanceAndPhoneInfo>> scopeTest() throws Exception {
        var result = aggregationService.getFullClientInfoStructured();
        return ResponseEntity.ok(result);
    }
}
