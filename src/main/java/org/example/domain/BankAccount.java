package org.example.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;

@Entity
@Table(name = "bank_accounts")
@Getter
@Setter
public class BankAccount {
    @Id
    @Column(name = "account_number", length = 20)
    private String accountNumber;

    @Column(name = "balance", nullable = false)
    private double balance;

    public BankAccount() {
    }

    public BankAccount(String accountNumber, double balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }


}
