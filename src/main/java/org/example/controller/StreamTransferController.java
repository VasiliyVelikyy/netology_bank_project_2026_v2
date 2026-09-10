package org.example.controller;


import lombok.RequiredArgsConstructor;
import org.example.service.streams.StreamTransferService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StreamTransferController {

    private final StreamTransferService streamTransferService;

    @GetMapping("/start-stream")
    public String startStream() {
        return streamTransferService.startStream();
    }

    @GetMapping("/start-parallel-stream")
    public String startParallelStream() {
        return streamTransferService.startParallelStream();
    }

//    @GetMapping("/start-parallel-stream/block")
//    public String startParallelStreamBlock() {
//        return streamTransferService.startParallelStreamBlock();
//    }
//
//    @GetMapping("/start-fork-join-parallel-stream")
//    public String startForkJoinPoolParallelStream() {
//        return streamTransferService.startForkJoinPoolParallelStream();
//    }
}
