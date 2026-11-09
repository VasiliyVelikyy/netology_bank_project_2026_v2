package org.example.service.memory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SurvivorWalkService {
    private final FillEdenService fillEdenService;
    private final List<byte[]> survivorHolder = new ArrayList<>();

    public void demonstrateSurvivor() {

        for (int i = 0; i < 100; i++) {
            survivorHolder.add(new byte[100 * 1024]);

        }
        log.info("Создано {} долгоживущих объектов", survivorHolder.size());

        fillEdenService.demonstrateFillEden();
    }


}
