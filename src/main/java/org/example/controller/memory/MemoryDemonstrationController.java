package org.example.controller.memory;

import lombok.RequiredArgsConstructor;
import org.example.service.memory.FillEdenService;
import org.example.service.memory.MemoryLeakService;
import org.example.service.memory.SurvivorWalkService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MemoryDemonstrationController {
    private final FillEdenService fillEdenService;
    private final SurvivorWalkService survivorWalkService;
    private final MemoryLeakService memoryLeakService;

    @GetMapping("/fill-eden")
    public void demonstrateFillEden(){
        fillEdenService.demonstrateFillEden();
    }

    @GetMapping("/fill-survivor")
    public void demonstrateSurvivor(){
        survivorWalkService.demonstrateSurvivor();
    }

    @GetMapping("/proceess-leak")
    public void demonstrateMemoryLeak(){
        memoryLeakService.demonstrateMemoryLeak();
    }

}
