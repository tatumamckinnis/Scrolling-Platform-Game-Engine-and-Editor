package oogasalad.editor.view.components;

import java.io.File;
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


  String resolveImagePath(String path) {
    if (path == null || path.trim().isEmpty()) {
      return null;
    }
    File f = new File(path);
    if (f.isAbsolute()) {
      log.trace("Path '{}' identified as absolute.", path);

      return f.exists() ? convertPathToUrlString(path) : null;
    }

    log.trace("Path '{}' not absolute, attempting classpath/relative resolution.", path);


    String resourcePath = path.startsWith("/") ? path : "/" + path;
    try {
      java.net.URL resourceUrl = getClass().getResource(resourcePath);
      if (resourceUrl != null) {
        return resourceUrl.toExternalForm();
      }
    } catch (Exception e) {
      log.warn("Error checking classpath resource '{}': {}", resourcePath, e.getMessage());
    }


    try {
      String gameName = controller.getEditorDataAPI().getGameName();
      if (gameName != null && !gameName.isBlank()) {
        Path assetPath = Paths.get("data", "graphicsData", gameName, path);
        File assetFile = assetPath.toFile();
        if (assetFile.exists() && assetFile.isFile()) {
          return assetFile.toURI().toString();
        }
      }
    } catch (Exception e) {
      log.warn("Error checking relative asset path '{}': {}", path, e.getMessage());
    }

    log.warn("Could not resolve relative path: {}", path);
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
