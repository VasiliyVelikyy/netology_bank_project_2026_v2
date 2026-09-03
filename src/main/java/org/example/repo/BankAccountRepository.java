package org.example.repo;

import org.example.domain.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount, String> {
    BankAccount getAllByAccountNumber(String accountNumber);
}
