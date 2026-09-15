package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.service.sync_problem.SynchronizationProblemThreadService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SynchronizationProblemController {
    private final SynchronizationProblemThreadService transferService;


    @GetMapping("/race-condition")
    public String processRaceCondition(){
        return transferService.processRaceCondition();
    }


    @GetMapping("/transfer-deadlock")
    public String processDeadLock(){
        return transferService.processDeadLock();
    }

    @GetMapping("/transfer-livelock")
    public String processLiveLock(){
        return transferService.processLiveLock();
    }

    @GetMapping("/transfer-livelock-max-attempt")
    public String processLivelockMaxAttempt() throws InterruptedException {
        return transferService.transferLivelockWithMaxAttempt();
    }

    @GetMapping("/transfer-starvation")
    public String processStarvation() throws InterruptedException {
        return transferService.transferStarvation();
    }

}
