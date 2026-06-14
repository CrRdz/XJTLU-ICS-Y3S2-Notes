package ui.path;

import model.graph.Graph;
import model.result.path.PathResult;
import util.FormatUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// Task B UI - Draws a clear schematic of the selected route.
public class PathVisualizationPanel extends JPanel {
    private static final Color BACKGROUND = Color.WHITE;
    private static final Color CARD_BACKGROUND = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(35, 44, 59);
    private static final Color MUTED_TEXT_COLOR = new Color(94, 108, 128);
    private static final Color PATH_COLOR = new Color(214, 39, 70);
    private static final Color PATH_SHADOW_COLOR = new Color(255, 255, 255, 235);
    private static final Color START_COLOR = new Color(36, 133, 91);
    private static final Color END_COLOR = new Color(38, 102, 194);
    private static final Color VIA_COLOR = new Color(249, 173, 52);
    private static final Color NORMAL_NODE_COLOR = new Color(103, 116, 137);
    private static final int NODE_DIAMETER = 34;
    private static final int MAX_NODES_PER_ROW = 6;

    private Graph graph;
    private PathResult pathResult;

    public PathVisualizationPanel() {
        setBackground(BACKGROUND);
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
        repaint();
    }

    public void setPathResult(PathResult pathResult) {
        this.pathResult = pathResult;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawSurface(g2);
        drawHeader(g2);

        if (graph == null || graph.getVertices().isEmpty()) {
            drawCenteredHint(g2, "Graph not loaded.");
            g2.dispose();
            return;
        }

        drawGraphOverview(g2);

        if (pathResult == null || !pathResult.pathExists()) {
            drawCenteredHint(g2, "Run a Task B case to display the Dijkstra route.");
            g2.dispose();
            return;
        }

        List<String> path = pathResult.getPath();
        List<Point> positions = buildRouteLayout(path.size());
        drawRouteLines(g2, path, positions);
        drawRouteNodes(g2, path, positions);
        drawRouteSummary(g2, path);

        g2.dispose();
    }

    private void drawSurface(Graphics2D g2) {
        int x = 18;
        int y = 16;
        int width = getWidth() - 36;
        int height = getHeight() - 32;
        g2.setColor(CARD_BACKGROUND);
        g2.fillRect(x, y, width, height);
    }

