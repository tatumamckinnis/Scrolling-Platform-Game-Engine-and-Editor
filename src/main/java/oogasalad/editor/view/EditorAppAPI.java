package oogasalad.editor.view;

import java.util.UUID;

public class EditorAppAPI {
  private static UUID currentObjectID;

  public static UUID getCurrentObjectID() {
    return currentObjectID;
  }

  public static void setCurrentObjectID(UUID currentObjectID) {
    EditorAppAPI.currentObjectID = currentObjectID;
  }
}
