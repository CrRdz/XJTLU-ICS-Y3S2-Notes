package algorithm.sort;

// Task A - Identifies sorting algorithms and their display names.
public enum SortAlgorithmType {
    BUBBLE("Bubble Sort"),
    INSERTION("Insertion Sort"),
    QUICK("Quick Sort"),
    MERGE("Merge Sort");

    private final String displayName;

    SortAlgorithmType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static SortAlgorithmType fromDisplayName(String displayName) {
        for (SortAlgorithmType type : values()) {
            if (type.displayName.equals(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown algorithm: " + displayName);
    }
}
