package oogasalad.editor.view.components;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javafx.scene.image.Image;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.model.data.SpriteSheetAtlas;
import oogasalad.editor.model.data.object.EditorObject;
import org.apache.logging.log4j.Logger;

/**
 * Manages image loading, caching, and resolution for the EditorGameView.
 */
class EditorGameViewImageManager {

  private final Map<UUID, Image> objectImages = new HashMap<>();
  private final EditorGameView view;
  private final EditorController controller;
  private final Logger log;

  EditorGameViewImageManager(EditorGameView view, EditorController controller, Logger log) {
    this.view = view;
    this.controller = controller;
    this.log = log;
  }

  void preloadObjectImage(UUID id) {
    try {
      String imagePath = getObjectSpritePath(id);
      if (imagePath == null) {
        objectImages.remove(id);
        log.trace("No valid sprite path found for ID {}. Removing image cache.", id);
        return;
      }

      String resolvedPathOrUrl = resolveImagePath(imagePath);
      if (resolvedPathOrUrl == null) {
        log.error("Could not resolve image path/URL for: {}", imagePath);
        objectImages.remove(id);

        return;
      }

      loadImageIfNotCached(id, resolvedPathOrUrl);

    } catch (Exception e) {
      log.error("Failed during image preload process for object ID {}: {}", id, e.getMessage(), e);
      objectImages.remove(id);

    }
  }

  private String getObjectSpritePath(UUID id) {
    EditorObject object = controller.getEditorObject(id);
    if (object == null || object.getSpriteData() == null) {
      return null;
    }

    SpriteSheetAtlas atlas = controller.getEditorDataAPI().getLevel().getAtlas(object.getId());
    if (atlas == null || atlas.getImageFile() == null) {
      log.warn("Atlas or image file not found for object {}", id);
      return null;
    }
    return atlas.getImageFile().getAbsolutePath();
  }


  private void loadImageIfNotCached(UUID id, String path) {
    Image cachedImage = objectImages.get(id);
    String urlString = convertPathToUrlString(path);

    if (urlString == null) {
      log.error("Could not create a valid URL string from path: {}", path);
      objectImages.remove(id);
      view.refreshDisplay();
      return;
    }

    boolean needsLoading = shouldLoadImage(cachedImage, urlString);

    if (needsLoading) {
      loadImage(id, urlString);
    } else {
      log.trace("Image for {} already cached and valid: {}", id, urlString);
    }
  }

  private String convertPathToUrlString(String path) {
    try {
      File file = new File(path);
      if (file.isAbsolute() && file.exists()) {
        log.trace("Converted absolute path '{}' to URL '{}'", path, file.toURI().toString());
        return file.toURI().toString();
      } else {

        String resolved = resolveImagePath(path);
        if (resolved != null) {
          log.trace("Resolved relative path '{}' to URL '{}'", path, resolved);
          return resolved;
        } else {
          log.warn("Path '{}' is not absolute and could not be resolved as a resource.", path);
          return null;
        }
      }
    } catch (Exception e) {
      log.error("Error converting path '{}' to URL: {}", path, e.getMessage(), e);
      return null;
    }
  }


  private boolean shouldLoadImage(Image cachedImage, String urlString) {
    return cachedImage == null
        || !Objects.equals(cachedImage.getUrl(), urlString)
        || cachedImage.isError();
  }

  private void loadImage(UUID id, String urlString) {
    log.debug("Loading image for object ID {} from resolved URL string: {}", id, urlString);
    try {
      Image newImage = new Image(urlString, true);
      setupImageListeners(newImage, id, urlString);
      objectImages.put(id, newImage);

    } catch (IllegalArgumentException e) {
      log.error("Failed to load image for {} - Invalid URL or resource not found: {}", id,
          urlString, e);
      objectImages.remove(id);
      view.refreshDisplay();
    } catch (Exception e) {
      log.error("Unexpected error loading image for {} from URL {}: {}", id, urlString,
          e.getMessage(), e);
      objectImages.remove(id);
      view.refreshDisplay();
    }
  }

