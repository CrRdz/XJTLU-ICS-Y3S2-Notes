package algorithm.path;

import model.graph.Edge;
import model.graph.Graph;
import model.graph.Vertex;
import model.result.path.PathResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Task B - Finds the shortest weighted route with Bellman-Ford.
public class BellmanFord implements PathAlgorithm {
    @Override
    public PathResult findPath(Vertex source, Vertex target, Graph graph) {
        if (source == null || target == null) {
            return new PathResult(Collections.emptyList(), Double.POSITIVE_INFINITY);
        }

        Map<Vertex, Integer> distances = new HashMap<>();
        Map<Vertex, Vertex> previousVertices = new HashMap<>();

        for (Vertex vertex : graph.getVertices()) {
            distances.put(vertex, Integer.MAX_VALUE);
        }
        distances.put(source, 0);

        int vertexCount = graph.getVertices().size();
        for (int pass = 0; pass < vertexCount - 1; pass++) {
            boolean updated = false;
            for (Vertex current : graph.getVertices()) {
                int currentDistance = distances.get(current);
                if (currentDistance == Integer.MAX_VALUE) {
                    continue;
                }

                for (Edge edge : graph.getEdgesFrom(current)) {
                    Vertex neighbor = edge.getTo();
                    int candidateDistance = currentDistance + edge.getWeight();
                    if (candidateDistance < distances.get(neighbor)) {
                        distances.put(neighbor, candidateDistance);
                        previousVertices.put(neighbor, current);
                        updated = true;
                    }
                }
            }

            if (!updated) {
                break;
            }
        }

        if (distances.get(target) == Integer.MAX_VALUE) {
            return new PathResult(Collections.emptyList(), Double.POSITIVE_INFINITY);
        }

        return new PathResult(buildPath(source, target, previousVertices), distances.get(target));
    }

    private List<String> buildPath(Vertex source, Vertex target, Map<Vertex, Vertex> previousVertices) {
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
