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
 * Manages the loading, caching, and resolution of image paths for game objects displayed
 * within the {@link EditorGameView}. It interacts with the {@link EditorController} to
 * retrieve object data (like sprite sheet paths) and handles asynchronous image loading
 * using JavaFX {@link Image}. Maintains an internal cache to avoid redundant loading.
 *
 * @author Tatum McKinnis
 */
class EditorGameViewImageManager {

  private final Map<UUID, Image> objectImages = new HashMap<>();
  private final EditorGameView view;
  private final EditorController controller;
  private final Logger log;

  /**
   * Constructs an EditorGameViewImageManager.
   *
   * @param view The parent EditorGameView.
   * @param controller The application's editor controller.
   * @param log The logger instance for logging.
   */
  EditorGameViewImageManager(EditorGameView view, EditorController controller, Logger log) {
    this.view = view;
    this.controller = controller;
    this.log = log;
  }

  /**
   * Initiates the loading and caching process for the image associated with a specific object ID.
   * It retrieves the sprite path, resolves it to a loadable URL, and then loads the image
   * if it's not already cached or if the cached version is invalid.
   *
   * @param id The UUID of the game object whose image should be preloaded.
   */
  void preloadObjectImage(UUID id) {
    try {
      String imagePath = getObjectSpritePath(id);
      System.out.println(imagePath);
      if (imagePath == null) {
        objectImages.remove(id);
        log.trace("No valid sprite path found for ID {}. Removing image cache.", id);
        return;
      }

      String resolvedPathOrUrl = resolveImagePath(imagePath);
      if (resolvedPathOrUrl == null) {
        log.error("Could not resolve image path/URL for: {}", imagePath);
        objectImages.remove(id);
        view.refreshDisplay();
        return;
      }

      loadImageIfNotCached(id, resolvedPathOrUrl);

    } catch (Exception e) {
      log.error("Failed during image preload process for object ID {}: {}", id, e.getMessage(), e);
      objectImages.remove(id);
      view.refreshDisplay();
    }
  }

  /**
   * Retrieves the absolute file path of the sprite sheet image associated with a given object ID.
   * Fetches the object data and its corresponding atlas from the controller.
   *
   * @param id The UUID of the game object.
   * @return The absolute path string to the image file, or null if the object, sprite data,
   * atlas, or image file information is not found.
   */
  private String getObjectSpritePath(UUID id) {
    EditorObject obj = controller.getEditorObject(id);
    if (obj == null || obj.getSpriteData() == null) return null;

    // 1) preferred: atlas registered for this object
    SpriteSheetAtlas atlas =
        controller.getEditorDataAPI().getLevel().getAtlas(obj.getId());
    if (atlas != null && atlas.getImageFile() != null) {
      return atlas.getImageFile().getPath();
    }

    // 2) fallback: whatever was stored in the sprite record itself
    String spriteFile = obj.getSpriteData().getSpritePath();
    if (spriteFile != null && !spriteFile.isEmpty()) {
      log.debug("Using direct sprite file for {} because no atlas is available", id);
      return spriteFile;
    }

    log.warn("No atlas *and* no sprite file for object {}", id);
    return null;
  }


  /**
   * Checks if an image needs to be loaded for the given ID and path/URL.
   * Converts the path to a URL string and compares it with the cached image's URL.
   * Initiates loading if the image is not cached, the URL has changed, or the cached image is in an error state.
   *
   * @param id The UUID of the object.
   * @param path The resolved path or URL string of the image.
   */
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

  /**
   * Converts a file path string into a URL string suitable for loading with JavaFX Image.
   * Handles absolute paths and attempts to resolve relative paths as classpath resources.
   *
   * @param path The file path string (can be absolute or relative).
   * @return A URL string (e.g., "file:/...", "jar:file:/..."), or null if conversion fails or the path is invalid.
   */
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


  /**
   * Determines if a new image load is required based on the cached image and the target URL.
   *
   * @param cachedImage The currently cached Image object (can be null).
   * @param urlString The target URL string for the image.
   * @return true if the image should be loaded (not cached, URL mismatch, or cache error), false otherwise.
   */
  private boolean shouldLoadImage(Image cachedImage, String urlString) {
    return cachedImage == null
        || !Objects.equals(cachedImage.getUrl(), urlString)
        || cachedImage.isError();
  }

