package com.wolfycz1.astradeck.ui.editors;

import com.wolfycz1.astradeck.model.ImageCard;
import com.wolfycz1.astradeck.model.Media;
import com.wolfycz1.astradeck.model.sides.ImageSide;
import com.wolfycz1.astradeck.model.sides.TextSide;
import com.wolfycz1.astradeck.storage.MediaStorageService;
import com.wolfycz1.astradeck.ui.util.ImageProvider;
import com.wolfycz1.astradeck.util.Constants;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.concurrent.CompletableFuture;

/**
 * An editor panel that allows attaching an image to the flashcard
 * @author wolfycz1
 */
@Slf4j
public class ImageCardEditor implements FlashcardEditor<ImageCard> {
    private final JPanel panel;
    private final JTextArea frontText, backText;
    private final JLabel imagePreview;
    private Runnable changeListener;

    private final MediaStorageService mediaStorageService;
    private final ImageProvider imageProvider;
    private Media currentMedia;

    /**
     * Sets up the editor view and its document listener
     */
    public ImageCardEditor(MediaStorageService mediaStorageService, ImageProvider imageProvider) {
        this.mediaStorageService = mediaStorageService;
        this.imageProvider = imageProvider;

        panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel imageHeader = new JPanel(new BorderLayout());
        imageHeader.setBorder(BorderFactory.createTitledBorder("Image attachment"));

        JButton attachImageButton = new JButton("Attach Image");
        attachImageButton.setFocusable(false);
        attachImageButton.addActionListener(_ -> handleImageAttach());
        imageHeader.add(attachImageButton, BorderLayout.EAST);

        imagePreview = new JLabel("No image selected");
        imagePreview.setHorizontalAlignment(SwingConstants.CENTER);
        imageHeader.add(imagePreview, BorderLayout.CENTER);

        frontText = new JTextArea();
        backText = new JTextArea();

        JScrollPane frontScroll = new JScrollPane(frontText);
        JScrollPane backScroll = new JScrollPane(backText);

        frontScroll.setBorder(BorderFactory.createTitledBorder("Front (Question)"));
        backScroll.setBorder(BorderFactory.createTitledBorder("Back (Answer)"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, frontScroll, backScroll);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(null);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.add(imageHeader, BorderLayout.NORTH);
        centerPanel.add(splitPane, BorderLayout.CENTER);
        panel.add(centerPanel, BorderLayout.CENTER);

        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                notifyChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                notifyChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                notifyChange();
            }
        };

        frontText.getDocument().addDocumentListener(documentListener);
        backText.getDocument().addDocumentListener(documentListener);
    }

    /**
     * Opens a file dialog to let the user select an image,
     * imports it to local storage, and scales it in the background
     */
    private void handleImageAttach() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select an image");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Images", "png", "jpg", "jpeg", "gif", "webp"));

        if (fileChooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                this.currentMedia = mediaStorageService.importLocalImage(selectedFile);
                CompletableFuture.runAsync(() -> {
                    log.info("Starting background image scale");
                    imageProvider.preloadIcon(currentMedia, Constants.MAX_CARD_IMAGE_WIDTH, Constants.MAX_CARD_IMAGE_HEIGHT);
                    log.info("Background image scale cached.");
                });

                imagePreview.setText("Attached: " + currentMedia.originalName());
                notifyChange();
            } catch (Exception e) {
                log.error("Unexpected error while importing file: {} - {}", selectedFile.getAbsolutePath(), e.getMessage());
                JOptionPane.showMessageDialog(panel, "Failed to process image", "Import Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Helper method to run the change listener whenever the text or image is modified. */
    private void notifyChange() {
        if (changeListener != null) changeListener.run();
    }

    /**
     * Returns the class type this editor supports
     * @return {@link ImageCard} class
     */
    @Override
    public Class<ImageCard> getSupportedType() {
        return ImageCard.class;
    }

    /**
     * Returns the display name of the editor
     * @return "Image Card"
     */
    @Override
    public String getDisplayName() {
        return "Image Card";
    }

    /**
     * Instantiates a new card of the supported type
     * @return new {@link ImageCard}
     */
    @Override
    public ImageCard createNewCard() {
        return new ImageCard();
    }

    /**
     * Returns the main {@link JPanel} containing the editor
     * @return main {@link JPanel}
     */
    @Override
    public JPanel getUI() {
        return panel;
    }

    /**
     * Populates the editor fields with the card data
     * @param card the card data
     */
    @Override
    public void populate(ImageCard card) {
        if (card.getFront() != null) {
            frontText.setText(card.getFront().getText());
            this.currentMedia = card.getFront().getImage();

            if (card.getFront().getImage() != null) {
                imagePreview.setText("Attached: " + card.getFront().getImage().originalName());
            } else {
                imagePreview.setText("No image selected");
            }
        } else {
            frontText.setText("");
            this.currentMedia = null;
            imagePreview.setText("No image selected");
        }

        if (card.getBack() != null) {
            backText.setText(card.getBack().getText());
        } else {
            backText.setText("");
        }
    }

    /**
     * Saves the editor fields to the card
     * @param card the {@link ImageCard} object to save to
     */
    @Override
    public void saveTo(ImageCard card) {
        if (card.getFront() == null) card.setFront(new ImageSide());
        card.getFront().setText(frontText.getText());
        card.getFront().setImage(this.currentMedia);

        if (card.getBack() == null) card.setBack(new TextSide());
        card.getBack().setText(backText.getText());
    }

    /**
     * Registers a callback on whenever a change is made
     * @param onChange {@link Runnable} listener
     */
    @Override
    public void setChangeListener(Runnable onChange) {
        this.changeListener = onChange;
    }

    /**
     * Puts the cursor in the primary field of the editor
     */
    @Override
    public void requestFocus() {
        if (frontText != null) {
            frontText.requestFocusInWindow();
        }
    }
}
