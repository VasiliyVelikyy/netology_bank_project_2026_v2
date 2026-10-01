package org.example.repo;

import org.example.domain.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankAccountRepository extends JpaRepository<BankAccount, String> {
    BankAccount getAllByAccountNumber(String accountNumber);

    List<BankAccount> findByBalanceGreaterThan(double threshold);

    List<BankAccount> findByBalanceLessThan(double threshold);
}
