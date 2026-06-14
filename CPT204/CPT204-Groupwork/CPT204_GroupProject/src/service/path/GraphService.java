package service.path;

import algorithm.path.BellmanFord;
import algorithm.path.Dijkstra;
import algorithm.path.FloydWarshall;
import algorithm.path.PathAlgorithm;
import algorithm.path.PathAlgorithmType;
import model.graph.Graph;
import model.result.path.PathResult;
import model.graph.Vertex;

import java.util.ArrayList;
import java.util.List;

// Task B - Provides reusable shortest-path operations.
public class GraphService {
    private final PathAlgorithm dijkstra = new Dijkstra();
    private final PathAlgorithm bellmanFord = new BellmanFord();
    private final PathAlgorithm floydWarshall = new FloydWarshall();

    public PathResult findPath(String sourceId, String targetId, Graph graph, PathAlgorithmType algorithmType) {
        if (sourceId.equals(targetId)) {
            return new PathResult(List.of(sourceId), 0.0);
        }

        Vertex source = graph.getVertex(sourceId);
        Vertex target = graph.getVertex(targetId);
        return getAlgorithm(algorithmType).findPath(source, target, graph);
    }

    public PathResult findShortestPath(String sourceId, String targetId, Graph graph) {
        return findPath(sourceId, targetId, graph, PathAlgorithmType.DIJKSTRA);
    }

    public PathResult findPathVia(
            String sourceId,
            String viaId,
            String targetId,
            Graph graph,
            PathAlgorithmType algorithmType
    ) {
        return findPathViaStops(sourceId, List.of(viaId), targetId, graph, algorithmType);
    }

    // Finds a shortest route through one intermediate stop.
    public PathResult findShortestPathVia(String sourceId, String viaId, String targetId, Graph graph) {
        return findPathVia(sourceId, viaId, targetId, graph, PathAlgorithmType.DIJKSTRA);
    }

    // Finds a route through multiple required stops in order.
    public PathResult findPathViaStops(
            String sourceId,
            List<String> viaIds,
            String targetId,
            Graph graph,
            PathAlgorithmType algorithmType
    ) {
        List<String> stops = new ArrayList<>();
        stops.add(sourceId);
        stops.addAll(viaIds);
        stops.add(targetId);

        List<String> combinedPath = new ArrayList<>();
        double totalDistance = 0.0;

        for (int i = 0; i < stops.size() - 1; i++) {
            PathResult segment = findPath(stops.get(i), stops.get(i + 1), graph, algorithmType);
            if (!segment.pathExists()) {
                return new PathResult(List.of(), Double.POSITIVE_INFINITY);
            }

            if (combinedPath.isEmpty()) {
                combinedPath.addAll(segment.getPath());
            } else {
                combinedPath.addAll(segment.getPath().subList(1, segment.getPath().size()));
            }

            totalDistance += segment.getDistance();
        }

        return new PathResult(combinedPath, totalDistance);
    }

    private PathAlgorithm getAlgorithm(PathAlgorithmType algorithmType) {
        return switch (algorithmType) {
            case DIJKSTRA -> dijkstra;
            case BELLMAN_FORD -> bellmanFord;
            case FLOYD_WARSHALL -> floydWarshall;
        };
    }
}
