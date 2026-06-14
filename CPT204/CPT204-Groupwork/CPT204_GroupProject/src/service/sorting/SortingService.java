package service.sorting;

import algorithm.sort.BubbleSort;
import algorithm.sort.InsertionSort;
import algorithm.sort.MergeSort;
import algorithm.sort.QuickSort;
import algorithm.sort.SortAlgorithm;
import algorithm.sort.SortAlgorithmType;
import model.sorting.Candidate;
import service.data.DataService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Task A - Provides candidate sorting operations for selected datasets.
public class SortingService {
    private final DataService dataService;
    private final Map<SortAlgorithmType, SortAlgorithm> algorithms = new LinkedHashMap<>();

    public SortingService(DataService dataService) {
        this.dataService = dataService;
        registerAlgorithm(SortAlgorithmType.BUBBLE, new BubbleSort());
        registerAlgorithm(SortAlgorithmType.INSERTION, new InsertionSort());
        registerAlgorithm(SortAlgorithmType.QUICK, new QuickSort());
        registerAlgorithm(SortAlgorithmType.MERGE, new MergeSort());
    }

    public List<Candidate> sortCandidates(String datasetName, String algorithmName) {
        return sortCandidates(datasetName, SortAlgorithmType.fromDisplayName(algorithmName));
    }

    public List<Candidate> sortCandidates(String datasetName, SortAlgorithmType algorithmType) {
        List<Candidate> candidates = new ArrayList<>(dataService.getCandidates(datasetName));
        SortAlgorithm algorithm = algorithms.get(algorithmType);
        if (algorithm == null) {
            throw new IllegalArgumentException("Unknown algorithm: " + algorithmType);
        }
        return algorithm.sort(candidates);
    }

    public List<String> getAlgorithmNames() {
        return algorithms.keySet().stream()
                .map(SortAlgorithmType::displayName)
                .toList();
    }

    private void registerAlgorithm(SortAlgorithmType algorithmType, SortAlgorithm algorithm) {
        algorithms.put(algorithmType, algorithm);
    }
}