    private void drawHeader(Graphics2D g2) {
        g2.setColor(TEXT_COLOR);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 17f));
        g2.drawString("Task B Dijkstra Route", 38, 48);

    }

    private void drawGraphOverview(Graphics2D g2) {
        int edges = graph.getAdjacencyList().values().stream().mapToInt(List::size).sum() / 2;
        String overview = graph.getVertices().size() + " nodes  |  " + edges + " paths  |  weighted graph";
        g2.setColor(MUTED_TEXT_COLOR);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
        g2.drawString(overview, getWidth() - 338, 57);
    }

    private List<Point> buildRouteLayout(int nodeCount) {
        List<Point> positions = new ArrayList<>();
        int left = 78;
        int right = getWidth() - 78;
        int top = 130;
        int rowGap = Math.max(96, (getHeight() - 260) / Math.max(1, (int) Math.ceil(nodeCount / 6.0)));

        for (int index = 0; index < nodeCount; index++) {
            int row = index / MAX_NODES_PER_ROW;
            int indexInRow = index % MAX_NODES_PER_ROW;
            int nodesThisRow = Math.min(MAX_NODES_PER_ROW, nodeCount - row * MAX_NODES_PER_ROW);
            double ratio = nodesThisRow == 1 ? 0.5 : indexInRow / (double) (nodesThisRow - 1);
            if (row % 2 == 1) {
                ratio = 1.0 - ratio;
            }
            int x = left + (int) Math.round((right - left) * ratio);
            int y = top + row * rowGap;
            positions.add(new Point(x, y));
        }
        return positions;
    }

    private void drawRouteLines(Graphics2D g2, List<String> path, List<Point> positions) {
        drawPolyline(g2, positions, PATH_SHADOW_COLOR, 12f);
        drawPolyline(g2, positions, PATH_COLOR, 5f);

        g2.setColor(PATH_COLOR);
        for (int index = 0; index < path.size() - 1; index++) {
            drawArrow(g2, positions.get(index), positions.get(index + 1));
        }
    }

    private void drawPolyline(Graphics2D g2, List<Point> positions, Color color, float width) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int index = 0; index < positions.size() - 1; index++) {
            Point from = positions.get(index);
            Point to = positions.get(index + 1);
            g2.drawLine(from.x, from.y, to.x, to.y);
        }
    }

    private void drawArrow(Graphics2D g2, Point from, Point to) {
        double angle = Math.atan2(to.y - from.y, to.x - from.x);
        int centerX = (from.x + to.x) / 2;
        int centerY = (from.y + to.y) / 2;
        int arrowLength = 13;
        int arrowWidth = 7;
        Polygon arrow = new Polygon();
        arrow.addPoint(centerX, centerY);
        arrow.addPoint(
                centerX - (int) (arrowLength * Math.cos(angle) - arrowWidth * Math.sin(angle)),
                centerY - (int) (arrowLength * Math.sin(angle) + arrowWidth * Math.cos(angle))
        );
        arrow.addPoint(
                centerX - (int) (arrowLength * Math.cos(angle) + arrowWidth * Math.sin(angle)),
                centerY - (int) (arrowLength * Math.sin(angle) - arrowWidth * Math.cos(angle))
        );
        g2.fillPolygon(arrow);
    }

    private void drawRouteNodes(Graphics2D g2, List<String> path, List<Point> positions) {
        String startId = path.get(0);
        String endId = path.get(path.size() - 1);
        FontMetrics metrics = g2.getFontMetrics();

        for (int index = 0; index < path.size(); index++) {
            String nodeId = path.get(index);
            Point point = positions.get(index);
            boolean start = nodeId.equals(startId);
            boolean end = nodeId.equals(endId);
            boolean repeated = path.indexOf(nodeId) != index;

            g2.setColor(start ? START_COLOR : end ? END_COLOR : repeated ? VIA_COLOR : NORMAL_NODE_COLOR);
            g2.fillOval(point.x - NODE_DIAMETER / 2, point.y - NODE_DIAMETER / 2, NODE_DIAMETER, NODE_DIAMETER);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(point.x - NODE_DIAMETER / 2, point.y - NODE_DIAMETER / 2, NODE_DIAMETER, NODE_DIAMETER);

            String order = String.valueOf(index + 1);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12f));
            g2.drawString(order, point.x - metrics.stringWidth(order) / 2, point.y + 5);

            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
            g2.setColor(TEXT_COLOR);
            int labelX = point.x - metrics.stringWidth(nodeId) / 2;
            g2.drawString(nodeId, labelX, point.y + 32);
        }
    }

    private void drawRouteSummary(Graphics2D g2, List<String> path) {
        int bottom = getHeight() - 70;
        String distance = FormatUtils.formatCost(pathResult.getDistance());
        String summary = "Route nodes: " + path.size() + "  |  Total distance: " + distance
                + "  |  Start: " + path.get(0) + "  |  End: " + path.get(path.size() - 1);

        g2.setColor(TEXT_COLOR);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 13f));
        g2.drawString(summary, 54, bottom + 4);
        drawLegend(g2, 54, bottom + 30);
    }

    private void drawLegend(Graphics2D g2, int x, int y) {
        int itemGap = 88;

        drawLegendItem(g2, x, y, START_COLOR, "Start");
        drawLegendItem(g2, x + itemGap, y, NORMAL_NODE_COLOR, "Intermediate");
        drawLegendItem(g2, x + itemGap * 2 + 18, y, VIA_COLOR, "Via");
        drawLegendItem(g2, x + itemGap * 3 + 36, y, END_COLOR, "End");
    }

    private void drawLegendItem(Graphics2D g2, int x, int y, Color color, String label) {
        int dotSize = 11;
        g2.setColor(color);
        g2.fillOval(x, y - dotSize + 2, dotSize, dotSize);

        g2.setColor(MUTED_TEXT_COLOR);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
        g2.drawString(label, x + 16, y + 1);
    }

    private void drawCenteredHint(Graphics2D g2, String text) {
        FontMetrics metrics = g2.getFontMetrics();
        int x = (getWidth() - metrics.stringWidth(text)) / 2;
        int y = getHeight() / 2;
        g2.setColor(MUTED_TEXT_COLOR);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
        g2.drawString(text, x, y);
    }

}
