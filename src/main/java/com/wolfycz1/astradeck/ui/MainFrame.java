package com.wolfycz1.astradeck.ui;

import com.wolfycz1.astradeck.event.*;

import javax.swing.*;
import java.awt.*;

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
        this.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        this.add(mainContainer);

        cardLayout.show(mainContainer, VIEW_DASHBOARD);
    }

    public void setView(String viewName, JPanel panel) {
        mainContainer.add(panel, viewName);
        cardLayout.show(mainContainer, viewName);
    }

    public void removeView(JPanel panel) {
        if (panel != null) {
            mainContainer.remove(panel);
        }
    }
}
