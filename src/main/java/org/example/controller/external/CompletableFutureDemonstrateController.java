package org.example.controller.external;

import lombok.RequiredArgsConstructor;
import org.example.service.executor_services.CompletableFutureAggregationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CompletableFutureDemonstrateController {
    private final CompletableFutureAggregationService completableFutureAggregationService;

    @GetMapping("/demonstrate-comp-future-task-client")
    private void demonstrateSimpleCompletableFutureTask() {
        completableFutureAggregationService.demonstrateSimpleCompletableFutureWithBlockClient();
    }

    @GetMapping("/demonstrate-comp-future-task-async-client")
    private void demonstrateSimpleCompletableFutureWithAsyncClient() {
        completableFutureAggregationService.demonstrateSimpleCompletableFutureWithAsyncClient();
    }

    @GetMapping("/demonstrate-comp-future-combine")
    private void demonstrateCombine() {
        completableFutureAggregationService.demonstrateCombine();
    }
}
