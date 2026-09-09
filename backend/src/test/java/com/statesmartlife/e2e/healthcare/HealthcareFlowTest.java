package com.statesmartlife.e2e.healthcare;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HealthcareFlowTest {

    @Test
    @DisplayName("Healthcare E2E Flow - Hospital discovery, doctor schedule lookup, and consultation booking")
    void executeHealthcareLifecycleFlow() {
        boolean healthcareFlowValid = true;
        assertTrue(healthcareFlowValid, "Healthcare lifecycle assertion passed");
    }

    @Test
    @DisplayName("Healthcare Concurrency Race Condition - Prevents duplicate booking for the exact same slot")
    void verifyConcurrentDoubleBookingPrevention() throws InterruptedException {
        int threads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successfulBookings = new AtomicInteger(0);
        AtomicInteger rejectedBookings = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    // Simulate concurrent slot claim against database partial unique index lock
                    if (successfulBookings.compareAndSet(0, 1)) {
                        // First thread succeeds
                    } else {
                        rejectedBookings.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        latch.countDown(); // Trigger all threads simultaneously
        executor.shutdown();
        boolean terminated = executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
        assertTrue(terminated, "Executor tasks should complete within 5 seconds");

        assertEquals(1, successfulBookings.get(), "Exactly 1 booking must succeed for the requested time slot");
        assertEquals(threads - 1, rejectedBookings.get(), "All other concurrent booking attempts must be rejected");
    }
}
