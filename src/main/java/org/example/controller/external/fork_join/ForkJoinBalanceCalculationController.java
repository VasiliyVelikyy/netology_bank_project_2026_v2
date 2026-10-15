package org.example.controller.external.fork_join;


import lombok.RequiredArgsConstructor;
import org.example.service.fork_join.ForkJoinBalanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ForkJoinBalanceCalculationController {
    private final ForkJoinBalanceService forkJoinBalanceService;

    @GetMapping("/fork-join/get-balance-scoring")
    public Long getBalanceScoringRecursive(){
        return forkJoinBalanceService.getBalanceScoringRecursive();
    }

    @GetMapping("/sequential/get-balance-scoring")
    public Long getBalanceScoringSequential(){
        return forkJoinBalanceService.getBalanceScoringSequential();
    }

    @GetMapping("/fork-join/interest/all")
    public void calculateInterest(){
         forkJoinBalanceService.calculateInterestRecursive();
    }



}
