package oogasalad.editor.view.panes.chat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Window;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.view.tools.ChatBotApi;
import oogasalad.editor.view.tools.ConfigLoader;

/**
 * ChatBotPane provides a simple UI for sending messages to ChatBotApi and displaying responses.
 */
public class ChatBotPane extends BorderPane {

  private static final Logger LOG = LogManager.getLogger(ChatBotPane.class);
  private static final String CHATBOT_PROPS_FILE = "oogasalad/editor/view/tools/chatbot.properties";

  private final ChatBotApi chatBotApi;
  private final TextArea chatArea;
  private final TextField inputField;
  private final Button sendButton;
  private final EditorController editorController;
  private final Properties chatbotProperties = new Properties();

  /**
   * Constructor for creating a new chatbot pane
   *
   * @param editorController editor controller
   * @param ownerWindow      the window that contains the pane
   */
  public ChatBotPane(EditorController editorController, Window ownerWindow) {
    LOG.info("Initializing ChatBotPane");
    this.editorController = editorController;

    // Load chatbot properties
    loadChatbotProperties();

    TextArea tempChatArea = new TextArea();
    TextField tempInputField = new TextField();
    Button tempSendButton = new Button("Send");
    ChatBotApi tempChatBotApi = null;

    try {
      // First set up the UI elements so we can display errors if needed
      tempChatArea = new TextArea();
      tempChatArea.setEditable(false);
      tempChatArea.setWrapText(true);
      // Improve text appearance
      tempChatArea.setStyle(
          "-fx-font-family: 'System'; -fx-font-size: 14px; -fx-line-spacing: 5px;");
      // Add some padding for better readability
      tempChatArea.setPadding(new Insets(10));
      tempChatArea.appendText("Initializing chat assistant...\n");
      setCenter(tempChatArea);

      tempInputField = new TextField();
      tempInputField.setPromptText("Type your message...");
      tempSendButton = new Button("Send");
      HBox bottomBox = new HBox(5, tempInputField, tempSendButton);
      bottomBox.setPadding(new Insets(5));
      tempInputField.setPrefWidth(400);
      HBox.setHgrow(tempInputField, Priority.ALWAYS);
      setBottom(bottomBox);

      // Output debug info to help diagnose configuration issues
      String debugInfo = ConfigLoader.getInstance().getDebugInfo();
      LOG.info("Configuration debug info:\n{}", debugInfo);

      // Now try to initialize the ChatBotApi
      LOG.info("Creating ChatBotApi instance");
      tempChatBotApi = new ChatBotApi();

      // Get project information if available
      LOG.info("Updating project context");
      final ChatBotApi finalApi = tempChatBotApi;
      updateProjectContext(finalApi);

      // Get and display introduction message
      LOG.info("Requesting introduction message");
      final TextArea finalChatArea = tempChatArea;
      finalApi.getIntroduction()
          .thenAccept(introduction -> {
            LOG.info("Received introduction message");
            Platform.runLater(() -> {
              finalChatArea.clear();
              // Format the introduction message properly
              String formattedIntro = formatBotResponse(introduction);
              finalChatArea.appendText("Bot: " + formattedIntro + "\n\n");
            });
          })
          .exceptionally(ex -> {
            LOG.error("Error getting introduction message", ex);
            Platform.runLater(() -> {
              finalChatArea.clear();
              finalChatArea.appendText("Welcome to the OOGASalad Game Editor Chat Assistant!\n");
              finalChatArea.appendText(
                  "I'm here to help you with game design and using the editor.\n");
              finalChatArea.appendText(
                  "Ask me anything about game design, programming, or how to use features.\n\n");
              finalChatArea.appendText(
                  "Note: Could not connect to OpenAI API. Some features may be limited.\n\n");
            });
            return null;
          });
    } catch (Exception e) {
      LOG.error("Critical error initializing ChatBotPane", e);
      tempChatArea.clear();
      tempChatArea.appendText("Error initializing chat assistant\n\n");
      tempChatArea.appendText("Error message: " + e.getMessage() + "\n\n");
      tempChatArea.appendText("Please check your OpenAI API key configuration and try again.\n");
      tempChatArea.appendText("See the log for detailed error information.\n\n");

      // Output debug configuration
      try {
        tempChatArea.appendText("Configuration Information:\n");
        tempChatArea.appendText(ConfigLoader.getInstance().getDebugInfo());
        tempChatArea.appendText("\nTo fix this issue:\n");
        tempChatArea.appendText("1. Create a .env file in the project root directory\n");
        tempChatArea.appendText("2. Add your OpenAI API key: OPENAI_API_KEY=your_api_key_here\n");
        tempChatArea.appendText("3. Restart the application\n\n");
        tempChatArea.appendText("Alternatively, set the OPENAI_API_KEY environment variable.\n");
      } catch (Exception debugEx) {
        LOG.warn("Error displaying debug info", debugEx);
      }

      // Disable input if we couldn't initialize the API
      tempInputField.setDisable(true);
      tempSendButton.setDisable(true);
    }

    // Assign the final references
    this.chatArea = tempChatArea;
    this.inputField = tempInputField;
    this.sendButton = tempSendButton;
    this.chatBotApi = tempChatBotApi;

    // Set up event handlers if we have a valid API
    if (this.chatBotApi != null) {
      this.sendButton.setOnAction(e -> onSend());
      this.inputField.setOnAction(e -> onSend());
    }
  }

