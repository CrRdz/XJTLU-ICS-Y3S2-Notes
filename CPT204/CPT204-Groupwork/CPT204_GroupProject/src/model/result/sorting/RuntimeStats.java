package model.result.sorting;

import java.util.List;

// Task A - Stores runtime statistics for one sorting algorithm.
public class RuntimeStats {
    private final double averageMs;
    private final double medianMs;
    private final double minMs;
    private final double maxMs;
    private final double rangeMs;
    private final double standardDeviationMs;
    private final List<Double> samplesMs;

    public RuntimeStats(
            double averageMs,
            double medianMs,
            double minMs,
            double maxMs,
            double rangeMs,
            double standardDeviationMs,
            List<Double> samplesMs
    ) {
        this.averageMs = averageMs;
        this.medianMs = medianMs;
        this.minMs = minMs;
        this.maxMs = maxMs;
        this.rangeMs = rangeMs;
        this.standardDeviationMs = standardDeviationMs;
        this.samplesMs = samplesMs;
    }

    public double averageMs() {
        return averageMs;
    }

    public double medianMs() {
        return medianMs;
    }

    public double minMs() {
        return minMs;
    }

    public double maxMs() {
        return maxMs;
    }

    public double rangeMs() {
        return rangeMs;
    }

    public double standardDeviationMs() {
        return standardDeviationMs;
    }

    public List<Double> samplesMs() {
        return samplesMs;
    }
}
