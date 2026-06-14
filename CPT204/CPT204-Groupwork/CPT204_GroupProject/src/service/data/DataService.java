package service.data;

import model.sorting.Candidate;
import model.graph.Graph;
import util.FileLoader;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

// Shared - Loads and caches Task A datasets and the Task B graph.
public class DataService {
    private final FileLoader fileLoader;
    private final Map<String, List<Candidate>> datasetCandidates;
    private final Graph graph;

    public DataService() throws IOException {
        this(new FileLoader());
    }

    public DataService(FileLoader fileLoader) throws IOException {
        this.fileLoader = fileLoader;
        this.datasetCandidates = Map.of(
                "Dataset A", List.copyOf(fileLoader.loadCandidatesA()),
                "Dataset B", List.copyOf(fileLoader.loadCandidatesB()),
                "Dataset C", List.copyOf(fileLoader.loadCandidatesC())
        );
        this.graph = fileLoader.loadGraph();
    }

    public List<Candidate> getCandidates(String datasetName) {
        List<Candidate> candidates = datasetCandidates.get(datasetName);
        if (candidates == null) {
            throw new IllegalArgumentException("Unknown dataset: " + datasetName);
        }
        return Collections.unmodifiableList(candidates);
    }

    public Graph getGraph() {
        return graph;
    }

    public List<String> getDatasetNames() {
        return List.of("Dataset A", "Dataset B", "Dataset C");
    }

    public FileLoader getFileLoader() {
        return fileLoader;
    }
}
