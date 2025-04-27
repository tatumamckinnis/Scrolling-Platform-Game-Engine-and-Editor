// src/main/java/oogasalad/editor/view/tools/ChatBotApi.java
package oogasalad.editor.view.tools;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ChatBotApi handles communication with the OpenAI Chat Completion API without relying on external
 * JSON libraries. It constructs HTTP requests, sends messages asynchronously, and parses the
 * response content.
 *
 * @author Billy McCune
 */
public class ChatBotApi {

  private static final Logger LOG = LogManager.getLogger(ChatBotApi.class);
  private static final String API_KEY_PARAM = "OPENAI_API_KEY";
  private static final String MODEL_PARAM = "OPENAI_MODEL";
  private static final String DEFAULT_MODEL = "gpt-4o-mini";
  private static final String CHATBOT_PROPS_FILE = "oogasalad/editor/view/tools/chatbot.properties";

  // IMPORTANT: This is for testing purposes only! Should be removed in production.
  // Replace with a valid API key for testing if needed, then remove before deployment.
  private static final String FALLBACK_TEST_API_KEY = "";

  private final HttpClient httpClient;
  private final String apiKey;
  private final String model;
  private static final Pattern CONTENT_PATTERN = Pattern.compile(
      "\\\"content\\\"\\s*:\\s*\\\"(.*?)\\\"", Pattern.DOTALL);

  // Keep track of conversation history
  private final List<Message> conversationHistory = new ArrayList<>();

  // Properties loaded from configuration file
  private final Properties chatbotProperties = new Properties();

  // Default system context as fallback if properties file can't be loaded
  private static final String DEFAULT_SYSTEM_CONTEXT =
      "You are an AI assistant for the OOGASalad game editor, a tool for creating 2D games. "
          + "Your role is to help users with game design concepts, provide coding assistance, "
          + "explain editor features, and offer creative game design ideas. "
          + "You should be friendly, helpful, and focus on game development topics. "
          + "The editor allows creating objects, events, conditions, and actions for 2D games.";

  // Default introduction prompt as fallback
  private static final String DEFAULT_INTRO_PROMPT =
      "Please introduce yourself and explain how you can help with the OOGASalad game editor.";

  /**
   * Represents a message in the conversation.
   */
  private static class Message {

    private final String role;
    private final String content;

    /**
     * constructor for making a new message
     *
     * @param role    the message role
     * @param content the content of the message
     */
    public Message(String role, String content) {
      this.role = role;
      this.content = content;
    }

    /**
     * converts the conversation to a json format
     *
     * @return a new String
     */
    public String toJson() {
      StringBuilder json = new StringBuilder();
      json.append("{");
      json.append("\"role\":\"").append(role).append("\",");
      json.append("\"content\":\"").append(escapeJson(content)).append("\"");
      json.append("}");
      return json.toString();
    }
  }

  /**
   * Constructs a new ChatBotApi instance using configuration values.
   */
  public ChatBotApi() {
    LOG.info("Initializing ChatBotApi with configuration values");
    this.httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    // Load chatbot properties
    loadChatbotProperties();

    // Get API key from config
    try {
      this.apiKey = getApiKey();
      LOG.info("Successfully retrieved API key");
    } catch (IllegalStateException e) {
      LOG.error("Failed to get API key: {}", e.getMessage());
      throw e;
    }

    // Get model from config or use default
    String configModel = ConfigLoader.getInstance().getProperty(MODEL_PARAM);
    if (configModel == null || configModel.isEmpty()) {
      configModel = chatbotProperties.getProperty("default.model", DEFAULT_MODEL);
    }
    this.model = configModel;

    LOG.info("Initialized ChatBotApi with model: {}", this.model);

    // Initialize conversation with system message
    String systemContext = chatbotProperties.getProperty("system.context", DEFAULT_SYSTEM_CONTEXT);
    conversationHistory.add(new Message("system", systemContext));
    LOG.info("System context loaded from properties: {} characters", systemContext.length());
  }

