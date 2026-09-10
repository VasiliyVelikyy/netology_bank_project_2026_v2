package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.service.threadservice.ProcessService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TransferController {
    private final ProcessService processService;

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    @GetMapping("/start-demon")
    public String startDemon() {
        return processService.startDemon();
    }

    @GetMapping("/process-runnable")
    public String startRunnable() {
        return processService.startRunnable();
    }

    @GetMapping("/process-sleep")
    public String startSleep() {
        return processService.startSleep();
    }

    @GetMapping("/process-waiting")
    public String startWaiting() {
        return processService.startWaiting();
    }

    @GetMapping("/process-blocked")
    public String startBlocked() {
        return processService.startBlocked();
    }

    @GetMapping("/process-park")
    public String startPark() {
        return processService.startPark();
    }

}


