package org.example.service.threadservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessService {
    private final TransferWaitingStateService transferWaitingStateService;
    private final TransferDaemonService transferDaemonService;
    private final TransferRunnableStateService transferRunnableStateService;
    private final TransferSleepStateService transferSleepStateService;
    private final TransferBlockedStateService transferWithBlock;

    public String startDemon() {
        return transferDaemonService.startDemon();
    }

    public String startRunnable() {
        return transferRunnableStateService.startRunnable();
    }

    public String startSleep() {
        return transferSleepStateService.startSleep();
    }

    public String startWaiting() {
        return transferWaitingStateService.startWaiting();
    }

    public String startPark() {
        return transferWaitingStateService.startPark();
    }

    public String startBlocked() {
        return transferWithBlock.startBlocked();
    }
}
