package com.wolfycz1.astradeck.ui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.wolfycz1.astradeck.logic.MediaManager;
import com.wolfycz1.astradeck.model.Flashcard;
import com.wolfycz1.astradeck.model.ImageCard;
import com.wolfycz1.astradeck.model.TextCard;
import com.wolfycz1.astradeck.util.Constants;

import javax.swing.*;
import java.awt.*;

public class CardViewPanel extends JPanel {
    private final MediaManager mediaManager;

    private final JLabel frontTextLabel;
    private final JLabel frontImageLabel;
    private final JLabel backTextLabel;

    public CardViewPanel(MediaManager mediaManager) {
        this.mediaManager = mediaManager;

        this.setLayout(new GridBagLayout());
        this.putClientProperty(FlatClientProperties.STYLE_CLASS, "CardViewPanel");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        gbc.weighty = 0.0;

        JPanel wrapperPanel = new JPanel();
        wrapperPanel.setLayout(new BoxLayout(wrapperPanel, BoxLayout.Y_AXIS));
        wrapperPanel.setOpaque(false);

        wrapperPanel.add(Box.createVerticalStrut(10));

        frontTextLabel = new JLabel();
        frontTextLabel.setAlignmentX(CENTER_ALIGNMENT);
        frontTextLabel.putClientProperty(FlatClientProperties.STYLE_CLASS, "frontTextLabel");
        wrapperPanel.add(frontTextLabel);

        wrapperPanel.add(Box.createVerticalStrut(10));

        frontImageLabel = new JLabel();
        frontImageLabel.setAlignmentX(CENTER_ALIGNMENT);
        frontImageLabel.setVisible(false);
        wrapperPanel.add(frontImageLabel);

        this.add(wrapperPanel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(15, 0, 15, 0);

        this.add(new JSeparator(SwingConstants.HORIZONTAL), gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, 0);

        backTextLabel = new JLabel();
        backTextLabel.setHorizontalAlignment(SwingConstants.CENTER);
        backTextLabel.setVisible(false);
        backTextLabel.putClientProperty(FlatClientProperties.STYLE_CLASS, "backTextLabel");
        this.add(backTextLabel, gbc);

        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        this.add(Box.createVerticalGlue(), gbc);
    }

    public void setCard(Flashcard card) {
        resetCard();

        if (card instanceof TextCard textCard) {
            frontTextLabel.setText(textCard.getFront().getText());
            backTextLabel.setText(textCard.getBack().getText());

        } else if (card instanceof ImageCard imageCard) {
            if (imageCard.getFront().getText() != null) {
                frontTextLabel.setText(imageCard.getFront().getText());
            }
            if (imageCard.getFront().getImage() != null) {
                frontImageLabel.setIcon(mediaManager.getImageIcon(imageCard.getFront().getImage(),
                        Constants.MAX_CARD_IMAGE_WIDTH, Constants.MAX_CARD_IMAGE_HEIGHT));
                frontImageLabel.setVisible(true);
            }
            backTextLabel.setText(imageCard.getBack().getText());
        }
    }

    public void showBack() {
        backTextLabel.setVisible(true);
        this.revalidate();
        this.repaint();
    }

    private void resetCard() {
        frontTextLabel.setText("");
        frontImageLabel.setIcon(null);
        frontImageLabel.setVisible(false);
        backTextLabel.setText("");
        backTextLabel.setVisible(false);
    }
}
