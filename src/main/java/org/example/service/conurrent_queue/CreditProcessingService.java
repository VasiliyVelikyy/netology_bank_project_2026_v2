package org.example.service.conurrent_queue;

import org.example.dto.CreditApplication;

import java.util.List;

public interface CreditProcessingService {
    String submit(List<CreditApplication> applications);

    CreditApplication take();

    String getState();



}
