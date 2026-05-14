package com.wolfycz1.astradeck.ui.editors;

import com.formdev.flatlaf.FlatClientProperties;
import com.wolfycz1.astradeck.model.ImageCard;
import com.wolfycz1.astradeck.model.Media;
import com.wolfycz1.astradeck.model.sides.ImageSide;
import com.wolfycz1.astradeck.model.sides.TextSide;
import com.wolfycz1.astradeck.storage.MediaStorageService;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

@Slf4j
public class ImageCardEditor implements FlashcardEditor<ImageCard> {
    private final JPanel panel;
    private final JTextArea frontText, backText;
    private final JLabel imagePreview;
    private Runnable changeListener;

    private final MediaStorageService mediaStorageService;
    private Media currentMedia;

    public ImageCardEditor(MediaStorageService mediaStorageService) {
        this.mediaStorageService = mediaStorageService;

        panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel imageHeader = new JPanel(new BorderLayout());
        imageHeader.setBorder(BorderFactory.createTitledBorder("Image attachment"));

        JButton attachImageButton = new JButton("Attach Image");
        attachImageButton.putClientProperty(FlatClientProperties.STYLE_CLASS, "standard");
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

    private void handleImageAttach() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select an image");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Images", "png", "jpg", "jpeg", "gif", "webp"));

        if (fileChooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                this.currentMedia = mediaStorageService.importLocalImage(selectedFile);
                imagePreview.setText("Attached: " + currentMedia.originalName());
                notifyChange();
            } catch (Exception e) {
                log.error("Unexpected error while importing file: {} - {}", selectedFile.getAbsolutePath(), e.getMessage());
                JOptionPane.showMessageDialog(panel, "Failed to process image", "Import Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void notifyChange() {
        if (changeListener != null) changeListener.run();
    }

    @Override
    public Class<ImageCard> getSupportedType() {
        return ImageCard.class;
    }

    @Override
    public String getDisplayName() {
        return "Image Card";
    }

    @Override
    public ImageCard createNewCard() {
        return new ImageCard();
    }

    @Override
    public JPanel getUI() {
        return panel;
    }

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

    @Override
    public void saveTo(ImageCard card) {
        if (card.getFront() == null) card.setFront(new ImageSide());
        card.getFront().setText(frontText.getText());
        card.getFront().setImage(this.currentMedia);

        if (card.getBack() == null) card.setBack(new TextSide());
        card.getBack().setText(backText.getText());
    }

    @Override
    public void setChangeListener(Runnable onChange) {
        this.changeListener = onChange;
    }

    @Override
    public void requestFocus() {
        if (frontText != null) {
            frontText.requestFocusInWindow();
        }
    }
}
