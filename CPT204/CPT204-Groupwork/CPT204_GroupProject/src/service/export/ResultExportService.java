package service.export;

import model.sorting.Candidate;
import model.result.sorting.DatasetBenchmarkResult;
import model.result.path.PathComparisonResult;
import model.result.path.PathRunResult;
import model.result.sorting.RuntimeStats;
import service.sorting.BenchmarkService;
import util.FormatUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

// Shared - Exports Task A benchmark results and Task B path results.
public class ResultExportService {
    private static final String RESULTS_DIRECTORY = "results";

    public void saveTaskAResults(DatasetBenchmarkResult benchmarkResult, String fileName) throws IOException {
        writeResultFile(fileName, buildTaskAResultsContent(benchmarkResult));
    }

    public void saveTaskASummaryCsv(List<DatasetBenchmarkResult> benchmarkResults, String fileName)
            throws IOException {
        StringBuilder content = new StringBuilder();
        content.append("dataset,algorithm,average_ms,median_ms,min_ms,max_ms,range_ms,std_dev_ms")
                .append(System.lineSeparator());

        for (DatasetBenchmarkResult benchmarkResult : benchmarkResults) {
            for (Map.Entry<String, RuntimeStats> entry : benchmarkResult.runtimes().entrySet()) {
                RuntimeStats stats = entry.getValue();
                content.append(benchmarkResult.datasetName()).append(",")
                        .append(entry.getKey()).append(",")
                        .append(formatCsvNumber(stats.averageMs())).append(",")
                        .append(formatCsvNumber(stats.medianMs())).append(",")
                        .append(formatCsvNumber(stats.minMs())).append(",")
                        .append(formatCsvNumber(stats.maxMs())).append(",")
                        .append(formatCsvNumber(stats.rangeMs())).append(",")
                        .append(formatCsvNumber(stats.standardDeviationMs())).append(System.lineSeparator());
            }
        }

        writeResultFile(fileName, content.toString());
    }

    public void saveTaskARawRuntimeCsv(List<DatasetBenchmarkResult> benchmarkResults, String fileName)
            throws IOException {
        StringBuilder content = new StringBuilder();
        content.append("dataset,algorithm,run,runtime_ms").append(System.lineSeparator());

        for (DatasetBenchmarkResult benchmarkResult : benchmarkResults) {
            for (Map.Entry<String, RuntimeStats> entry : benchmarkResult.runtimes().entrySet()) {
                List<Double> samplesMs = entry.getValue().samplesMs();
                for (int runIndex = 0; runIndex < samplesMs.size(); runIndex++) {
                    content.append(benchmarkResult.datasetName()).append(",")
                            .append(entry.getKey()).append(",")
                            .append(runIndex + 1).append(",")
                            .append(formatCsvNumber(samplesMs.get(runIndex)))
                            .append(System.lineSeparator());
                }
            }
        }

        writeResultFile(fileName, content.toString());
    }

    public void saveShortestPathResults(List<PathRunResult> results, String fileName) throws IOException {
        writeResultFile(fileName, buildShortestPathResultsContent(results));
    }

    public void savePathComparisonResults(List<PathComparisonResult> results, String fileName) throws IOException {
        writeResultFile(fileName, buildPathComparisonResultsContent(results));
    }

