package oogasalad.engine.view.screen;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.view.ViewState;

public class WinScreen extends GameOverlayScreen {
    private static final Logger LOG = LogManager.getLogger();
    private static final String overlayPropertiesFilepath = "/oogasalad/screens/winScene.properties";
    private static final Properties overlayProperties = new Properties();
    private String overlayStylesheet;
    private ViewState viewState;

    public WinScreen(ViewState viewState) {
        super(viewState);
        try {
            InputStream stream = getClass().getResourceAsStream(overlayPropertiesFilepath);
            overlayProperties.load(stream);
        } catch (IOException e) {
            LOG.warn("Unable to load overlay screen properties");
        }
        String overlayStylesheetFilepath = overlayProperties.getProperty("win.stylesheet");
        overlayStylesheet = Objects.requireNonNull(getClass().getResource(overlayStylesheetFilepath))
                .toExternalForm();
        this.viewState = viewState;
    }

    @Override
    public void initialRender() {
        VBox combinedBox = new VBox();
        VBox textBox = createOverlayTextBox();
        VBox buttonBox = createOverlayButtonBox();
        combinedBox.getChildren().addAll(textBox, buttonBox);
        combinedBox.getStyleClass().add(overlayProperties.getProperty("win.background.style"));
        alignBox(combinedBox, getIntegerValue(overlayProperties, "win.boxSpacing"));
        this.getChildren().add(combinedBox);
        this.getStylesheets().add(overlayStylesheet);
    }

    @Override
    public VBox createOverlayTextBox() {
        VBox textBox = new VBox();
        int wrappingWidth = getIntegerValue(overlayProperties, "win.wrappingWidth");

        // First, extract the "win message" from the properties file
        String winMessage = overlayProperties.getProperty("win.message");
        int winFont = getIntegerValue(overlayProperties, "win.primaryFont");
        Text winText = createStyledText(winMessage, new Font(winFont), wrappingWidth);

        // Then, extract the score message
        String scoreMessage = overlayProperties.getProperty("win.score");
        int scoreFont = getIntegerValue(overlayProperties, "win.secondaryFont");
        Text scoreText = createStyledText(scoreMessage, new Font(scoreFont), wrappingWidth);

        // Finally, combine both text messages together
        textBox.getChildren().addAll(winText, scoreText);
        alignBox(textBox, getIntegerValue(overlayProperties, "win.defaultSpacing"));
        return textBox;
    }

    @Override
    public VBox createOverlayButtonBox() {
        VBox buttonBox = new VBox();
        String[] buttonTexts = getWinButtonTexts();
        String[] buttonIDs = getWinButtonIDs();

        for (int i = 0; i < buttonIDs.length; i++) {
            Button currButton = new Button(buttonTexts[i]);
            currButton.getStyleClass().add(overlayProperties.getProperty("win.button.style"));
            buttonBox.getChildren().add(currButton);
        }

        int buttonSpacing = Integer.parseInt(overlayProperties.getProperty("win.defaultSpacing"));
        alignBox(buttonBox, buttonSpacing);
        return buttonBox;
    }

    /**
     * Provides button texts for the win screen.
     * @return array of strings for button strings
     */
    private String[] getWinButtonTexts() {
        return new String[]{overlayProperties.getProperty("win.button.nextLevel.text"),
                overlayProperties.getProperty("win.button.restartLevel.text"),
                overlayProperties.getProperty("win.button.returnHome.text")
        };
    }

    /**
     * Provides button IDs for the win screen.
     * @return array of strings for button IDs
     */
    private String[] getWinButtonIDs() {
        return new String[]{overlayProperties.getProperty("win.button.nextLevel.id"),
                overlayProperties.getProperty("win.button.restartLevel.id"),
                overlayProperties.getProperty("win.button.returnHome.id")
        };
    }

    @Override
    public void removeGameObjectImage(ImmutableGameObject gameObject) {
        // No need to remove game objects from the win screen, as it is a static overlay
        // that doesn't support adding dynamic objects.
        // This method is intentionally left blank.
    }

}
