package com.wolfycz1.astradeck.ui;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.wolfycz1.astradeck.algorithm.Sm2Algorithm;
import com.wolfycz1.astradeck.event.*;
import com.wolfycz1.astradeck.logic.StudySessionManager;
import com.wolfycz1.astradeck.storage.AstraArchiveHandler;
import com.wolfycz1.astradeck.storage.MediaStorageService;
import com.wolfycz1.astradeck.ui.editors.FlashcardEditor;
import com.wolfycz1.astradeck.ui.editors.ImageCardEditor;
import com.wolfycz1.astradeck.ui.editors.TextCardEditor;
import com.wolfycz1.astradeck.ui.panel.DashboardPanel;
import com.wolfycz1.astradeck.ui.panel.EditorPanel;
import com.wolfycz1.astradeck.ui.panel.StudyPanel;
import com.wolfycz1.astradeck.ui.util.ImageProvider;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame {
    private final EventBus eventBus;
    private final MediaStorageService mediaStorageService;
    private final ImageProvider imageProvider;
    private final AstraArchiveHandler astraArchiveHandler;

    public static final String VIEW_DASHBOARD = "DASHBOARD";
    public static final String VIEW_STUDY = "STUDY";
    public static final String VIEW_EDITOR = "EDITOR";

    private final CardLayout cardLayout;
    private final JPanel mainContainer;

    private StudyPanel currentStudyPanel;
    private EditorPanel currentEditorPanel;


    public MainFrame(EventBus eventBus, MediaStorageService mediaStorageService, ImageProvider imageProvider, AstraArchiveHandler astraArchiveHandler) {
        this.eventBus = eventBus;
        this.mediaStorageService = mediaStorageService;
        this.imageProvider = imageProvider;
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

        currentStudyPanel = new StudyPanel(studySessionManager, eventBus, imageProvider);

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

    @Subscribe
    public void onEditorRequest(RequestEditEvent event) {
        List<FlashcardEditor<?>> registeredEditors = List.of(new TextCardEditor(), new ImageCardEditor(mediaStorageService));
        currentEditorPanel = new EditorPanel(event.deck(), eventBus, registeredEditors);

        mainContainer.add(currentEditorPanel, VIEW_EDITOR);
        cardLayout.show(mainContainer, VIEW_EDITOR);
    }

    @Subscribe
    public void onDeckUpdated(DeckUpdatedEvent event) {
        if (currentEditorPanel != null && currentEditorPanel.isShowing()) {
            returnToDashboard();
        }
    }

    private void returnToDashboard() {
        cardLayout.show(mainContainer, VIEW_DASHBOARD);

        if (currentStudyPanel != null) {
            mainContainer.remove(currentStudyPanel);
            currentStudyPanel = null;
        }

        if (currentEditorPanel != null) {
            mainContainer.remove(currentEditorPanel);
            currentEditorPanel = null;
        }
    }
}
