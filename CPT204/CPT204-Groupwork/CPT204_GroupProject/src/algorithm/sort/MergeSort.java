package algorithm.sort;
import model.sorting.Candidate;
import java.util.ArrayList;
import java.util.List;

// Task A - Sorts candidate rankings with Merge Sort.
public class MergeSort implements SortAlgorithm {
    // Returns a sorted copy, leaving the original list unchanged.
    @Override
    public List<Candidate> sort(List<Candidate> candidates) {
        if (candidates.size() <= 1) {
            return new ArrayList<>(candidates);
        }

        return mergeSort(new ArrayList<>(candidates));
    }

    // Recursively splits the list until each part is already sorted.
    private List<Candidate> mergeSort(List<Candidate> candidates) {
        if (candidates.size() <= 1) {
            return candidates;
        }

        int middle = candidates.size() / 2;
        List<Candidate> left = mergeSort(new ArrayList<>(candidates.subList(0, middle)));
        List<Candidate> right = mergeSort(new ArrayList<>(candidates.subList(middle, candidates.size())));

        return merge(left, right);
    }

    // Merges two sorted lists into one sorted result.
    private List<Candidate> merge(List<Candidate> left, List<Candidate> right) {
        List<Candidate> merged = new ArrayList<>();
        int leftIndex = 0;
        int rightIndex = 0;

        while (leftIndex < left.size() && rightIndex < right.size()) {
            // Keep equal items from the left first to preserve stable ordering.
            if (left.get(leftIndex).compareTo(right.get(rightIndex)) <= 0) {
                merged.add(left.get(leftIndex));
                leftIndex++;
            } else {
                merged.add(right.get(rightIndex));
                rightIndex++;
            }
        }

        while (leftIndex < left.size()) {
            merged.add(left.get(leftIndex));
            leftIndex++;
        }

        while (rightIndex < right.size()) {
            merged.add(right.get(rightIndex));
            rightIndex++;
        }

        return merged;
    }
}
