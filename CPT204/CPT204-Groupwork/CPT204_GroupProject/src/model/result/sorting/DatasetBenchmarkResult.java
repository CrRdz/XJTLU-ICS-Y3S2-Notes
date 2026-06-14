package model.result.sorting;

import model.sorting.Candidate;

import java.util.List;
import java.util.Map;

// Task A - Stores benchmark output and top candidates for one dataset.
public class DatasetBenchmarkResult {
    private final String datasetName;
    private final int datasetSize;
    private final Map<String, RuntimeStats> runtimes;
    private final List<Candidate> topCandidates;

    public DatasetBenchmarkResult(
            String datasetName,
            int datasetSize,
            Map<String, RuntimeStats> runtimes,
            List<Candidate> topCandidates
    ) {
        this.datasetName = datasetName;
        this.datasetSize = datasetSize;
        this.runtimes = runtimes;
        this.topCandidates = topCandidates;
    }

    public String datasetName() {
        return datasetName;
    }

    public int datasetSize() {
        return datasetSize;
    }

    public Map<String, RuntimeStats> runtimes() {
        return runtimes;
    }

    public List<Candidate> topCandidates() {
        return topCandidates;
    }
}
