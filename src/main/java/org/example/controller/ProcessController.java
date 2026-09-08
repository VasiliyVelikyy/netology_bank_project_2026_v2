package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.service.ProcessService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProcessController {
    private final ProcessService processService;

    @GetMapping("/hello")
    public String hello(){
        return "hello";
    }

    @GetMapping("/test/process")
    public String process(){
        return processService.runThread();
    }
}