  private void setupImageListeners(Image image, UUID id, String url) {
    image.errorProperty().addListener((obs, oldErr, newErr) -> {
      if (newErr) {
        handleImageError(image, id, url);
      }
    });
    image.progressProperty().addListener((obs, oldProgress, newProgress) -> {
      if (newProgress != null && newProgress.doubleValue() >= 1.0) {
        handleImageLoadComplete(image, id, url);
      }
    });
  }

  private void handleImageError(Image image, UUID id, String url) {
    String errorMessage = (image.getException() != null) ? image.getException().getMessage()
        : "Unknown image loading error";
    log.error("Failed to load image for {} from {}: {}", id, url, errorMessage);

    objectImages.remove(id);
    view.refreshDisplay();
  }

  private void handleImageLoadComplete(Image image, UUID id, String url) {
    if (image.isError()) {
      log.warn("Error flag set after image load completion signal for ID {}, URL: {}", id, url);
      objectImages.remove(id);
    } else {
      log.trace("Image loaded successfully for ID {}: {}", id, url);
    }
    view.refreshDisplay();
  }

  /**
   * Resolves a given path string into a loadable URL string. Tries absolute path, then classpath,
   * then relative asset path.
   *
   * @param path The path string to resolve.
   * @return A URL string if resolvable, otherwise null.
   */
  String resolveImagePath(String path) {
    if (path == null || path.trim().isEmpty()) {
      log.warn("Attempted to resolve null or empty image path.");
      return null;
    }

    log.trace("Attempting to resolve image path: {}", path);

    String absoluteUrl = checkAbsolutePath(path);
    if (absoluteUrl != null) {
      log.trace("Resolved '{}' as absolute path URL: {}", path, absoluteUrl);
      return absoluteUrl;
    }

    String classpathUrl = findClasspathResource(path);
    if (classpathUrl != null) {
      log.trace("Resolved '{}' as classpath resource URL: {}", path, classpathUrl);
      return classpathUrl;
    }

    String relativeAssetUrl = findRelativeAssetPath(path);
    if (relativeAssetUrl != null) {
      log.trace("Resolved '{}' as relative asset URL: {}", path, relativeAssetUrl);
      return relativeAssetUrl;
    }

    log.warn("Could not resolve image path: {}", path);
    return null;
  }

  /**
   * Checks if the path represents an existing absolute file path and returns its URL string.
   *
   * @param path The path string.
   * @return File URL string if absolute and exists, null otherwise.
   */
  private String checkAbsolutePath(String path) {
    try {
      File f = new File(path);
      if (f.isAbsolute() && f.exists() && f.isFile()) {
        return f.toURI().toString();
      }
    } catch (Exception e) {
      log.debug("Error checking absolute path '{}': {}", path, e.getMessage());
    }
    return null;
  }

  /**
   * Attempts to find the path as a resource within the application's classpath.
   *
   * @param path The relative path string (e.g., "images/player.png").
   * @return The full URL string if the resource is found, or null otherwise.
   */
  private String findClasspathResource(String path) {
    try {
      String resourcePath = path.startsWith("/") ? path : "/" + path;
      URL resourceUrl = getClass().getResource(resourcePath);
      if (resourceUrl != null) {
        return resourceUrl.toExternalForm();
      }
    } catch (Exception e) {
      log.debug("Error checking classpath resource '{}': {}", path, e.getMessage());
    }
    return null;
  }

  /**
   * Attempts to find the path relative to the game's graphics data directory.
   *
   * @param path The relative path string (e.g., "player.png").
   * @return The file URL string if found, or null otherwise.
   */
  private String findRelativeAssetPath(String path) {
    try {
      String gameName = controller.getEditorDataAPI().getGameName();
      if (gameName != null && !gameName.isBlank()) {
        Path assetPath = Paths.get("data", "graphicsData", gameName, path);
        File assetFile = assetPath.toFile();
        if (assetFile.exists() && assetFile.isFile()) {
          return assetFile.toURI().toString();
        }
      } else {
        log.debug("Cannot check relative asset path for '{}': game name is blank.", path);
      }
    } catch (Exception e) {
      log.debug("Error checking relative asset path '{}': {}", path, e.getMessage());
    }
    return null;
  }


  Image getImage(UUID id) {
    return objectImages.get(id);
  }

  void removeImage(UUID id) {
    objectImages.remove(id);
  }

  void clearCache() {
    objectImages.clear();
  }
}