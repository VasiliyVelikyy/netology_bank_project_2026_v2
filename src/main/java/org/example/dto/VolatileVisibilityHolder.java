package org.example.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VolatileVisibilityHolder {

    private boolean isReady = false;

    private volatile String message = "Ожидание данных";
}
