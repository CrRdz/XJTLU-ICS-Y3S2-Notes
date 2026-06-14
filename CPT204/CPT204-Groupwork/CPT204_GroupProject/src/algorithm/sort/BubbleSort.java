package algorithm.sort;
import model.sorting.Candidate;
import java.util.ArrayList;
import java.util.List;

// Task A - Sorts candidate rankings with Bubble Sort.
public class BubbleSort implements SortAlgorithm {
    // Returns a sorted copy, leaving the original list unchanged.
    @Override
    public List<Candidate> sort(List<Candidate> candidates) {
        List<Candidate> sorted = new ArrayList<>(candidates);
        int n = sorted.size();

        for (int i = 0; i < n - 1; i++) {
            // If no swap happens in a pass, the list is already sorted.
            boolean swapped = false;
            for (int j = 0; j < n - 1 - i; j++) { // compare 2 to 1, then 3 to 2, etc
                if (sorted.get(j).compareTo(sorted.get(j + 1)) > 0) {
                    Candidate temp = sorted.get(j);
                    sorted.set(j, sorted.get(j + 1));
                    sorted.set(j + 1, temp);
                    swapped = true;
                }
            }

            if (!swapped) { // sorted
                break;
            }
        }

        return sorted;
    }
}
