package com.group04;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class SeatReservationDemo {

    private static final int INITIAL_SEATS = 50;
    private static final int ATTEMPTS = 100;

    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicInteger successful = new AtomicInteger();
    private final AtomicInteger failed = new AtomicInteger();

    // Chỉ đọc/ghi bên trong lock
    private int remainingSeats = INITIAL_SEATS;

    public boolean reserveOneSeat() {
        lock.lock();
        try {
            if (remainingSeats == 0) {
                failed.incrementAndGet();
                return false;
            }

            remainingSeats--;
            successful.incrementAndGet();
            return true;
        } finally {
            lock.unlock();
        }
    }

    public void runDemo() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(ATTEMPTS);
        CountDownLatch ready = new CountDownLatch(ATTEMPTS);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(ATTEMPTS);

        for (int i = 0; i < ATTEMPTS; i++) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    reserveOneSeat();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finished.countDown();
                }
            });
        }

        try {
            ready.await();
            start.countDown();

            if (!finished.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Cac tac vu dat ghe khong hoan tat kip");
            }
        } finally {
            executor.shutdownNow();
        }

        System.out.println("Dat thanh cong: " + successful.get());
        System.out.println("That bai: " + failed.get());
        System.out.println("Ghe con lai: " + remainingSeats);
    }

    public int getSuccessfulCount() {
        return successful.get();
    }

    public int getFailedCount() {
        return failed.get();
    }

    public int getRemainingSeats() {
        lock.lock();
        try {
            return remainingSeats;
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new SeatReservationDemo().runDemo();
    }
}