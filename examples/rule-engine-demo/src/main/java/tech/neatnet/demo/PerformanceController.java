package tech.neatnet.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.neatnet.core.rule.engine.api.RuleEngineClient;
import tech.neatnet.core.rule.engine.domain.Category;
import tech.neatnet.core.rule.engine.domain.HitPolicy;
import tech.neatnet.core.rule.engine.domain.RuleExecutionResult;
import tech.neatnet.core.rule.engine.exceptions.RuleEngineClientProcessingException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@Slf4j
@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
public class PerformanceController {

    private final RuleEngineClient ruleEngineClient;

    /**
     * Run a performance benchmark.
     *
     * POST /api/performance/benchmark
     *
     * @param iterations Number of rule evaluations to perform (default: 1000)
     * @param warmup     Number of warmup iterations (default: 100)
     * @param parallel   Number of parallel threads (default: 1)
     */
    @PostMapping("/benchmark")
    public ResponseEntity<BenchmarkResult> runBenchmark(
            @RequestParam(defaultValue = "1000") int iterations,
            @RequestParam(defaultValue = "100") int warmup,
            @RequestParam(defaultValue = "1") int parallel) throws RuleEngineClientProcessingException {

        Map<String, Object> testInput = Map.of(
                "customerType", "premium",
                "orderTotal", 150
        );

        log.info("Starting benchmark: iterations={}, warmup={}, parallel={}", iterations, warmup, parallel);

        // Warmup phase - populate caches
        log.info("Warmup phase ({} iterations)...", warmup);
        for (int i = 0; i < warmup; i++) {
            ruleEngineClient.evaluateRules(testInput, Category.PRICING, Category.DEFAULT, HitPolicy.FIRST);
        }

        // Benchmark phase
        log.info("Benchmark phase ({} iterations)...", iterations);

        BenchmarkResult result;
        if (parallel <= 1) {
            result = runSequentialBenchmark(testInput, iterations);
        } else {
            result = runParallelBenchmark(testInput, iterations, parallel);
        }

        log.info("Benchmark complete: {}", result);
        return ResponseEntity.ok(result);
    }

    /**
     * Quick single evaluation with timing.
     *
     * POST /api/performance/single
     */
    @PostMapping("/single")
    public ResponseEntity<SingleEvaluationResult> singleEvaluation(@RequestBody Map<String, Object> input)
            throws RuleEngineClientProcessingException {

        long startNanos = System.nanoTime();
        List<RuleExecutionResult> results = ruleEngineClient.evaluateRules(
                input, Category.PRICING, Category.DEFAULT, HitPolicy.FIRST);
        long durationNanos = System.nanoTime() - startNanos;

        return ResponseEntity.ok(new SingleEvaluationResult(
                durationNanos / 1_000_000.0,
                results.size(),
                results.stream().anyMatch(RuleExecutionResult::isRuleCriteriaMet)
        ));
    }

    /**
     * Compare cold vs warm evaluation times.
     *
     * POST /api/performance/cold-vs-warm
     */
    @PostMapping("/cold-vs-warm")
    public ResponseEntity<ColdWarmComparison> coldVsWarm() throws RuleEngineClientProcessingException {
        Map<String, Object> testInput = Map.of(
                "customerType", "premium",
                "orderTotal", 150
        );

        // Measure 10 sequential evaluations
        List<Double> timings = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            long startNanos = System.nanoTime();
            ruleEngineClient.evaluateRules(testInput, Category.PRICING, Category.DEFAULT, HitPolicy.FIRST);
            long durationNanos = System.nanoTime() - startNanos;
            timings.add(durationNanos / 1_000_000.0);
        }

        double firstCall = timings.get(0);
        double avgWarm = timings.stream().skip(1).mapToDouble(d -> d).average().orElse(0);
        double speedup = firstCall / avgWarm;

        return ResponseEntity.ok(new ColdWarmComparison(
                firstCall,
                avgWarm,
                speedup,
                timings
        ));
    }

    private BenchmarkResult runSequentialBenchmark(Map<String, Object> input, int iterations)
            throws RuleEngineClientProcessingException {

        List<Long> timings = new ArrayList<>(iterations);
        long totalStart = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            ruleEngineClient.evaluateRules(input, Category.PRICING, Category.DEFAULT, HitPolicy.FIRST);
            timings.add(System.nanoTime() - start);
        }

        long totalDuration = System.nanoTime() - totalStart;
        return calculateStats(timings, totalDuration, iterations, 1);
    }

    private BenchmarkResult runParallelBenchmark(Map<String, Object> input, int iterations, int threads) {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Long> timings = java.util.Collections.synchronizedList(new ArrayList<>(iterations));

        long totalStart = System.nanoTime();

        List<CompletableFuture<Void>> futures = IntStream.range(0, iterations)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    try {
                        long start = System.nanoTime();
                        ruleEngineClient.evaluateRules(input, Category.PRICING, Category.DEFAULT, HitPolicy.FIRST);
                        timings.add(System.nanoTime() - start);
                    } catch (Throwable e) {
                        log.error("Error in parallel benchmark", e);
                    }
                }, executor))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        long totalDuration = System.nanoTime() - totalStart;

        executor.shutdown();
        return calculateStats(timings, totalDuration, iterations, threads);
    }

    private BenchmarkResult calculateStats(List<Long> timingsNanos, long totalDurationNanos,
                                           int iterations, int threads) {
        List<Double> timingsMs = timingsNanos.stream()
                .map(n -> n / 1_000_000.0)
                .sorted()
                .toList();

        double avg = timingsMs.stream().mapToDouble(d -> d).average().orElse(0);
        double min = timingsMs.stream().mapToDouble(d -> d).min().orElse(0);
        double max = timingsMs.stream().mapToDouble(d -> d).max().orElse(0);
        double p50 = percentile(timingsMs, 50);
        double p95 = percentile(timingsMs, 95);
        double p99 = percentile(timingsMs, 99);
        double throughput = iterations / (totalDurationNanos / 1_000_000_000.0);

        return new BenchmarkResult(
                iterations,
                threads,
                totalDurationNanos / 1_000_000.0,
                avg,
                min,
                max,
                p50,
                p95,
                p99,
                throughput
        );
    }

    private double percentile(List<Double> sorted, int percentile) {
        if (sorted.isEmpty()) return 0;
        int index = (int) Math.ceil(percentile / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1)));
    }

    // Response DTOs

    public record BenchmarkResult(
            int iterations,
            int threads,
            double totalDurationMs,
            double avgMs,
            double minMs,
            double maxMs,
            double p50Ms,
            double p95Ms,
            double p99Ms,
            double throughputPerSecond
    ) {}

    public record SingleEvaluationResult(
            double durationMs,
            int rulesEvaluated,
            boolean matched
    ) {}

    public record ColdWarmComparison(
            double firstCallMs,
            double avgWarmMs,
            double speedupFactor,
            List<Double> allTimingsMs
    ) {}
}
