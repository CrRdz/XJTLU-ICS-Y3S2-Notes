package algorithm.path;
import model.graph.Edge;
import model.graph.Graph;
import model.result.path.PathResult;
import model.graph.Vertex;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

// Task B - Finds the shortest weighted route with Dijkstra's algorithm.
public class Dijkstra implements PathAlgorithm {
    // Calculates the shortest path from source to target.
    @Override
    public PathResult findPath(Vertex source, Vertex target, Graph graph) {
        return shortestPath(source, target, graph);
    }

    public PathResult shortestPath(Vertex source, Vertex target, Graph graph) {
        if (source == null || target == null) {
            return new PathResult(Collections.emptyList(), Double.POSITIVE_INFINITY);
        }

        // Track best known distances and previous vertices for path rebuilding.
        Map<Vertex, Integer> distances = new HashMap<>();
        Map<Vertex, Vertex> previousVertices = new HashMap<>();
        PriorityQueue<Vertex> queue = new PriorityQueue<>(Comparator.comparingInt(distances::get));

        // Start with all vertices marked as unreachable.
        for (Vertex vertex : graph.getVertices()) {
            distances.put(vertex, Integer.MAX_VALUE);
        }

        distances.put(source, 0);
        queue.offer(source);

        while (!queue.isEmpty()) {
            // Always expand the currently closest vertex.
            Vertex current = queue.poll();
            int currentDistance = distances.get(current);

            if (current.equals(target)) {
                break;
            }

            if (currentDistance == Integer.MAX_VALUE) {
                continue;
            }

            for (Edge edge : graph.getEdgesFrom(current)) {
                Vertex neighbor = edge.getTo();
                int candidateDistance = currentDistance + edge.getWeight();

                // Relax the edge when a shorter route to the neighbor is found.
                if (candidateDistance < distances.get(neighbor)) {
                    distances.put(neighbor, candidateDistance);
                    previousVertices.put(neighbor, current);
                    queue.remove(neighbor);
                    queue.offer(neighbor);
                }
            }
        }

        if (distances.get(target) == Integer.MAX_VALUE) {
            return new PathResult(Collections.emptyList(), Double.POSITIVE_INFINITY);
        }

        return new PathResult(buildPath(source, target, previousVertices), distances.get(target));
    }

    // Rebuilds the path by walking backward from target to source.
    private List<String> buildPath(
            Vertex source,
            Vertex target,
            Map<Vertex, Vertex> previousVertices
    ) {
        List<String> path = new ArrayList<>();
        Vertex current = target;

        while (current != null) {
            path.add(current.getId());
            if (current.equals(source)) {
                break;
            }
            current = previousVertices.get(current);
        }

        Collections.reverse(path);
        return path;
    }
}
