package oogasalad.editor.view.panes.chat;

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
import oogasalad.editor.api.ChatBotApi;


/**
 * ChatBotPane provides a simple UI for sending messages to ChatBotApi and displaying responses.
 */
public class ChatBotPane extends BorderPane {
  private final ChatBotApi chatBotApi;
  private final TextArea chatArea;
  private final TextField inputField;
  private final Button sendButton;

  public ChatBotPane(EditorController editorController, Window ownerWindow) {
    String apiKey = System.getenv("OPENAI_API_KEY");
    this.chatBotApi = new ChatBotApi(apiKey);
    this.chatArea = new TextArea();
    this.inputField = new TextField();
    this.sendButton = new Button("Send");
    initializeUI();
  }

  private void initializeUI() {
    // Configure chat area
    chatArea.setEditable(false);
    chatArea.setWrapText(true);
    setCenter(chatArea);

    // Configure input area
    inputField.setPromptText("Type your message...");
    HBox bottomBox = new HBox(5, inputField, sendButton);
    bottomBox.setPadding(new Insets(5));
    inputField.setPrefWidth(400);
    HBox.setHgrow(inputField, Priority.ALWAYS);
    setBottom(bottomBox);

    // Event handlers
    sendButton.setOnAction(e -> onSend());
    inputField.setOnAction(e -> onSend());
  }

  private void onSend() {
    String message = inputField.getText().trim();
    if (message.isEmpty()) {
      return;
    }
    chatArea.appendText("You: " + message + "\n");
    inputField.clear();
    chatBotApi.sendMessage(message)
        .thenAccept(reply -> Platform.runLater(() -> chatArea.appendText("Bot: " + reply + "\n")));
  }
}