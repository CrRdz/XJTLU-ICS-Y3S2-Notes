package algorithm.path;

import model.graph.Graph;
import model.result.path.PathResult;
import model.graph.Vertex;

// Task B - Common contract for path-finding algorithms.
public interface PathAlgorithm {
    PathResult findPath(Vertex source, Vertex target, Graph graph);
}
