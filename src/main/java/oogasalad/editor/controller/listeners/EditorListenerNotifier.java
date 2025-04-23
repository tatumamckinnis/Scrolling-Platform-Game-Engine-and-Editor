package oogasalad.editor.controller.listeners;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import oogasalad.editor.view.EditorViewListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manages and notifies EditorViewListeners about changes in the editor state. This class
 * centralizes notification logic, decoupling it from the main controller.
 *
 * @author Tatum McKinnis, Jacob You
 */
public class EditorListenerNotifier {

  private static final Logger LOG = LogManager.getLogger(EditorListenerNotifier.class);
  private final CopyOnWriteArrayList<EditorViewListener> viewListeners = new CopyOnWriteArrayList<>();

  /**
   * Registers a new view listener if it is not already registered and is not null.
   *
   * @param listener the {@link EditorViewListener} to register.
   */
  public void registerViewListener(EditorViewListener listener) {
    if (listener != null && viewListeners.addIfAbsent(listener)) {
      LOG.debug("Registered view listener: {}", listener.getClass().getSimpleName());
    }
  }

  /**
   * Unregisters a previously registered view listener if it is not null.
   *
   * @param listener the {@link EditorViewListener} to unregister.
   */
  public void unregisterViewListener(EditorViewListener listener) {
    if (listener != null) {
      boolean removed = viewListeners.remove(listener);
      if (removed) {
        LOG.debug("Unregistered view listener: {}", listener.getClass().getSimpleName());
      }
    }
  }

  /**
   * Notifies all registered view listeners that a new object has been added.
   *
   * @param objectId the UUID of the object that was added.
   */
  public void notifyObjectAdded(UUID objectId) {
    LOG.debug("Notifying listeners: Object added {}", objectId);
    viewListeners.forEach(listener -> listener.onObjectAdded(objectId));
  }

  /**
   * Notifies all registered view listeners that an object has been removed.
   *
   * @param objectId the UUID of the object that was removed.
   */
  public void notifyObjectRemoved(UUID objectId) {
    LOG.debug("Notifying listeners: Object removed {}", objectId);
    viewListeners.forEach(listener -> {
      listener.onObjectRemoved(objectId);
    });
  }

  /**
   * Notifies all registered view listeners that an object's data has been updated.
   *
   * @param objectId the UUID of the object that was updated.
   */
  public void notifyObjectUpdated(UUID objectId) {
    LOG.debug("Notifying listeners: Object updated {}", objectId);
    viewListeners.forEach(listener -> listener.onObjectUpdated(objectId));
  }

  /**
   * Notifies all registered view listeners that the currently selected object has changed.
   *
   * @param selectedObjectId the UUID of the newly selected object, or null if no object is
   *                         selected.
   */
  public void notifySelectionChanged(UUID selectedObjectId) {
    LOG.debug("Notifying listeners: Selection changed {}", selectedObjectId);
    viewListeners.forEach(listener -> listener.onSelectionChanged(selectedObjectId));
  }

  /**
   * Notifies all registered view listeners that the set of available dynamic variables has
   * potentially changed.
   */
  public void notifyDynamicVariablesChanged() {
    LOG.debug("Notifying listeners: Dynamic variables changed");
    viewListeners.forEach(listener -> listener.onDynamicVariablesChanged());
  }

  /**
   * Notifies all registered view listeners that the set of available prefabs has changed.
   */
  public void notifyPrefabsChanged() {
    LOG.debug("Notifying listeners: Prefabs changed");
    viewListeners.forEach(listener -> listener.onPrefabsChanged());
  }

  /**
   * Notifies all registered view listeners that an error has occurred.
   *
   * @param errorMessage A descriptive message about the error.
   */
  public void notifyErrorOccurred(String errorMessage) {
    LOG.debug("Notifying listeners: Error occurred - {}", errorMessage);
    viewListeners.forEach(listener -> listener.onErrorOccurred(errorMessage));
  }

  /**
   * Notified all registered view listeners that the set of sprite templates has changed
   */
  public void notifySpriteTemplateChanged() {
    LOG.debug("Notifying listeners: Sprite template changed");
    viewListeners.forEach(EditorViewListener::onSpriteTemplateChanged);
  }
}