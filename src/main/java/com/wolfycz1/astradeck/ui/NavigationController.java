package com.wolfycz1.astradeck.ui;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.wolfycz1.astradeck.algorithm.Sm2Algorithm;
import com.wolfycz1.astradeck.database.DeckRepositoryService;
import com.wolfycz1.astradeck.event.*;
import com.wolfycz1.astradeck.logic.StudySessionManager;
import com.wolfycz1.astradeck.model.Deck;
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

public class NavigationController {
    private final EventBus eventBus;
    private final MainFrame mainFrame;
    private final MediaStorageService mediaStorageService;
    private final ImageProvider imageProvider;
    private final AstraArchiveHandler astraArchiveHandler;
    private final DeckRepositoryService deckRepositoryService;

    private DashboardPanel dashboardPanel;
    private StudyPanel currentStudyPanel;
    private EditorPanel currentEditorPanel;

    public NavigationController(EventBus eventBus, MainFrame mainFrame, MediaStorageService mediaStorageService,
                                ImageProvider imageProvider, AstraArchiveHandler astraArchiveHandler, DeckRepositoryService deckRepositoryService) {
        this.eventBus = eventBus;
        this.mainFrame = mainFrame;
        this.mediaStorageService = mediaStorageService;
        this.imageProvider = imageProvider;
        this.astraArchiveHandler = astraArchiveHandler;
        this.deckRepositoryService = deckRepositoryService;

        this.eventBus.register(this);
    }

    public void start() {
        List<Deck> savedDecks = deckRepositoryService.loadAllDecks();
        this.dashboardPanel = new DashboardPanel(eventBus, astraArchiveHandler, savedDecks);
        mainFrame.setView(MainFrame.VIEW_DASHBOARD, dashboardPanel);
        mainFrame.setVisible(true);
    }

    @Subscribe
    public void onStudyRequest(RequestStudyEvent event) {
        StudySessionManager studySessionManager = new StudySessionManager(event.deck(), new Sm2Algorithm(), eventBus);
        currentStudyPanel = new StudyPanel(studySessionManager, eventBus, imageProvider);

        mainFrame.setView(MainFrame.VIEW_STUDY, currentStudyPanel);
        studySessionManager.startSession();
    }

    @Subscribe
    public void onSessionAborted(SessionAbortedEvent event) {
        returnToDashboard();
    }

    @Subscribe
    public void onSessionFinished(SessionFinishedEvent event) {
        Window parentWindow = SwingUtilities.getWindowAncestor(mainFrame);
        JOptionPane.showMessageDialog(parentWindow, "Session complete.\n"
                + event.totalReviewed() + " cards reviewed.", "Done", JOptionPane.INFORMATION_MESSAGE);
        returnToDashboard();
    }

    @Subscribe
    public void onEditorRequest(RequestEditEvent event) {
        List<FlashcardEditor<?>> registeredEditors = List.of(new TextCardEditor(),
                new ImageCardEditor(mediaStorageService, imageProvider));
        currentEditorPanel = new EditorPanel(event.deck(), eventBus, registeredEditors);

        mainFrame.setView(MainFrame.VIEW_EDITOR, currentEditorPanel);
    }

    @Subscribe
    public void onReturnToDashboard(ReturnToDashboardEvent event) {
        returnToDashboard();
    }

    private void returnToDashboard() {
        mainFrame.setView(MainFrame.VIEW_DASHBOARD, dashboardPanel);

        if (currentStudyPanel != null) {
            mainFrame.removeView(currentStudyPanel);
            currentStudyPanel = null;
        }

        if (currentEditorPanel != null) {
            mainFrame.removeView(currentEditorPanel);
            currentEditorPanel = null;
        }
    }
}
