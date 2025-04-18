package oogasalad.editor.view.sprites;

import java.util.List;
import javafx.beans.property.*;
import oogasalad.editor.model.data.object.sprite.AnimationData;

/**
 * Helper row in the SpriteDefinitionComponent table:
 * holds a single animation’s name, duration, and frame‐sequence.
 */
public class AnimationRow {
  private final SimpleStringProperty name;
  private final SimpleDoubleProperty frameLength;
  private final SimpleStringProperty frames;

  public AnimationRow(String name, double length, List<String> seq) {
    this.name = new SimpleStringProperty(name);
    this.frameLength = new SimpleDoubleProperty(length);
    this.frames = new SimpleStringProperty(String.join(",", seq));
  }

  public StringProperty nameProperty()      { return name; }
  public DoubleProperty frameLengthProperty() { return frameLength; }
  public StringProperty framesProperty()    { return frames; }

  public AnimationData toAnimationData() {
    List<String> seq = List.of(frames.get().split(","));
    return new AnimationData(frameLength.get(), seq);
  }
}
