package org.example.data;

import lombok.RequiredArgsConstructor;
import org.example.domain.BankAccount;
import org.example.service.bank_account.BankAccountService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class TransferGeneratorService {
    private final BankAccountService bankAccountService;


    private List<String> getAllAccountNumber() {
        return bankAccountService.findAll().stream()
                .map(BankAccount::getAccountNumber)
                .toList();
    }

    public List<TransferOperation> generateTransfer(int count) {
        List<String> accounts = getAllAccountNumber();
        Random rand = new Random();

        return IntStream.range(0, count)
                        .mapToObj(i -> {
                            String from = accounts.get(rand.nextInt(accounts.size()));
                            String to;
                            do {
                                to = accounts.get(rand.nextInt(accounts.size()));
                            } while (from.equals(to));

                            double rawAmount = 1 + rand.nextDouble() + 9; //1 -10
                            double amount = Math.round(rawAmount * 100.0) / 100.0;
                            return new TransferOperation(from, to, amount);
                        }).toList();

    }


    public record TransferOperation(String from, String to, double amount) { }
}
