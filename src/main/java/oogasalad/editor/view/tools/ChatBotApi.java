// src/main/java/oogasalad/editor/api/ChatBotApi.java
package oogasalad.editor.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ChatBotApi handles communication with the OpenAI Chat Completion API without external JSON libraries.
 */
public class ChatBotApi {
  private final HttpClient httpClient;
  private final String apiKey;
  private static final Pattern CONTENT_PATTERN = Pattern.compile("\\\"content\\\"\\s*:\\s*\\\"(.*?)\\\"", Pattern.DOTALL);

  public ChatBotApi(String apiKey) {
    this.httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();
    this.apiKey = apiKey;
  }

  /**
   * Sends a message to the Chat API and returns the assistant's reply asynchronously.
   */
  public CompletableFuture<String> sendMessage(String userMessage) {
    String escaped = escapeJson(userMessage);
    String requestBody = "{"
        + "\"model\":\"gpt-4o-mini\","
        + "\"messages\":[{\"role\":\"user\",\"content\":\"" + escaped + "\"}]"
        + "}";
    try {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create("https://api.openai.com/v1/chat/completions"))
          .header("Content-Type", "application/json")
          .header("Authorization", "Bearer " + apiKey)
          .POST(HttpRequest.BodyPublishers.ofString(requestBody))
          .build();

      return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
          .thenApply(HttpResponse::body)
          .thenApply(this::parseContent);
    } catch (Exception e) {
      CompletableFuture<String> failed = new CompletableFuture<>();
      failed.complete("Error: " + e.getMessage());
      return failed;
    }
  }

  /**
   * Escapes backslashes and quotes for JSON string.
   */
  private String escapeJson(String text) {
    return text.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  /**
   * Parses the first occurrence of "content" in the response JSON.
   */
  private String parseContent(String responseBody) {
    Matcher matcher = CONTENT_PATTERN.matcher(responseBody);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return "Error: content not found in response";
  }
}