package org.example.service.volatile_ex;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.BankAccount;
import org.example.dto.VolatileBalanceHolder;
import org.example.dto.VolatileVisibilityHolder;
import org.example.service.bank_account.BankAccountService;
import org.springframework.stereotype.Service;

import static org.example.util.Constants.ACC_1;
import static org.example.util.LoggingUtils.loggingThreadError;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessVolatileService {
    private final BankAccountService bankAccountService;


    public String processVolatileRaceCondition() throws InterruptedException {
        BankAccount account = bankAccountService.getAccount(ACC_1);
        VolatileBalanceHolder holder = new VolatileBalanceHolder(account.getBalance());

        int steps = 10000;
        double amountToAdd = 1.0;

        log.info("Начальный баланс в обертке {}", holder.getBalance());
        Runnable write = () -> {
            for (int i = 0; i < steps; i++) {
                double currentBalance = holder.getBalance();

                double newBalance = currentBalance + amountToAdd;

                holder.setBalance(newBalance);

            }
        };

        Thread t1 = new Thread(write, "Writer 1 (пополнение)");
        Thread t2 = new Thread(write, "Writer 2 (пополнение)");

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        double expectedBalance = (steps * 2.0 * amountToAdd);
        double actualBalance = holder.getBalance();
        double lostAmount = expectedBalance - actualBalance;

        log.info("Ожидаемый баланс {}", expectedBalance);
        log.info("Фактический баланс {}", actualBalance);
        log.info("Потеря на сумму {}", lostAmount);


//        account.setBalance(actualBalance);
//        bankAccountService.save(account);
        return "ok";
    }

    public String processWriteAndRead() throws InterruptedException {
        VolatileVisibilityHolder holder = new VolatileVisibilityHolder();

        Thread reader = new Thread(() -> {
            log.info("Reader начинает ожидать обновление данных");
            int checkCount = 0;

            while (!holder.isReady()) {
                checkCount++;

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            log.info("Reader Данные получены Сообщение {}, проверок цикла {}"
                    , holder.getMessage(), checkCount);
        }, "Reader");

        Thread writer = new Thread(() -> {
            log.info("Writer  начинаю имитацию загрузки данных");

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                loggingThreadError(e);
            }

            log.info("Writer операция завершена. Записываю результат в volatile переменную");
            holder.setMessage("Данные по транзакции #999 загружены. Баланс 10000000");
            holder.setReady(true);

            log.info("Writer флаг is ready установлен");

        }, "Writer");

        reader.start();
        Thread.sleep(100);

        writer.start();

        reader.join();
        writer.join();

        return "ok";

    }

}
