package utils;

import java.util.concurrent.atomic.AtomicInteger;

public class Counter {
    private final AtomicInteger totalRequestCount = new AtomicInteger(0);

    public synchronized void increment(int count) {
        totalRequestCount.addAndGet(count);
    }

    public synchronized int getCount() {
        return totalRequestCount.get();
    }

    public synchronized void decrement(int count) {
        totalRequestCount.addAndGet(-count);
        notifyAll();
    }
}
