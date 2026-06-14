package model.result.path;

// Task B - Stores one required route solved by weighted shortest-path algorithms.
public class PathComparisonResult {
    private final String caseName;
    private final String start;
    private final String destination;
    private final String via;
    private final PathRunResult dijkstraResult;
    private final PathRunResult bellmanFordResult;
    private final PathRunResult floydWarshallResult;

    public PathComparisonResult(
            String caseName,
            String start,
            String destination,
            String via,
            PathRunResult dijkstraResult,
            PathRunResult bellmanFordResult,
            PathRunResult floydWarshallResult
    ) {
        this.caseName = caseName;
        this.start = start;
        this.destination = destination;
        this.via = via;
        this.dijkstraResult = dijkstraResult;
        this.bellmanFordResult = bellmanFordResult;
        this.floydWarshallResult = floydWarshallResult;
    }

    public String caseName() {
        return caseName;
    }

    public String start() {
        return start;
    }

    public String destination() {
        return destination;
    }

    public String via() {
        return via;
    }

    public PathRunResult dijkstraResult() {
        return dijkstraResult;
    }

    public PathRunResult bellmanFordResult() {
        return bellmanFordResult;
    }

    public PathRunResult floydWarshallResult() {
        return floydWarshallResult;
    }
}
