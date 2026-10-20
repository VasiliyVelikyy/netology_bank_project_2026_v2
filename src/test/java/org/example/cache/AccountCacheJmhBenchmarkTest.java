package org.example.cache;

import org.example.domain.BankAccount;
import org.example.repo.BankAccountRepository;
import org.mockito.Mockito;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.example.util.LoggingUtils.loggingThreadError;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, warmups = 1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 2)
@Threads(8)
public class AccountCacheJmhBenchmarkTest {

    private AccountCacheService accountCacheService;
    private String[] hitKeys;
    private int hitKeysLength;

    @State(Scope.Thread)
    public static class ThreadState {
        private int hitIndex = 0;
        private final AtomicLong missCounter = new AtomicLong();

        public String nextHitKey(String[] keys, int len) {
            String key = keys[hitIndex];
            hitIndex = (hitIndex + 1) % len;
            return key;

        }

        public String nextUniqMissKey() {
            return "MISS " + Thread.currentThread().getName() + "-" + missCounter.incrementAndGet();
        }
    }

    @Setup(Level.Trial)
    public void setup() {
        BankAccountRepository mockRepository = Mockito.mock(BankAccountRepository.class);

        when(mockRepository.findByAccountNumber(anyString()))
                .thenAnswer(invocation -> {
                    String accNum = invocation.getArgument(0);
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        loggingThreadError(e);
                    }
                    return new BankAccount(accNum, 1000);
                });

        this.accountCacheService = new AccountCacheService(mockRepository);

        int keyCount = 1000;
        this.hitKeys = new String[keyCount];
        this.hitKeysLength = hitKeys.length;

        for (int i = 0; i < keyCount; i++) {
            String key = "HIT-ACC-" + i;
            this.hitKeys[i] = key;
            accountCacheService.getConcurrentOptimize(key);
            accountCacheService.getSynchronized(key);

        }
    }


    @Benchmark
    public void benchmarkConcurrentMapHit(ThreadState state, Blackhole blackhole) {
        String key = state.nextHitKey(this.hitKeys, this.hitKeysLength);
        BankAccount account = accountCacheService.getConcurrentOptimize(key);
        blackhole.consume(account);
    }

    @Benchmark
    public void benchmarkSynctMapHit(ThreadState state, Blackhole blackhole) {
        String key = state.nextHitKey(this.hitKeys, this.hitKeysLength);
        BankAccount account = accountCacheService.getSynchronized(key);
        blackhole.consume(account);
    }


    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(AccountCacheJmhBenchmarkTest.class.getSimpleName())
                //.forks(1)
                .build();
        new Runner(opt).run();

    }
}