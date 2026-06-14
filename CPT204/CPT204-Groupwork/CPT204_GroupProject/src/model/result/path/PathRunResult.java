package model.result.path;

// Task B - Stores one measured path-finding run.
public class PathRunResult {
    private final String caseName;
    private final String start;
    private final String destination;
    private final String via;
    private final PathResult pathResult;
    private final double runtimeMs;

    public PathRunResult(
            String caseName,
            String start,
            String destination,
            String via,
            PathResult pathResult,
            double runtimeMs
    ) {
        this.caseName = caseName;
        this.start = start;
        this.destination = destination;
        this.via = via;
        this.pathResult = pathResult;
        this.runtimeMs = runtimeMs;
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

    public PathResult pathResult() {
        return pathResult;
    }

    public double runtimeMs() {
        return runtimeMs;
    }
}
