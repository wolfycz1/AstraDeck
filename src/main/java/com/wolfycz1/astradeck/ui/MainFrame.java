package com.wolfycz1.astradeck.ui;

import com.wolfycz1.astradeck.event.*;

import javax.swing.*;
import java.awt.*;

/**
 * The primary application window, uses a card layout to swap main screens
 * @author wolfycz1
 */
public class MainFrame extends JFrame {
    public static final String VIEW_DASHBOARD = "DASHBOARD";
    public static final String VIEW_STUDY = "STUDY";
    public static final String VIEW_EDITOR = "EDITOR";

    private final CardLayout cardLayout;
    private final JPanel mainContainer;

    public MainFrame() {
        this.setTitle("AstraDeck");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 600);
        this.setMinimumSize(new Dimension(800, 600));
        this.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        this.add(mainContainer);

        cardLayout.show(mainContainer, VIEW_DASHBOARD);
    }

    /**
     * Mounts a new panel into the container and displays it
     * @param viewName name of the panel
     * @param panel the panel to mount
     */
    public void setView(String viewName, JPanel panel) {
        mainContainer.add(panel, viewName);
        cardLayout.show(mainContainer, viewName);
    }

    /**
     * Unmounts a panel from the container
     * @param panel the panel to unmount
     */
    public void removeView(JPanel panel) {
        if (panel != null) {
            mainContainer.remove(panel);
        }
    }
}
