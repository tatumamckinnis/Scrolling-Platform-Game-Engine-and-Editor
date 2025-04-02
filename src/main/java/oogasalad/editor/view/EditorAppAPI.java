package oogasalad.editor.view;

import java.util.UUID;

public class EditorAppAPI {
  private UUID currentObjectID;
  private InputTabController inputTabController;

  public EditorAppAPI(InputTabController inputTabController) {
    this.inputTabController = inputTabController;
  }

  public UUID getCurrentObjectID() {
    return currentObjectID;
  }

  public void setCurrentObjectID(UUID currentObjectID) {
    this.currentObjectID = currentObjectID;
    inputTabController.setCurrentObjectId(currentObjectID);
  }
}