    private String buildTaskAResultsContent(DatasetBenchmarkResult benchmarkResult) {
        StringBuilder content = new StringBuilder();
        content.append(benchmarkResult.datasetName()).append(System.lineSeparator());
        content.append("Dataset size: ").append(benchmarkResult.datasetSize()).append(System.lineSeparator());
        content.append(System.lineSeparator());
        content.append("Runtime statistics (over ").append(BenchmarkService.RUNS_PER_ALGORITHM).append(" runs):")
                .append(System.lineSeparator());

        for (Map.Entry<String, RuntimeStats> entry : benchmarkResult.runtimes().entrySet()) {
            RuntimeStats stats = entry.getValue();
            content.append(entry.getKey()).append(":").append(System.lineSeparator());
            content.append("  Average: ").append(String.format("%.3f ms", stats.averageMs()))
                    .append(System.lineSeparator());
            content.append("  Median: ").append(String.format("%.3f ms", stats.medianMs()))
                    .append(System.lineSeparator());
            content.append("  Best: ").append(String.format("%.3f ms", stats.minMs()))
                    .append(System.lineSeparator());
            content.append("  Worst: ").append(String.format("%.3f ms", stats.maxMs()))
                    .append(System.lineSeparator());
            content.append("  Range: ").append(String.format("%.3f ms", stats.rangeMs()))
                    .append(System.lineSeparator());
            content.append("  Standard deviation: ")
                    .append(String.format("%.3f ms", stats.standardDeviationMs()))
                    .append(System.lineSeparator());
        }

        content.append(System.lineSeparator());
        content.append("Top ").append(benchmarkResult.topCandidates().size()).append(":")
                .append(System.lineSeparator());
        for (int index = 0; index < benchmarkResult.topCandidates().size(); index++) {
            Candidate candidate = benchmarkResult.topCandidates().get(index);
            content.append(index + 1)
                    .append(". ")
                    .append(candidate.getLocationId())
                    .append(" (priorityScore=")
                    .append(String.format("%.0f", candidate.getPriorityScore()))
                    .append(")")
                    .append(System.lineSeparator());
        }
        return content.toString();
    }

    private String buildShortestPathResultsContent(List<PathRunResult> results) {
        StringBuilder content = new StringBuilder();
        content.append("Task B: Shortest Path Results").append(System.lineSeparator());

        for (PathRunResult result : results) {
            content.append(System.lineSeparator());
            content.append(result.caseName()).append(System.lineSeparator());
            content.append("Start: ").append(result.start()).append(System.lineSeparator());
            content.append("Destination: ").append(result.destination()).append(System.lineSeparator());
            if (result.via() != null) {
                content.append("Via: ").append(result.via()).append(System.lineSeparator());
            }
            content.append("Total distance: ").append(FormatUtils.formatCost(result.pathResult().getDistance()))
                    .append(System.lineSeparator());
            content.append("Number of nodes in path: ").append(result.pathResult().getPath().size())
                    .append(System.lineSeparator());
            content.append("Path: ").append(String.join(" -> ", result.pathResult().getPath()))
                    .append(System.lineSeparator());
        }
        return content.toString();
    }

    private String buildPathComparisonResultsContent(List<PathComparisonResult> results) {
        StringBuilder content = new StringBuilder();
        content.append("Task B: Weighted Shortest Path Algorithm Comparison").append(System.lineSeparator());

        for (PathComparisonResult result : results) {
            content.append(System.lineSeparator());
            content.append(result.caseName()).append(System.lineSeparator());
            content.append("Start: ").append(result.start()).append(System.lineSeparator());
            content.append("Destination: ").append(result.destination()).append(System.lineSeparator());
            if (result.via() != null) {
                content.append("Via: ").append(result.via()).append(System.lineSeparator());
            }
            content.append(buildPathAlgorithmSection("Dijkstra", result.dijkstraResult()));
            content.append(buildPathAlgorithmSection("Bellman-Ford", result.bellmanFordResult()));
            content.append(buildPathAlgorithmSection("Floyd-Warshall", result.floydWarshallResult()));
        }

        return content.toString();
    }

    private String buildPathAlgorithmSection(String algorithmName, PathRunResult result) {
        return algorithmName + ":" + System.lineSeparator()
                + "  Total distance: " + FormatUtils.formatCost(result.pathResult().getDistance())
                + System.lineSeparator()
                + "  Number of nodes in path: " + result.pathResult().getPath().size()
                + System.lineSeparator()
                + "  Runtime: " + String.format("%.3f ms", result.runtimeMs())
                + System.lineSeparator()
                + "  Path: " + String.join(" -> ", result.pathResult().getPath())
                + System.lineSeparator();
    }

    private void writeResultFile(String fileName, String content) throws IOException {
        File resultsDirectory = new File(RESULTS_DIRECTORY);
        if (!resultsDirectory.exists() && !resultsDirectory.mkdirs()) {
            throw new IOException("Failed to create results directory: " + RESULTS_DIRECTORY);
        }

        File outputFile = new File(resultsDirectory, fileName);
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(content);
        }
    }

    private String formatCsvNumber(double value) {
        return String.format("%.6f", value);
    }

}