  /**
   * Loads an image from the given URL string asynchronously.
   * Creates a new JavaFX {@link Image}, sets up listeners for load completion and errors,
   * and adds the new image to the cache. If loading fails immediately (e.g., invalid URL),
   * removes the entry and refreshes the view.
   *
   * @param id The UUID of the object associated with this image.
   * @param urlString The URL string from which to load the image.
   */
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

  /**
   * Sets up listeners on an Image object to handle asynchronous loading events (errors and progress).
   *
   * @param image The Image object to attach listeners to.
   * @param id The UUID of the associated object.
   * @param url The URL from which the image is being loaded (for logging).
   */
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

  /**
   * Handles the case where image loading fails asynchronously.
   * Logs the error, removes the failed image entry from the cache, and triggers a view refresh
   * so a placeholder might be drawn.
   *
   * @param image The Image object that failed to load.
   * @param id The UUID of the associated object.
   * @param url The URL that failed to load (for logging).
   */
  private void handleImageError(Image image, UUID id, String url) {
    String errorMessage = (image.getException() != null) ? image.getException().getMessage()
        : "Unknown image loading error";
    log.error("Failed to load image for {} from {}: {}", id, url, errorMessage);

    objectImages.remove(id);
    view.refreshDisplay();
  }

  /**
   * Handles the case where image loading completes asynchronously (progress reaches 1.0).
   * Double-checks if an error occurred despite the completion signal, logs success,
   * and triggers a view refresh to display the newly loaded image.
   *
   * @param image The Image object that finished loading.
   * @param id The UUID of the associated object.
   * @param url The URL that was loaded (for logging).
   */
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
   * Resolves a given path string into a loadable URL string using multiple strategies.
   * It prioritizes:
   * 1. Absolute file paths.
   * 2. Classpath resources.
   * 3. Paths relative to the current game's graphics data directory.
   *
   * @param path The path string to resolve (can be absolute, classpath-relative, or game-asset-relative).
   * @return A loadable URL string (e.g., "file:/...", "jar:file:/...") if the path can be
   * resolved using one of the strategies, otherwise null.
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
   * Checks if the given path string represents an existing absolute file path.
   *
   * @param path The path string to check.
   * @return The file URL string (e.g., "file:/...") if the path is absolute, exists,
   * and is a file; otherwise, returns null.
   */
  private String checkAbsolutePath(String path) {
    try {
      File f = new File(path);
      if (f.isAbsolute() && f.exists() && f.isFile()) {
        return f.getAbsolutePath();
      }
    } catch (Exception e) {
      log.debug("Error checking absolute path '{}': {}", path, e.getMessage());
    }
    return null;
  }

  /**
   * Attempts to find the given path as a resource within the application's classpath.
   * Prepends a '/' if the path doesn't start with one.
   *
   * @param path The relative path string within the classpath (e.g., "images/player.png").
   * @return The full URL string (e.g., "jar:file:/...") if the resource is found,
   * or null otherwise.
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
   * Attempts to find the given path relative to the current game's specific graphics data directory
   * (e.g., "data/graphicsData/MyGameName/player.png"). Requires the game name to be set in the model.
   *
   * @param path The relative path string within the game's graphics directory (e.g., "player.png").
   * @return The file URL string (e.g., "file:/...") if the file is found, or null otherwise.
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


  /**
   * Retrieves the cached Image object associated with the given object ID.
   *
   * @param id The UUID of the object.
   * @return The cached {@link Image} object, or null if no image is cached for this ID
   * or if the cached image failed to load.
   */
  Image getImage(UUID id) {
    return objectImages.get(id);
  }

  /**
   * Removes the image associated with the given object ID from the cache.
   *
   * @param id The UUID of the object whose image should be removed.
   */
  void removeImage(UUID id) {
    objectImages.remove(id);
  }

  /**
   * Clears the entire image cache, removing all stored images.
   */
  void clearCache() {
    objectImages.clear();
  }
}