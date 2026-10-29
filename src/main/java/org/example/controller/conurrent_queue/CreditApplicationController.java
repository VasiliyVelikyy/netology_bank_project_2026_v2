package org.example.controller.conurrent_queue;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.CreditApplication;
import org.example.service.conurrent_queue.CreditProcessingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CreditApplicationController {
    private final CreditProcessingService linkedCreditProcessingService;
    private final CreditProcessingService arrayCreditProcessingService;

    @PostMapping("/credit/array-queue")
    public String submitInArrayQueue(@RequestBody List<CreditApplication> applications) {
        return arrayCreditProcessingService.submit(applications);
    }

    @PostMapping("/credit/linked-queue")
    public String submitInLinkedQueue(@RequestBody List<CreditApplication> applications) {
        return linkedCreditProcessingService.submit(applications);
    }

    @GetMapping("/stat/queue")
    public String getStatus() {
        return "Статистика по очередям " + linkedCreditProcessingService.getState() + "\n"
                + arrayCreditProcessingService.getState();
    }

}
