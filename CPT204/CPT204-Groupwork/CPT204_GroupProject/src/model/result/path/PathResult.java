package model.result.path;
import java.util.Collections;
import java.util.List;

// Task B - Stores a path result and its total distance.
public class PathResult {
    private final List<String> path;
    private final double distance;

    public PathResult(List<String> path, double distance) {
        // Copy the list so the result cannot be changed from outside.
        this.path = List.copyOf(path);
        this.distance = distance;
    }

    public List<String> getPath() {
        return Collections.unmodifiableList(path);
    }

    public double getDistance() {
        return distance;
    }

    public boolean pathExists() {
        // A valid path must have nodes and a finite distance.
        return !path.isEmpty() && !Double.isInfinite(distance);
    }

    @Override
    public String toString() {
        return "Path: " + String.join(" -> ", path) + System.lineSeparator()
                + "Distance: " + distance;
    }
}
