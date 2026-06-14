package service.path;

import algorithm.path.PathAlgorithmType;
import algorithm.sort.SortAlgorithmType;
import model.sorting.Candidate;
import model.graph.Graph;
import model.result.path.PathComparisonResult;
import model.result.path.PathResult;
import model.result.path.PathRunResult;
import service.sorting.SortingService;

import java.util.List;

// Task B - Builds required routes and compares path-finding algorithms.
public class PathComparisonService {
    private static final SortAlgorithmType RANKING_ALGORITHM = SortAlgorithmType.MERGE;

    private final SortingService sortingService;
    private final GraphService graphService;

    public PathComparisonService(SortingService sortingService, GraphService graphService) {
        this.sortingService = sortingService;
        this.graphService = graphService;
    }

    public List<PathRunResult> buildShortestPathResults(Graph graph) {
        return compareRequiredRoutes(graph).stream()
                .map(PathComparisonResult::dijkstraResult)
                .toList();
    }

    public List<PathComparisonResult> compareRequiredRoutes(Graph graph) {
        return buildRouteDefinitions().stream()
                .map(route -> measureComparison(route, graph))
                .toList();
    }

    public PathComparisonResult findComparison(String caseName, List<PathComparisonResult> results) {
        for (PathComparisonResult result : results) {
            if (result.caseName().equals(caseName)) {
                return result;
            }
        }
        throw new IllegalArgumentException("Unknown required route: " + caseName);
    }

    public PathRunResult findRunResult(String caseName, List<PathRunResult> results) {
        for (PathRunResult result : results) {
            if (result.caseName().equals(caseName)) {
                return result;
            }
        }
        throw new IllegalArgumentException("Unknown required route: " + caseName);
    }

    private List<RouteDefinition> buildRouteDefinitions() {
        List<Candidate> sortedCandidatesA = sortingService.sortCandidates("Dataset A", RANKING_ALGORITHM);
        List<Candidate> sortedCandidatesB = sortingService.sortCandidates("Dataset B", RANKING_ALGORITHM);
        List<Candidate> sortedCandidatesC = sortingService.sortCandidates("Dataset C", RANKING_ALGORITHM);

        String a1 = getLocationId(sortedCandidatesA, 0);
        String a10 = getLocationId(sortedCandidatesA, 9);
        String b1 = getLocationId(sortedCandidatesB, 0);
        String b5 = getLocationId(sortedCandidatesB, 4);
        String c1 = getLocationId(sortedCandidatesC, 0);
        String c5 = getLocationId(sortedCandidatesC, 4);

        return List.of(
                new RouteDefinition("Case 1", a1, a1, List.of()),
                new RouteDefinition("Case 2", a1, a10, List.of()),
                new RouteDefinition("Case 3", a1, b1, List.of(b5)),
                new RouteDefinition("Case 4", a1, c1, List.of(b5, c5))
        );
    }

    private PathComparisonResult measureComparison(RouteDefinition route, Graph graph) {
        PathRunResult dijkstraResult = measureRoute(route, graph, PathAlgorithmType.DIJKSTRA);
        PathRunResult bellmanFordResult = measureRoute(route, graph, PathAlgorithmType.BELLMAN_FORD);
        PathRunResult floydWarshallResult = measureRoute(route, graph, PathAlgorithmType.FLOYD_WARSHALL);

        return new PathComparisonResult(
                route.caseName,
                route.start,
                route.destination,
                route.formatVia(),
                dijkstraResult,
                bellmanFordResult,
                floydWarshallResult
        );
    }

    private PathRunResult measureRoute(RouteDefinition route, Graph graph, PathAlgorithmType algorithmType) {
        long startTime = System.nanoTime();
        PathResult result = graphService.findPathViaStops(
                route.start,
                route.viaIds,
                route.destination,
                graph,
                algorithmType
        );
        long endTime = System.nanoTime();

        return new PathRunResult(
                route.caseName,
                route.start,
                route.destination,
                route.formatVia(),
                result,
                (endTime - startTime) / 1_000_000.0
        );
    }

    private String getLocationId(List<Candidate> candidates, int index) {
        if (index >= candidates.size()) {
            throw new IllegalStateException("Dataset does not contain enough candidates for route comparison.");
        }
        return candidates.get(index).getLocationId();
    }

    // Task B - Defines one required route for path comparison.
    private static class RouteDefinition {
        private final String caseName;
        private final String start;
        private final String destination;
        private final List<String> viaIds;

        private RouteDefinition(String caseName, String start, String destination, List<String> viaIds) {
            this.caseName = caseName;
            this.start = start;
            this.destination = destination;
            this.viaIds = List.copyOf(viaIds);
        }

        private String formatVia() {
            if (viaIds.isEmpty()) {
                return null;
            }
            return String.join(" -> ", viaIds);
        }
    }
}
