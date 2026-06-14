package ui.sorting;

import model.sorting.Candidate;
import model.result.sorting.DatasetBenchmarkResult;
import model.result.sorting.RuntimeStats;
import service.sorting.BenchmarkService;
import service.data.DataService;
import service.export.ResultExportService;
import ui.shared.RoundedSegmentedControl;
import ui.shared.SplitPaneStyle;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import java.util.Map;

// Task A UI - Runs benchmarks and displays sorting results.
public class TaskAPanel extends JPanel {
    private static final Color PAGE_BACKGROUND = Color.WHITE;
    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color TITLE_COLOR = new Color(34, 43, 56);

    private final BenchmarkService benchmarkService;
    private final ResultExportService resultExportService;
    private final JComboBox<String> datasetComboBox;
    private final JButton runBenchmarkButton;
    private final JTable candidateTable;
    private final DefaultTableModel candidateTableModel;
    private final JTextArea statsTextArea;
    private final TaskAVisualizationPanel visualizationPanel;

    public TaskAPanel(
            BenchmarkService benchmarkService,
            DataService dataService,
            ResultExportService resultExportService
    ) {
        this.benchmarkService = benchmarkService;
        this.resultExportService = resultExportService;

        setLayout(new BorderLayout(14, 14));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(PAGE_BACKGROUND);

        datasetComboBox = new JComboBox<>(dataService.getDatasetNames().toArray(String[]::new));
        runBenchmarkButton = new JButton("Run Benchmark");
        add(buildHeaderPanel(), BorderLayout.NORTH);

        candidateTableModel = new DefaultTableModel(new Object[]{"Rank", "Location", "Priority Score"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        candidateTable = new JTable(candidateTableModel);
        candidateTable.setRowHeight(24);
        candidateTable.setFillsViewportHeight(true);
        candidateTable.setFocusable(false);
        candidateTable.setShowGrid(false);
        candidateTable.setIntercellSpacing(new Dimension(0, 0));
        candidateTable.setBorder(BorderFactory.createEmptyBorder());
        JTableHeader candidateTableHeader = candidateTable.getTableHeader();
        candidateTableHeader.setBorder(BorderFactory.createEmptyBorder());
        candidateTableHeader.setBackground(PANEL_BACKGROUND);
        candidateTableHeader.setReorderingAllowed(false);
        candidateTableHeader.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column
            ) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column
                );
                label.setBorder(BorderFactory.createEmptyBorder());
                label.setBackground(PANEL_BACKGROUND);
                label.setForeground(Color.BLACK);
                label.setHorizontalAlignment(SwingConstants.LEFT);
                return label;
            }
        });

        statsTextArea = new JTextArea();
        statsTextArea.setEditable(false);
        statsTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        statsTextArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        visualizationPanel = new TaskAVisualizationPanel();

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                buildTablePanel(),
                buildRightPanel()
        );
        splitPane.setResizeWeight(0.36);
        SplitPaneStyle.apply(splitPane);
        add(splitPane, BorderLayout.CENTER);

        statsTextArea.setText(buildInitialMessage());
        runBenchmarkButton.addActionListener(event -> runBenchmark());
    }

    private JPanel buildHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(16, 0));
        headerPanel.setBackground(PANEL_BACKGROUND);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel titleLabel = new JLabel("Task A Sorting Benchmark");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(TITLE_COLOR);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlsPanel.setOpaque(false);
        controlsPanel.add(new JLabel("Dataset"));
        controlsPanel.add(datasetComboBox);
        controlsPanel.add(runBenchmarkButton);
        headerPanel.add(controlsPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel titleLabel = new JLabel("Top 10 Ranking");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 15f));
        titleLabel.setForeground(TITLE_COLOR);
        panel.add(titleLabel, BorderLayout.NORTH);

        JScrollPane tableScrollPane = new JScrollPane(candidateTable);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableScrollPane.setBackground(PANEL_BACKGROUND);
        tableScrollPane.getViewport().setBackground(PANEL_BACKGROUND);
        tableScrollPane.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                updateCandidateTableRowHeight(tableScrollPane);
            }
        });
        panel.add(tableScrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void updateCandidateTableRowHeight(JScrollPane tableScrollPane) {
        int viewportHeight = tableScrollPane.getViewport().getExtentSize().height;
        if (viewportHeight <= 0) {
            return;
        }

        int rowCount = Math.max(BenchmarkService.TOP_N, candidateTable.getRowCount());
        int rowHeight = Math.max(24, viewportHeight / rowCount);
        if (candidateTable.getRowHeight() != rowHeight) {
            candidateTable.setRowHeight(rowHeight);
        }
    }

    private JPanel buildRightPanel() {
        CardLayout cardLayout = new CardLayout();
        JPanel cardsPanel = new JPanel(cardLayout);
        cardsPanel.setBackground(PAGE_BACKGROUND);
        cardsPanel.setBorder(BorderFactory.createEmptyBorder());

        JScrollPane statsScrollPane = new JScrollPane(statsTextArea);
        statsScrollPane.setBorder(BorderFactory.createEmptyBorder());
        statsScrollPane.getViewport().setBackground(PAGE_BACKGROUND);
        cardsPanel.add(visualizationPanel, "Charts");
        cardsPanel.add(statsScrollPane, "Stats");

        JToggleButton chartsButton = new JToggleButton("Charts");
        JToggleButton statsButton = new JToggleButton("Stats");
        chartsButton.addActionListener(event -> {
            cardLayout.show(cardsPanel, "Charts");
            RoundedSegmentedControl.select(chartsButton, statsButton);
        });
        statsButton.addActionListener(event -> {
            cardLayout.show(cardsPanel, "Stats");
            RoundedSegmentedControl.select(statsButton, chartsButton);
        });
        RoundedSegmentedControl.select(chartsButton, statsButton);

        JPanel switchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        switchPanel.setBackground(PAGE_BACKGROUND);
        switchPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        switchPanel.add(new RoundedSegmentedControl(17, new Dimension(106, 30), chartsButton, statsButton));

        return new JPanel(new BorderLayout()) {
            {
                setBackground(PAGE_BACKGROUND);
                setBorder(BorderFactory.createEmptyBorder());
                add(switchPanel, BorderLayout.NORTH);
                add(cardsPanel, BorderLayout.CENTER);
            }
        };
    }

    private void runBenchmark() {
        String datasetName = (String) datasetComboBox.getSelectedItem();
        setButtonsEnabled(false);
        statsTextArea.setText("Running Task A benchmark and saving result files...");

        SwingWorker<List<DatasetBenchmarkResult>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<DatasetBenchmarkResult> doInBackground() throws Exception {
                List<DatasetBenchmarkResult> results = benchmarkService.runAllDatasetBenchmarks();
                saveTaskAFiles(results);
                return results;
            }

            @Override
            protected void done() {
                try {
                    List<DatasetBenchmarkResult> results = get();
                    DatasetBenchmarkResult selectedResult = findResult(results, datasetName);
                    updateCandidateTable(selectedResult.topCandidates());
                    visualizationPanel.setResult(selectedResult);
                    statsTextArea.setText(buildBenchmarkSummary(selectedResult));
                } catch (Exception exception) {
                    showError(exception);
                } finally {
                    setButtonsEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void saveTaskAFiles(List<DatasetBenchmarkResult> results) throws Exception {
        for (DatasetBenchmarkResult result : results) {
            String fileName = switch (result.datasetName()) {
                case "Dataset A" -> "datasetA_results.txt";
                case "Dataset B" -> "datasetB_results.txt";
                case "Dataset C" -> "datasetC_results.txt";
                default -> throw new IllegalArgumentException("Unknown dataset: " + result.datasetName());
            };
            resultExportService.saveTaskAResults(result, fileName);
        }
        resultExportService.saveTaskASummaryCsv(results, "taskA_runtime_summary.csv");
        resultExportService.saveTaskARawRuntimeCsv(results, "taskA_runtime_samples.csv");
    }

    private DatasetBenchmarkResult findResult(List<DatasetBenchmarkResult> results, String datasetName) {
        for (DatasetBenchmarkResult result : results) {
            if (result.datasetName().equals(datasetName)) {
                return result;
            }
        }
        throw new IllegalArgumentException("Missing benchmark result for " + datasetName);
    }

    private void updateCandidateTable(List<Candidate> candidates) {
        candidateTableModel.setRowCount(0);
        int limit = Math.min(BenchmarkService.TOP_N, candidates.size());
        for (int index = 0; index < limit; index++) {
            Candidate candidate = candidates.get(index);
            candidateTableModel.addRow(new Object[]{
                    index + 1,
                    candidate.getLocationId(),
                    String.format("%.0f", candidate.getPriorityScore())
            });
        }
    }

    private String buildBenchmarkSummary(DatasetBenchmarkResult result) {
        StringBuilder builder = new StringBuilder();
        builder.append("Task A Benchmark").append(System.lineSeparator());
        builder.append("Dataset: ").append(result.datasetName()).append(System.lineSeparator());
        builder.append("Size: ").append(result.datasetSize()).append(System.lineSeparator());
        builder.append("Runs per algorithm: ").append(BenchmarkService.RUNS_PER_ALGORITHM).append(System.lineSeparator());
        builder.append("Project ranking flow: fixed to Merge Sort").append(System.lineSeparator());
        builder.append("Saved files: datasetA/B/C results, taskA_runtime_summary.csv, taskA_runtime_samples.csv")
                .append(System.lineSeparator());
        builder.append(System.lineSeparator());

        for (Map.Entry<String, RuntimeStats> entry : result.runtimes().entrySet()) {
            RuntimeStats stats = entry.getValue();
            builder.append(entry.getKey()).append(System.lineSeparator());
            builder.append("  Average: ").append(formatMs(stats.averageMs())).append(System.lineSeparator());
            builder.append("  Median: ").append(formatMs(stats.medianMs())).append(System.lineSeparator());
            builder.append("  Best: ").append(formatMs(stats.minMs())).append(System.lineSeparator());
            builder.append("  Worst: ").append(formatMs(stats.maxMs())).append(System.lineSeparator());
            builder.append("  Range: ").append(formatMs(stats.rangeMs())).append(System.lineSeparator());
            builder.append("  Std dev: ").append(formatMs(stats.standardDeviationMs())).append(System.lineSeparator());
            builder.append(System.lineSeparator());
        }

        builder.append("Top ").append(result.topCandidates().size()).append(":").append(System.lineSeparator());
        for (int index = 0; index < result.topCandidates().size(); index++) {
            Candidate candidate = result.topCandidates().get(index);
            builder.append(index + 1)
                    .append(". ")
                    .append(candidate.getLocationId())
                    .append(" (priorityScore=")
                    .append(String.format("%.0f", candidate.getPriorityScore()))
                    .append(")")
                    .append(System.lineSeparator());
        }
        return builder.toString();
    }

    private String buildInitialMessage() {
        StringBuilder builder = new StringBuilder();
        builder.append("Task A").append(System.lineSeparator());
        builder.append("1. Choose a dataset.").append(System.lineSeparator());
        builder.append("2. Click 'Run Benchmark' to compare Bubble Sort, Insertion Sort, Quick Sort, and Merge Sort.")
                .append(System.lineSeparator());
        builder.append(System.lineSeparator());
        builder.append("The project ranking flow is fixed to Merge Sort.").append(System.lineSeparator());
        builder.append("This panel shows the benchmark comparison and the final Top 10 ranking for the selected dataset.")
                .append(System.lineSeparator());
        return builder.toString();
    }

    private String formatMs(double value) {
        return String.format("%.3f ms", value);
    }

    private void setButtonsEnabled(boolean enabled) {
        runBenchmarkButton.setEnabled(enabled);
    }

    private void showError(Exception exception) {
        statsTextArea.setText("Failed to run Task A.\n" + exception.getMessage());
        JOptionPane.showMessageDialog(this, exception.getMessage(), "Task A Error", JOptionPane.ERROR_MESSAGE);
    }
}
