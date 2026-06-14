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

// Task B - Finds shortest weighted routes with Floyd-Warshall.
public class FloydWarshall implements PathAlgorithm {
    @Override
    public PathResult findPath(Vertex source, Vertex target, Graph graph) {
        if (source == null || target == null) {
            return new PathResult(Collections.emptyList(), Double.POSITIVE_INFINITY);
        }

        List<Vertex> vertices = new ArrayList<>(graph.getVertices());
        Map<Vertex, Integer> indexes = buildIndexMap(vertices);
        int vertexCount = vertices.size();
        double[][] distances = new double[vertexCount][vertexCount];
        int[][] next = new int[vertexCount][vertexCount];

        initializeMatrices(graph, vertices, indexes, distances, next);

        for (int via = 0; via < vertexCount; via++) {
            for (int from = 0; from < vertexCount; from++) {
                for (int to = 0; to < vertexCount; to++) {
                    double candidateDistance = distances[from][via] + distances[via][to];
                    if (candidateDistance < distances[from][to]) {
                        distances[from][to] = candidateDistance;
                        next[from][to] = next[from][via];
                    }
                }
            }
        }

        int sourceIndex = indexes.get(source);
        int targetIndex = indexes.get(target);
        if (next[sourceIndex][targetIndex] == -1) {
            return new PathResult(Collections.emptyList(), Double.POSITIVE_INFINITY);
        }

        return new PathResult(buildPath(vertices, next, sourceIndex, targetIndex), distances[sourceIndex][targetIndex]);
    }

    private Map<Vertex, Integer> buildIndexMap(List<Vertex> vertices) {
        Map<Vertex, Integer> indexes = new HashMap<>();
        for (int index = 0; index < vertices.size(); index++) {
            indexes.put(vertices.get(index), index);
        }
        return indexes;
    }

    private void initializeMatrices(
            Graph graph,
            List<Vertex> vertices,
            Map<Vertex, Integer> indexes,
            double[][] distances,
            int[][] next
    ) {
        for (int row = 0; row < vertices.size(); row++) {
            for (int column = 0; column < vertices.size(); column++) {
                distances[row][column] = row == column ? 0.0 : Double.POSITIVE_INFINITY;
                next[row][column] = row == column ? column : -1;
            }
        }

        for (Vertex from : vertices) {
            int fromIndex = indexes.get(from);
            for (Edge edge : graph.getEdgesFrom(from)) {
                int toIndex = indexes.get(edge.getTo());
                if (edge.getWeight() < distances[fromIndex][toIndex]) {
                    distances[fromIndex][toIndex] = edge.getWeight();
                    next[fromIndex][toIndex] = toIndex;
                }
            }
        }
    }

    private List<String> buildPath(List<Vertex> vertices, int[][] next, int sourceIndex, int targetIndex) {
        List<String> path = new ArrayList<>();
        int currentIndex = sourceIndex;
        path.add(vertices.get(currentIndex).getId());

        while (currentIndex != targetIndex) {
            currentIndex = next[currentIndex][targetIndex];
            if (currentIndex == -1) {
                return Collections.emptyList();
            }
            path.add(vertices.get(currentIndex).getId());
        }

        return path;
    }
}
