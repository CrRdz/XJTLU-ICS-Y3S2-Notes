package ui.path;

import model.graph.Graph;
import model.result.path.PathComparisonResult;
import model.result.path.PathResult;
import model.result.path.PathRunResult;
import service.data.DataService;
import service.path.PathComparisonService;
import service.export.ResultExportService;
import ui.shared.RoundedSegmentedControl;
import ui.shared.SplitPaneStyle;
import util.FormatUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;

// Task B UI - Runs required route comparisons and displays path details.
public class ShortestPathPanel extends JPanel {
    private static final Color PAGE_BACKGROUND = Color.WHITE;
    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color TITLE_COLOR = new Color(34, 43, 56);

    private final PathComparisonService pathComparisonService;
    private final Graph graph;
    private final ResultExportService resultExportService;
    private final JComboBox<String> caseComboBox;
    private final JButton runCaseButton;
    private final JTextArea resultTextArea;
    private final PathVisualizationPanel pathVisualizationPanel;
    private final PathComparisonVisualizationPanel comparisonVisualizationPanel;

    public ShortestPathPanel(
            PathComparisonService pathComparisonService,
            DataService dataService,
            ResultExportService resultExportService
    ) {
        this.pathComparisonService = pathComparisonService;
        this.graph = dataService.getGraph();
        this.resultExportService = resultExportService;

        setLayout(new BorderLayout(14, 14));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(PAGE_BACKGROUND);

        caseComboBox = new JComboBox<>(new String[]{"Case 1", "Case 2", "Case 3", "Case 4"});
        runCaseButton = new JButton("Run Path Finding");

        add(buildHeaderPanel(), BorderLayout.NORTH);

        pathVisualizationPanel = new PathVisualizationPanel();
        pathVisualizationPanel.setGraph(graph);
        pathVisualizationPanel.setPreferredSize(new Dimension(820, 620));
        comparisonVisualizationPanel = new PathComparisonVisualizationPanel();

        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        resultTextArea.setLineWrap(true);
        resultTextArea.setWrapStyleWord(true);
        resultTextArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        resultTextArea.setText("Choose a case and click Run Path Finding.");

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                pathVisualizationPanel,
                buildRightPanel()
        );
        splitPane.setResizeWeight(0.64);
        SplitPaneStyle.apply(splitPane);
        add(splitPane, BorderLayout.CENTER);

        runCaseButton.addActionListener(event -> runSelectedCase());
    }

    private JPanel buildHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(16, 0));
        headerPanel.setBackground(PANEL_BACKGROUND);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel titleLabel = new JLabel("Task B Shortest Path");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(TITLE_COLOR);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlsPanel.setOpaque(false);
        controlsPanel.add(new JLabel("Case"));
        controlsPanel.add(caseComboBox);
        controlsPanel.add(runCaseButton);
        headerPanel.add(controlsPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel buildRightPanel() {
        CardLayout cardLayout = new CardLayout();
        JPanel cardsPanel = new JPanel(cardLayout);
        cardsPanel.setBackground(PAGE_BACKGROUND);
        cardsPanel.setBorder(BorderFactory.createEmptyBorder());

        JScrollPane scrollPane = new JScrollPane(resultTextArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(PAGE_BACKGROUND);
        scrollPane.getViewport().setBackground(PAGE_BACKGROUND);
        cardsPanel.add(comparisonVisualizationPanel, "Charts");
        cardsPanel.add(scrollPane, "Details");

        JToggleButton chartsButton = new JToggleButton("Charts");
        JToggleButton detailsButton = new JToggleButton("Details");
        chartsButton.addActionListener(event -> {
            cardLayout.show(cardsPanel, "Charts");
            RoundedSegmentedControl.select(chartsButton, detailsButton);
        });
        detailsButton.addActionListener(event -> {
            cardLayout.show(cardsPanel, "Details");
            RoundedSegmentedControl.select(detailsButton, chartsButton);
        });
        RoundedSegmentedControl.select(chartsButton, detailsButton);

        JPanel switchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        switchPanel.setBackground(PAGE_BACKGROUND);
        switchPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        switchPanel.add(new RoundedSegmentedControl(17, new Dimension(112, 30), chartsButton, detailsButton));

        return new JPanel(new BorderLayout()) {
            {
                setBackground(PAGE_BACKGROUND);
                setBorder(BorderFactory.createEmptyBorder());
                add(switchPanel, BorderLayout.NORTH);
                add(cardsPanel, BorderLayout.CENTER);
            }
        };
    }

    private void runSelectedCase() {
        String caseName = (String) caseComboBox.getSelectedItem();
        runCaseButton.setEnabled(false);
        resultTextArea.setText("Running " + caseName + "...");

        SwingWorker<PathComparisonResult, Void> worker = new SwingWorker<>() {
            @Override
            protected PathComparisonResult doInBackground() throws Exception {
                List<PathComparisonResult> comparisons = pathComparisonService.compareRequiredRoutes(graph);
                List<PathRunResult> dijkstraResults = comparisons.stream()
                        .map(PathComparisonResult::dijkstraResult)
                        .toList();
                resultExportService.saveShortestPathResults(dijkstraResults, "taskB_results.txt");
                resultExportService.savePathComparisonResults(comparisons, "taskB_comparison_results.txt");
                return pathComparisonService.findComparison(caseName, comparisons);
            }

            @Override
            protected void done() {
                try {
                    PathComparisonResult comparison = get();
                    PathResult pathResult = comparison.dijkstraResult().pathResult();
                    pathVisualizationPanel.setPathResult(pathResult);
                    comparisonVisualizationPanel.setComparison(comparison);
                    resultTextArea.setText(buildResultSummary(comparison));
                } catch (Exception exception) {
                    pathVisualizationPanel.setPathResult(null);
                    comparisonVisualizationPanel.setComparison(null);
                    resultTextArea.setText("Failed to run Task B.\n" + exception.getMessage());
                    JOptionPane.showMessageDialog(ShortestPathPanel.this, exception.getMessage(), "Task B Error",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    runCaseButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private String buildResultSummary(PathComparisonResult comparison) {
        StringBuilder builder = new StringBuilder();
        builder.append(comparison.caseName()).append(System.lineSeparator());
        builder.append("Start: ").append(comparison.start()).append(System.lineSeparator());
        builder.append("Destination: ").append(comparison.destination()).append(System.lineSeparator());
        if (comparison.via() != null) {
            builder.append("Via: ").append(comparison.via()).append(System.lineSeparator());
        }
        builder.append(System.lineSeparator());

        appendAlgorithmSection(builder, "Dijkstra", comparison.dijkstraResult());
        builder.append(System.lineSeparator());
        appendAlgorithmSection(builder, "Bellman-Ford", comparison.bellmanFordResult());
        builder.append(System.lineSeparator());
        appendAlgorithmSection(builder, "Floyd-Warshall", comparison.floydWarshallResult());
        return builder.toString();
    }

    private void appendAlgorithmSection(StringBuilder builder, String algorithmName, PathRunResult result) {
        PathResult pathResult = result.pathResult();
        List<String> path = pathResult.getPath();

        builder.append(algorithmName).append(System.lineSeparator());
        builder.append("Nodes in path: ").append(path.size()).append(System.lineSeparator());
        builder.append("Total distance: ").append(FormatUtils.formatCost(pathResult.getDistance()))
                .append(System.lineSeparator());
        builder.append("Runtime: ").append(String.format("%.3f ms", result.runtimeMs())).append(System.lineSeparator());
        builder.append("Path: ").append(String.join(" -> ", path)).append(System.lineSeparator());
    }

}