  /**
   * Constructs a new ChatBotApi instance with the given API key. This constructor is maintained for
   * backward compatibility.
   *
   * @param apiKey the OpenAI API key used for authorization
   */
  public ChatBotApi(String apiKey) {
    LOG.info("Initializing ChatBotApi with provided API key");
    this.httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    if (apiKey == null || apiKey.isEmpty()) {
      String message = "API key cannot be null or empty";
      LOG.error(message);
      throw new IllegalArgumentException(message);
    }

    // Load chatbot properties
    loadChatbotProperties();

    this.apiKey = apiKey;

    // Get model from config or use default
    String configModel = ConfigLoader.getInstance().getProperty(MODEL_PARAM);
    if (configModel == null || configModel.isEmpty()) {
      configModel = chatbotProperties.getProperty("default.model", DEFAULT_MODEL);
    }
    this.model = configModel;

    LOG.info("Initialized ChatBotApi with model: {}", this.model);

    // Initialize conversation with system message
    String systemContext = chatbotProperties.getProperty("system.context", DEFAULT_SYSTEM_CONTEXT);
    conversationHistory.add(new Message("system", systemContext));
    LOG.info("System context loaded from properties: {} characters", systemContext.length());
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
   * Get API key from configuration. Throws an IllegalStateException if API key is not found.
   */
  private String getApiKey() {
    LOG.info("Retrieving API key from configuration sources");

    // First, try to get from regular configuration sources
    String key = ConfigLoader.getInstance().getProperty(API_KEY_PARAM);

    // If no key was found and we have a test key, use it as a last resort
    if ((key == null || key.isEmpty()) && !FALLBACK_TEST_API_KEY.isEmpty()) {
      LOG.warn(
          "No API key found in configuration. Using test fallback key for development purposes ONLY.");
      return FALLBACK_TEST_API_KEY;
    }

    // No regular key and no test key
    if (key == null || key.isEmpty()) {
      String message = "OpenAI API key not found. Please set " + API_KEY_PARAM +
          " in environment variables, system properties, config.properties, or .env file.";
      LOG.error(message);
      throw new IllegalStateException(message);
    }

    // Mask the key in logs for security
    String maskedKey = key.length() > 4
        ? key.substring(0, 4) + "..." + key.substring(key.length() - 4)
        : "***";
    LOG.info("Using API key: {}", maskedKey);

    return key;
  }

  /**
   * Gets the introduction message from the assistant.
   *
   * @return a CompletableFuture with the introduction message
   */
  public CompletableFuture<String> getIntroduction() {
    LOG.info("Requesting introduction message from API");
    String introPrompt = chatbotProperties.getProperty("introduction.prompt", DEFAULT_INTRO_PROMPT);
    return sendMessage(introPrompt, true);
  }

  /**
   * Sends a user message to the OpenAI Chat Completion API and returns the assistant's reply
   * asynchronously.
   *
   * @param userMessage the message text to send as the user role
   * @return a CompletableFuture that completes with the assistant's reply, or an error message if
   * the request fails
   */
  public CompletableFuture<String> sendMessage(String userMessage) {
    return sendMessage(userMessage, false);
  }

  /**
   * Sends a user message to the OpenAI Chat Completion API with an option to not add it to
   * history.
   *
   * @param userMessage    the message text to send as the user role
   * @param isSystemPrompt if true, this message won't be added to conversation history
   * @return a CompletableFuture that completes with the assistant's reply
   */
  private CompletableFuture<String> sendMessage(String userMessage, boolean isSystemPrompt) {
    LOG.info("Preparing to send message to OpenAI API: {}",
        (userMessage.length() > 50 ? userMessage.substring(0, 50) + "..." : userMessage));

    // Add user message to conversation history if it's not a system prompt
    if (!isSystemPrompt) {
      conversationHistory.add(new Message("user", userMessage));
    }

    try {
      // Construct a proper JSON structure instead of building strings
      StringBuilder requestBodyBuilder = new StringBuilder();
      requestBodyBuilder.append("{");
      requestBodyBuilder.append("\"model\":\"").append(model).append("\",");
      requestBodyBuilder.append("\"messages\":[");

      // Add all messages from history
      for (int i = 0; i < conversationHistory.size(); i++) {
        requestBodyBuilder.append(conversationHistory.get(i).toJson());
        if (i < conversationHistory.size() - 1) {
          requestBodyBuilder.append(",");
        }
      }

      // Add the current message if it's a system prompt
      if (isSystemPrompt) {
        if (!conversationHistory.isEmpty()) {
          requestBodyBuilder.append(",");
        }
        requestBodyBuilder.append("{\"role\":\"user\",\"content\":\"")
            .append(escapeJson(userMessage))
            .append("\"}");
      }

      requestBodyBuilder.append("]");
      requestBodyBuilder.append("}");

      String requestBody = requestBodyBuilder.toString();

      // Log the request for debugging (mask any API keys)
      String logRequest = requestBody;
      if (requestBody.length() > 500) {
        logRequest = requestBody.substring(0, 500) + "... (truncated)";
      }
      LOG.info("Request body: {}", logRequest);

      LOG.info("Sending request to OpenAI API with model: {}", model);

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create("https://api.openai.com/v1/chat/completions"))
          .header("Content-Type", "application/json")
          .header("Authorization", "Bearer " + apiKey)
          .POST(HttpRequest.BodyPublishers.ofString(requestBody))
          .build();

      LOG.info("Sending HTTP request to OpenAI API");

      return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
          .thenApply(response -> {
            LOG.info("Received response from OpenAI API with status code: {}",
                response.statusCode());
            if (response.statusCode() != 200) {
              String errorMessage = "API error: " + response.statusCode() + " - " + response.body();
              LOG.error(errorMessage);
              throw new RuntimeException(errorMessage);
            }
            return response.body();
          })
          .thenApply(body -> {
            LOG.info("Parsing response body");
            String content = parseContent(body);
            if (content.startsWith("Error:")) {
              LOG.warn("Failed to parse response: {}", content);
            } else {
              LOG.info("Successfully parsed response");
            }
            return content;
          })
          .thenCompose(response -> {
            // Only add the assistant's response to conversation history if it's not a system prompt
            if (!isSystemPrompt && !response.startsWith("Error:")) {
              conversationHistory.add(new Message("assistant", response));
              LOG.info("Added assistant response to conversation history");
            }
            return CompletableFuture.completedFuture(response);
          })
          .exceptionally(e -> {
            LOG.error("Error in API communication", e);
            return "Error: " + e.getMessage();
          });
    } catch (Exception e) {
      LOG.error("Error preparing OpenAI API request", e);
      CompletableFuture<String> failed = new CompletableFuture<>();
      failed.complete("Error: " + e.getMessage());
      return failed;
    }
  }

