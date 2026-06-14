package ui.shared;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;

// Gives Swing split panes a subtle macOS-style divider with a small drag handle.
// Shared UI - Applies consistent styling to split panes.
public final class SplitPaneStyle {
    private static final Color DIVIDER_LINE = new Color(224, 227, 233);
    private static final Color HANDLE = new Color(183, 189, 199);

    private SplitPaneStyle() {
    }

    public static void apply(JSplitPane splitPane) {
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setDividerSize(14);
        splitPane.setContinuousLayout(true);
        splitPane.setOpaque(false);
        splitPane.setUI(new BasicSplitPaneUI() {
            @Override
            public BasicSplitPaneDivider createDefaultDivider() {
                return new RoundedDivider(this);
            }
        });
    }

    private static final class RoundedDivider extends BasicSplitPaneDivider {
        private RoundedDivider(BasicSplitPaneUI ui) {
            super(ui);
            setBorder(BorderFactory.createEmptyBorder());
        }

        @Override
        public void setBorder(Border border) {
            super.setBorder(BorderFactory.createEmptyBorder());
        }

        @Override
        public void paint(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int centerX = width / 2;

            g2.setColor(DIVIDER_LINE);
            g2.drawLine(centerX, 12, centerX, height - 12);

            int handleWidth = 4;
            int handleHeight = 42;
            int handleX = centerX - handleWidth / 2;
            int handleY = Math.max(14, (height - handleHeight) / 2);
            g2.setColor(HANDLE);
            g2.fillRoundRect(handleX, handleY, handleWidth, handleHeight, handleWidth, handleWidth);

            g2.dispose();
        }
    }
}