  /**
   * Loads properties from the chatbot.properties file.
   */
  private void loadChatbotProperties() {
    try (InputStream input = getClass().getClassLoader().getResourceAsStream(CHATBOT_PROPS_FILE)) {
      if (input != null) {
        chatbotProperties.load(input);
        LOG.info("Loaded chatbot properties from {}", CHATBOT_PROPS_FILE);
      } else {
        LOG.warn("Could not find chatbot properties file: {}. Using default values.",
            CHATBOT_PROPS_FILE);
      }
    } catch (IOException e) {
      LOG.warn("Failed to load chatbot properties: {}", e.getMessage());
    }
  }

  /**
   * Updates the chatbot with information about the current project. This should be called whenever
   * significant project changes occur.
   */
  public void updateProjectContext() {
    if (chatBotApi != null) {
      updateProjectContext(chatBotApi);
    }
  }

  /**
   * Updates the provided ChatBotApi with project context
   */
  private void updateProjectContext(ChatBotApi api) {
    if (editorController == null || api == null) {
      LOG.warn("Cannot update project context: editor controller or API is null");
      return;
    }

    try {
      // Extract relevant information from the editor controller
      StringBuilder projectInfo = new StringBuilder();

      // Add basic project information
      projectInfo.append("Project information from the OOGASalad editor:\n");

      // Note: Add specific project information extraction here when editor API is finalized
      // For example:
      // - Game name
      // - Number and types of game objects
      // - Game rules or mechanics
      // - Events defined in the game

      // Only update if we have meaningful information
      if (projectInfo.length() > 0) {
        api.updateProjectContext(projectInfo.toString());
        LOG.info("Updated chat assistant with current project context");
      }
    } catch (Exception e) {
      LOG.warn("Failed to update project context", e);
    }
  }

  private void onSend() {
    if (chatBotApi == null) {
      LOG.warn("Cannot send message: ChatBotApi is null");
      return;
    }

    String message = inputField.getText().trim();
    if (message.isEmpty()) {
      return;
    }

    // Handle special commands
    if (message.startsWith("/")) {
      handleSpecialCommand(message);
      return;
    }

    LOG.info("Sending user message: {}",
        (message.length() > 50 ? message.substring(0, 50) + "..." : message));

    chatArea.appendText("You: " + message + "\n");
    inputField.clear();

    // Show "thinking" indicator
    chatArea.appendText("Bot is thinking...\n");

    chatBotApi.sendMessage(message)
        .thenAccept(reply -> {
          LOG.info("Received response from chatbot");
          Platform.runLater(() -> {
            // Remove the "thinking" indicator
            String currentText = chatArea.getText();
            chatArea.setText(currentText.replace("Bot is thinking...\n", ""));

            // Format the response to properly handle newlines
            String formattedReply = formatBotResponse(reply);

            // Add the actual response
            chatArea.appendText("Bot: " + formattedReply + "\n\n");
          });
        })
        .exceptionally(ex -> {
          LOG.error("Error sending message to chatbot", ex);
          Platform.runLater(() -> {
            // Remove the "thinking" indicator
            String currentText = chatArea.getText();
            chatArea.setText(currentText.replace("Bot is thinking...\n", ""));

            // Add error message
            chatArea.appendText("Error: Failed to get response. " + ex.getMessage() + "\n\n");
          });
          return null;
        });
  }

  /**
   * Handles special commands that start with /.
   *
   * @param command The command to handle
   */
  private void handleSpecialCommand(String command) {
    inputField.clear();

    if (command.equalsIgnoreCase("/debug")) {
      LOG.info("Handling debug command");
      showDebugInfo();
      return;
    }

    if (command.equalsIgnoreCase("/help")) {
      chatArea.appendText("Available commands:\n");
      chatArea.appendText("/debug - Show configuration and debug information\n");
      chatArea.appendText("/help - Show this help message\n\n");
      return;
    }

    // Unknown command
    chatArea.appendText("Unknown command: " + command + "\n");
    chatArea.appendText("Type /help for a list of available commands\n\n");
  }

  /**
   * Displays configuration debug information in the chat area. This can be called by typing
   * "/debug" in the chat input.
   */
  private void showDebugInfo() {
    chatArea.appendText("\n--- Configuration Debug Information ---\n");
    try {
      String debugInfo = ConfigLoader.getInstance().getDebugInfo();
      chatArea.appendText(debugInfo);
      chatArea.appendText("\n--------------------------------------\n\n");
    } catch (Exception e) {
      chatArea.appendText("Error retrieving debug info: " + e.getMessage() + "\n\n");
    }
  }

  /**
   * Formats the bot's response for better readability. Handles newlines, bullet points, and other
   * formatting.
   *
   * @param response The raw response from the API
   * @return The formatted response
   */
  private String formatBotResponse(String response) {
    if (response == null || response.isEmpty()) {
      return "";
    }

    String formatted = response;

    // Apply formatting based on properties
    boolean formatNewlines = Boolean.parseBoolean(
        chatbotProperties.getProperty("format.newlines", "true"));
    boolean formatBullets = Boolean.parseBoolean(
        chatbotProperties.getProperty("format.bullets", "true"));
    boolean formatLists = Boolean.parseBoolean(
        chatbotProperties.getProperty("format.lists", "true"));

    // Replace escaped newlines with actual newlines
    if (formatNewlines) {
      formatted = formatted.replace("\\n", "\n");
    }

    // Ensure proper spacing for list items
    if (formatBullets) {
      formatted = formatted.replace("\n•", "\n • ");
      formatted = formatted.replace("\n-", "\n - ");
      formatted = formatted.replace("\n*", "\n * ");
    }

    // Handle numbered lists
    if (formatLists) {
      for (int i = 1; i <= 9; i++) {
        formatted = formatted.replace("\n" + i + ".", "\n" + i + ". ");
      }
    }

    return formatted;
  }
}