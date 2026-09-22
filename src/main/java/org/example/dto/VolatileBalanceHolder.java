package org.example.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class VolatileBalanceHolder {
    private volatile double balance;
}
