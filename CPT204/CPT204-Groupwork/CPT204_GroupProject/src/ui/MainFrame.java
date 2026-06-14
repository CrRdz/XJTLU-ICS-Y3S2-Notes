package ui;

import service.sorting.BenchmarkService;
import service.data.DataService;
import service.path.GraphService;
import service.path.PathComparisonService;
import service.export.ResultExportService;
import service.sorting.SortingService;
import ui.path.ShortestPathPanel;
import ui.shared.RoundedSegmentedControl;
import ui.sorting.TaskAPanel;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

// Shared UI - Hosts the Task A benchmark view and Task B path view.
public class MainFrame extends JFrame {
    private static final Color BACKGROUND = Color.WHITE;
    private static final String SORTING_VIEW = "Task A";
    private static final String SHORTEST_PATH_VIEW = "Shortest Path";

    public MainFrame() throws IOException {
        super("CPT204 Group Project");

        DataService dataService = new DataService();
        SortingService sortingService = new SortingService(dataService);
        BenchmarkService benchmarkService = new BenchmarkService(dataService, sortingService);
        GraphService graphService = new GraphService();
        PathComparisonService pathComparisonService = new PathComparisonService(sortingService, graphService);
        ResultExportService resultExportService = new ResultExportService();

        CardLayout cardLayout = new CardLayout();
        JPanel contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BACKGROUND);
        contentPanel.add(new TaskAPanel(benchmarkService, dataService, resultExportService), SORTING_VIEW);
        contentPanel.add(new ShortestPathPanel(pathComparisonService, dataService, resultExportService), SHORTEST_PATH_VIEW);

        JToggleButton taskAButton = new JToggleButton("Task A");
        JToggleButton shortestPathButton = new JToggleButton("Task B");
        taskAButton.addActionListener(event -> {
            cardLayout.show(contentPanel, SORTING_VIEW);
            RoundedSegmentedControl.select(taskAButton, shortestPathButton);
        });
        shortestPathButton.addActionListener(event -> {
            cardLayout.show(contentPanel, SHORTEST_PATH_VIEW);
            RoundedSegmentedControl.select(shortestPathButton, taskAButton);
        });
        RoundedSegmentedControl.select(taskAButton, shortestPathButton);

        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        navigationPanel.setBackground(BACKGROUND);
        navigationPanel.add(new RoundedSegmentedControl(18, new Dimension(104, 32), taskAButton, shortestPathButton));

        setLayout(new BorderLayout());
        add(navigationPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1200, 760);
        setLocationRelativeTo(null);
    }
}
