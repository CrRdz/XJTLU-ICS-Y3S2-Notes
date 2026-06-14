package algorithm.sort;

import model.sorting.Candidate;

import java.util.ArrayList;
import java.util.List;

// Task A - Sorts candidate rankings with Insertion Sort.
public class InsertionSort implements SortAlgorithm {
    // Returns a sorted copy, leaving the original list unchanged.
    @Override
    public List<Candidate> sort(List<Candidate> candidates) {
        List<Candidate> sorted = new ArrayList<>(candidates);

        for (int i = 1; i < sorted.size(); i++) {
            Candidate current = sorted.get(i);
            int j = i - 1;

            // Shift larger items right until the correct insertion point is found.
            while (j >= 0 && sorted.get(j).compareTo(current) > 0) {
                sorted.set(j + 1, sorted.get(j));
                j--;
            }
            sorted.set(j + 1, current);
        }

        return sorted;
    }
}
