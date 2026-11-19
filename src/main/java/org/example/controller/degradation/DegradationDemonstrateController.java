package org.example.controller.degradation;


import lombok.RequiredArgsConstructor;
import org.example.domain.BankAccount;
import org.example.service.degradation.DegradationDemonstrateService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/degradation")
@RequiredArgsConstructor
public class DegradationDemonstrateController {
    private final DegradationDemonstrateService degradationDemonstrateService;

    @GetMapping("/cpu")
    public String burnCpu() {
        return degradationDemonstrateService.burnCpu();
    }

    @GetMapping("/slow-io")
    public String slowIo() {
        return degradationDemonstrateService.slowIo();
    }

    @GetMapping("/fast-io")
    public String fastIo() {
        return degradationDemonstrateService.fastIo();
    }

    @PostMapping("/slow-alg")
    public BankAccount slowAlg(@RequestBody Map<String, String> request) {
        return degradationDemonstrateService.slowAlg(request);
    }

    @PostMapping("/fast-alg")
    public BankAccount fastAlg(@RequestBody Map<String, String> request) {
        return degradationDemonstrateService.fastAlg(request);
    }
}