  /**
   * Escapes backslashes, double quotes, newlines, and other control characters to safely include
   * text in a JSON string.
   *
   * @param text the raw message text
   * @return the JSON-escaped text
   */
  private static String escapeJson(String text) {
    if (text == null) {
      return "";
    }

    StringBuilder escaped = new StringBuilder();
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      switch (c) {
        case '"':
          escaped.append("\\\"");
          break;
        case '\\':
          escaped.append("\\\\");
          break;
        case '\b':
          escaped.append("\\b");
          break;
        case '\f':
          escaped.append("\\f");
          break;
        case '\n':
          escaped.append("\\n");
          break;
        case '\r':
          escaped.append("\\r");
          break;
        case '\t':
          escaped.append("\\t");
          break;
        default:
          // Unicode escape for control characters, otherwise append directly
          if (c < 32) {
            String hex = Integer.toHexString(c);
            escaped.append("\\u");
            for (int j = 0; j < 4 - hex.length(); j++) {
              escaped.append('0');
            }
            escaped.append(hex);
          } else {
            escaped.append(c);
          }
          break;
      }
    }
    return escaped.toString();
  }

  /**
   * Parses the first occurrence of the "content" field from the API response JSON using a regular
   * expression.
   *
   * @param responseBody the raw JSON response body
   * @return the extracted content value, or an error message if not found
   */
  private String parseContent(String responseBody) {
    try {
      Matcher matcher = CONTENT_PATTERN.matcher(responseBody);
      if (matcher.find()) {
        return matcher.group(1);
      }
      LOG.warn("Content pattern not found in response: {}",
          (responseBody.length() > 100 ? responseBody.substring(0, 100) + "..." : responseBody));
      return "Error: content not found in response";
    } catch (Exception e) {
      LOG.error("Error parsing response content", e);
      return "Error: " + e.getMessage();
    }
  }

  /**
   * Updates the chatbot's context with information about the current project. This will help the
   * chatbot provide more specific and helpful responses.
   *
   * @param projectInfo Information about the current project
   */
  public void updateProjectContext(String projectInfo) {
    if (projectInfo == null || projectInfo.isEmpty()) {
      return;
    }

    LOG.info("Updating project context for ChatBotApi");

    // Get the base system context
    String systemContext = chatbotProperties.getProperty("system.context", DEFAULT_SYSTEM_CONTEXT);

    // Append the project information
    String updatedContext = systemContext + "\n\nCurrent project information: " + projectInfo;

    // Check if we already have a system message
    boolean hasSystemMessage = false;
    for (int i = 0; i < conversationHistory.size(); i++) {
      Message message = conversationHistory.get(i);
      if ("system".equals(message.role)) {
        // Update the first system message with the new context
        conversationHistory.set(i, new Message("system", updatedContext));
        hasSystemMessage = true;
        break;
      }
    }

    // If no system message was found, add one
    if (!hasSystemMessage) {
      conversationHistory.add(0, new Message("system", updatedContext));
    }
  }
}