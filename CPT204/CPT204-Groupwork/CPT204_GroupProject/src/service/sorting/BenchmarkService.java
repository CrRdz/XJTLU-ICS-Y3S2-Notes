package service.sorting;

import algorithm.sort.SortAlgorithmType;
import model.sorting.Candidate;
import model.result.sorting.DatasetBenchmarkResult;
import model.result.sorting.RuntimeStats;
import service.data.DataService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Task A - Runs sorting benchmarks and prepares dataset results.
public class BenchmarkService {
    public static final int RUNS_PER_ALGORITHM = 100;
    public static final int WARM_UP_RUNS = 10;
    public static final int TOP_N = 10;

    private final DataService dataService;
    private final SortingService sortingService;
    private final RuntimeStatsCalculator statsCalculator;

    public BenchmarkService(DataService dataService, SortingService sortingService) {
        this(dataService, sortingService, new RuntimeStatsCalculator());
    }

    public BenchmarkService(
            DataService dataService,
            SortingService sortingService,
            RuntimeStatsCalculator statsCalculator
    ) {
        this.dataService = dataService;
        this.sortingService = sortingService;
        this.statsCalculator = statsCalculator;
    }

    public DatasetBenchmarkResult runDatasetBenchmark(String datasetName) {
        List<Candidate> originalCandidates = dataService.getCandidates(datasetName);
        Map<String, RuntimeStats> runtimes = new LinkedHashMap<>();

        for (String algorithmName : sortingService.getAlgorithmNames()) {
            runtimes.put(algorithmName, benchmarkAlgorithm(datasetName, algorithmName, originalCandidates));
        }

        List<Candidate> sortedCandidates = sortingService.sortCandidates(datasetName, SortAlgorithmType.MERGE);
        List<Candidate> topCandidates = new ArrayList<>(
                sortedCandidates.subList(0, Math.min(TOP_N, sortedCandidates.size()))
        );

        return new DatasetBenchmarkResult(datasetName, originalCandidates.size(), runtimes, topCandidates);
    }

    public List<DatasetBenchmarkResult> runAllDatasetBenchmarks() {
        List<DatasetBenchmarkResult> results = new ArrayList<>();
        for (String datasetName : dataService.getDatasetNames()) {
            results.add(runDatasetBenchmark(datasetName));
        }
        return results;
    }

    private RuntimeStats benchmarkAlgorithm(
            String datasetName,
            String algorithmName,
            List<Candidate> originalCandidates
    ) {
        double[] runTimesMs = new double[RUNS_PER_ALGORITHM];

        for (int warmUpRun = 0; warmUpRun < WARM_UP_RUNS; warmUpRun++) {
            List<Candidate> warmUpResult = sortingService.sortCandidates(datasetName, algorithmName);
            if (warmUpResult.size() != originalCandidates.size()) {
                throw new IllegalStateException("Warm-up result size does not match original dataset size.");
            }
        }

        for (int run = 0; run < RUNS_PER_ALGORITHM; run++) {
            long startTime = System.nanoTime();
            List<Candidate> sortedCandidates = sortingService.sortCandidates(datasetName, algorithmName);
            long endTime = System.nanoTime();
            runTimesMs[run] = (endTime - startTime) / 1_000_000.0;

            if (sortedCandidates.size() != originalCandidates.size()) {
                throw new IllegalStateException("Sorted result size does not match original dataset size.");
            }
        }

        return statsCalculator.calculate(runTimesMs);
    }
}
