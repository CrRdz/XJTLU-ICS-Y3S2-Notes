package algorithm.sort;

import model.sorting.Candidate;

import java.util.List;

// Task A - Common contract for candidate sorting algorithms.
public interface SortAlgorithm {
    List<Candidate> sort(List<Candidate> candidates);
}
