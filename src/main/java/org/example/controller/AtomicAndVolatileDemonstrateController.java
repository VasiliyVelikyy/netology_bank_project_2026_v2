package org.example.controller;


import lombok.RequiredArgsConstructor;
import org.example.service.atomic_ex.ProcessAtomicExampleService;
import org.example.service.atomic_ex.SpeedTestSyncAndAtomicService;
import org.example.service.volatile_ex.ProcessVolatileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AtomicAndVolatileDemonstrateController {
    private final ProcessVolatileService processVolatileService;
    private final ProcessAtomicExampleService processAtomicExampleService;
    private final SpeedTestSyncAndAtomicService speedTestSyncAndAtomicService;

    @GetMapping("/volatile-race-condition")
    public String processVolatileRaceCondition() throws InterruptedException {
      return processVolatileService.processVolatileRaceCondition();
    }

    @GetMapping("/write-and-read-volatile")
    public String processWriteAndRead() throws InterruptedException {
        return processVolatileService.processWriteAndRead();
    }

    @GetMapping("/atomic-examples")
    public String processAtomic() throws InterruptedException {
        return processAtomicExampleService.processAtomic();
    }

    @GetMapping("/test-speed-sync-atomic")
    public String processSpeedTestSyncAndAtomic() {
        return speedTestSyncAndAtomicService.processSpeedTestSyncAndAtomic();
    }

    @GetMapping("/atomic-reference")
    public String processAtomicReference() throws InterruptedException {
        return processAtomicExampleService.processAtomicReference();
    }
}
