package com.statesmartlife.e2e;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Java Operational Load Test Executor for Mo Odisha Super App Platform.
 * Baseline Commit: a14e9a0
 */
public class LoadTestRunner {

    private static final String BASE_URL = "http://localhost:8081/api/v1";

    public static void main(String[] args) throws Exception {
        System.out.println("=== MO ODISHA SUPER APP OPERATIONAL LOAD TEST EXECUTION ===");
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        // 1. Authenticate & Obtain Admin JWT Token
        String loginJson = "{\"email\":\"admin@smartlife.odisha.gov.in\",\"password\":\"AdminPassword123!\"}";
        HttpRequest loginReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(loginJson))
                .build();

        HttpResponse<String> loginRes = client.send(loginReq, HttpResponse.BodyHandlers.ofString());
        if (loginRes.statusCode() != 200) {
            System.err.println("FAILED TO AUTHENTICATE IN LOAD TEST SETUP: Status " + loginRes.statusCode());
            return;
        }

        String body = loginRes.body();
        int tokenIdx = body.indexOf("\"accessToken\":\"");
        String token = "";
        if (tokenIdx != -1) {
            int start = tokenIdx + 15;
            int end = body.indexOf("\"", start);
            token = body.substring(start, end);
        }

        System.out.println("[SETUP] Authenticated Admin JWT Token obtained successfully.");

        // 2. Configure Concurrency & Execution Profile
        int concurrentVUs = 50;
        int durationSeconds = 15;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentVUs);
        List<Long> latenciesMs = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger totalRequests = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger serverErrorCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (durationSeconds * 1000L);

        final String bearerToken = token;
        System.out.println("[EXECUTION] Launching " + concurrentVUs + " Virtual Users for " + durationSeconds + "s...");

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < concurrentVUs; i++) {
            futures.add(executor.submit(() -> {
                while (System.currentTimeMillis() < endTime) {
                    try {
                        // Scenario 1: System Info
                        long reqStart = System.currentTimeMillis();
                        HttpRequest healthReq = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/system/info")).GET().build();
                        HttpResponse<Void> r1 = client.send(healthReq, HttpResponse.BodyHandlers.discarding());
                        long reqEnd = System.currentTimeMillis();
                        latenciesMs.add(reqEnd - reqStart);
                        totalRequests.incrementAndGet();
                        if (r1.statusCode() == 200) successCount.incrementAndGet();
                        if (r1.statusCode() >= 500) serverErrorCount.incrementAndGet();

                        // Scenario 2: System Info
                        reqStart = System.currentTimeMillis();
                        HttpRequest infoReq = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/system/info")).GET().build();
                        HttpResponse<Void> r2 = client.send(infoReq, HttpResponse.BodyHandlers.discarding());
                        reqEnd = System.currentTimeMillis();
                        latenciesMs.add(reqEnd - reqStart);
                        totalRequests.incrementAndGet();
                        if (r2.statusCode() == 200) successCount.incrementAndGet();
                        if (r2.statusCode() >= 500) serverErrorCount.incrementAndGet();

                        // Scenario 3: Governance Overview (Authenticated)
                        reqStart = System.currentTimeMillis();
                        HttpRequest govReq = HttpRequest.newBuilder()
                                .uri(URI.create(BASE_URL + "/governance/analytics/overview"))
                                .header("Authorization", "Bearer " + bearerToken)
                                .GET().build();
                        HttpResponse<Void> r3 = client.send(govReq, HttpResponse.BodyHandlers.discarding());
                        reqEnd = System.currentTimeMillis();
                        latenciesMs.add(reqEnd - reqStart);
                        totalRequests.incrementAndGet();
                        if (r3.statusCode() >= 200 && r3.statusCode() < 300) successCount.incrementAndGet();
                        if (r3.statusCode() >= 500) serverErrorCount.incrementAndGet();

                    } catch (Exception e) {
                        serverErrorCount.incrementAndGet();
                    }
                }
            }));
        }

        for (Future<?> f : futures) {
            f.get();
        }

        executor.shutdown();
        long actualDurationMs = System.currentTimeMillis() - startTime;
        double actualDurationSec = actualDurationMs / 1000.0;

        // 3. Compute Measured Empirical Results
        List<Long> sortedLatencies = new ArrayList<>(latenciesMs);
        Collections.sort(sortedLatencies);

        int totalReqs = totalRequests.get();
        double rps = totalReqs / actualDurationSec;
        long p50 = sortedLatencies.isEmpty() ? 0 : sortedLatencies.get((int) (sortedLatencies.size() * 0.50));
        long p95 = sortedLatencies.isEmpty() ? 0 : sortedLatencies.get((int) (sortedLatencies.size() * 0.95));
        long p99 = sortedLatencies.isEmpty() ? 0 : sortedLatencies.get((int) (sortedLatencies.size() * 0.99));
        double errorRatePercent = totalReqs == 0 ? 0.0 : (serverErrorCount.get() * 100.0 / totalReqs);

        System.out.println("\n=== EMPIRICAL LOAD TEST RESULTS ===");
        System.out.println("Concurrent VUs:        " + concurrentVUs);
        System.out.println("Execution Duration:    " + String.format("%.2f", actualDurationSec) + " s");
        System.out.println("Total Requests Sent:   " + totalReqs);
        System.out.println("Throughput (RPS):      " + String.format("%.2f", rps) + " req/s");
        System.out.println("Latency p50:           " + p50 + " ms");
        System.out.println("Latency p95:           " + p95 + " ms");
        System.out.println("Latency p99:           " + p99 + " ms");
        System.out.println("Operational Errors:    " + serverErrorCount.get() + " (" + String.format("%.4f", errorRatePercent) + "%)");
        System.out.println("Latency SLA (p95<=200): " + (p95 <= 200 ? "PASS" : "FAIL"));
        System.out.println("Error SLA (<0.1%):     " + (errorRatePercent < 0.1 ? "PASS" : "FAIL"));
        System.out.println("=====================================\n");
    }
}
