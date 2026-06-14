package service.sorting;

import model.result.sorting.RuntimeStats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// Task A - Calculates summary statistics from benchmark samples.
public class RuntimeStatsCalculator {
    public RuntimeStats calculate(double[] runTimesMs) {
        double total = 0.0;
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        for (double runTimeMs : runTimesMs) {
            total += runTimeMs;
            min = Math.min(min, runTimeMs);
            max = Math.max(max, runTimeMs);
        }

        double average = total / runTimesMs.length;
        double standardDeviation = calculateStandardDeviation(runTimesMs, average);

        double[] sortedRunTimes = Arrays.copyOf(runTimesMs, runTimesMs.length);
        Arrays.sort(sortedRunTimes);

        return new RuntimeStats(
                average,
                calculateMedian(sortedRunTimes),
                min,
                max,
                max - min,
                standardDeviation,
                Collections.unmodifiableList(toList(runTimesMs))
        );
    }

    private double calculateStandardDeviation(double[] runTimesMs, double average) {
        double squaredDifferenceSum = 0.0;
        for (double runTimeMs : runTimesMs) {
            double difference = runTimeMs - average;
            squaredDifferenceSum += difference * difference;
        }
        return Math.sqrt(squaredDifferenceSum / runTimesMs.length);
    }

    private double calculateMedian(double[] sortedRunTimes) {
        int middleIndex = sortedRunTimes.length / 2;
        if (sortedRunTimes.length % 2 == 0) {
            return (sortedRunTimes[middleIndex - 1] + sortedRunTimes[middleIndex]) / 2.0;
        }
        return sortedRunTimes[middleIndex];
    }

    private List<Double> toList(double[] runTimesMs) {
        List<Double> samplesMs = new ArrayList<>(runTimesMs.length);
        for (double runTimeMs : runTimesMs) {
            samplesMs.add(runTimeMs);
        }
        return samplesMs;
    }
}
