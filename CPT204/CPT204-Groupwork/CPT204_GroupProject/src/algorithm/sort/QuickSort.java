package algorithm.sort;
import model.sorting.Candidate;
import java.util.ArrayList;
import java.util.List;

// Task A - Sorts candidate rankings with Quick Sort.
public class QuickSort implements SortAlgorithm {
    // Returns a sorted copy, leaving the original list unchanged.
    @Override
    public List<Candidate> sort(List<Candidate> candidates) {
        List<Candidate> sorted = new ArrayList<>(candidates);
        // candidates, low, high
        quickSort(sorted, 0, sorted.size() - 1);
        return sorted;
    }

    // Recursively sorts the range between low and high.
    private void quickSort(List<Candidate> candidates, int low, int high) {
        if (low >= high) {
            return;
        }

        int pivotIndex = partition(candidates, low, high);

        // right and left
        quickSort(candidates, low, pivotIndex - 1);
        quickSort(candidates, pivotIndex + 1, high);
    }

    // choose a pivot, right: lower, left: higher
    private int partition(List<Candidate> candidates, int low, int high) {
        Candidate pivot = candidates.get(high);
        int i = low - 1;

        // (low-i) pivot ((i+1)-(high-1))
        for (int j = low; j < high; j++) {
            // Items ordered before the pivot are moved to the left side.
            if (candidates.get(j).compareTo(pivot) <= 0) { // priority and location id
                i++;
                swap(candidates, i, j);
            }
        }

        swap(candidates, i + 1, high);
        return i + 1; // pivot
    }

    // A B to B A
    private void swap(List<Candidate> candidates, int first, int second) {
        Candidate temp = candidates.get(first);
        candidates.set(first, candidates.get(second));
        candidates.set(second, temp);
    }
}
