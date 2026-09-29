package org.example.dto;

public record ClientBalanceAndPhoneInfo(
        String accountNumber,
        double balance,
        String phoneNumber
) {
}
