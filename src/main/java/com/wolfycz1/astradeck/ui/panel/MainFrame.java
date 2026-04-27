package com.wolfycz1.astradeck.ui.panel;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.wolfycz1.astradeck.algorithm.Sm2Algorithm;
import com.wolfycz1.astradeck.event.RequestStudyEvent;
import com.wolfycz1.astradeck.event.SessionAbortedEvent;
import com.wolfycz1.astradeck.event.SessionFinishedEvent;
import com.wolfycz1.astradeck.logic.MediaManager;
import com.wolfycz1.astradeck.logic.StudySessionManager;
import com.wolfycz1.astradeck.storage.AstraArchiveHandler;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final EventBus eventBus;
    private final MediaManager mediaManager;
    private final AstraArchiveHandler astraArchiveHandler;

    public static final String VIEW_DASHBOARD = "DASHBOARD";
    public static final String VIEW_STUDY = "STUDY";

    private final CardLayout cardLayout;
    private final JPanel mainContainer;

    private StudyPanel currentStudyPanel;

    public MainFrame(EventBus eventBus, MediaManager mediaManager, AstraArchiveHandler astraArchiveHandler) {
        this.eventBus = eventBus;
        this.mediaManager = mediaManager;
        this.astraArchiveHandler = astraArchiveHandler;

        this.eventBus.register(this);

        this.setTitle("AstraDeck");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        DashboardPanel dashboardPanel = new DashboardPanel(eventBus, astraArchiveHandler);
        mainContainer.add(dashboardPanel, VIEW_DASHBOARD);

        this.add(mainContainer);

        cardLayout.show(mainContainer, VIEW_DASHBOARD);
    }

    @Subscribe
    public void onStudyRequest(RequestStudyEvent event) {
        StudySessionManager studySessionManager = new StudySessionManager(event.deck(), new Sm2Algorithm(), eventBus);

        currentStudyPanel = new StudyPanel(studySessionManager, eventBus, mediaManager);

        mainContainer.add(currentStudyPanel, VIEW_STUDY);
        cardLayout.show(mainContainer, VIEW_STUDY);

        studySessionManager.startSession();
    }

    @Subscribe
    public void onSessionAborted(SessionAbortedEvent event) {
        returnToDashboard();
    }

    @Subscribe
    public void onSessionFinished(SessionFinishedEvent event) {
        JOptionPane.showMessageDialog(this, "Session complete.\n"
                + event.totalReviewed() + " cards reviewed.", "Done", JOptionPane.INFORMATION_MESSAGE);
        returnToDashboard();
    }

    private void returnToDashboard() {
        cardLayout.show(mainContainer, VIEW_DASHBOARD);

        if (currentStudyPanel != null) {
            mainContainer.remove(currentStudyPanel);
            currentStudyPanel = null;
        }
    }
}
