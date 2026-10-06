package org.example.controller.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.executor_services.BalanceMonitorScheduler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class SchedulerInvokeController {
    private final BalanceMonitorScheduler balanceMonitorScheduler;

    @GetMapping("/schedule-one-task-invoke")
    public void scheduleOneTimeClick() {
        balanceMonitorScheduler.scheduleOneTimeClick();
    }

    @GetMapping("/schedule-demonstrate-callable")
    public void demonstrateScheduledCallable() {
        balanceMonitorScheduler.demonstrateScheduledCallable();
    }
}
