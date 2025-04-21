package oogasalad.editor.view.panes.spriteCreation;

import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import oogasalad.editor.model.data.object.sprite.AnimationData;

/**
 * A helper class representing one row in the SpriteDefinitionComponent table.
 * <p>
 * Encapsulates a single animation’s name, frame duration, and sequence of frame names,
 * exposed as JavaFX properties for data binding in a TableView.
 * </p>
 *
 * @author Jacob You
 */
public class AnimationRow {

  private final SimpleStringProperty name;
  private final SimpleDoubleProperty frameLength;
  private final SimpleStringProperty frames;

  /**
   * Constructs an AnimationRow with the specified values.
   *
   * @param name   the name of the animation
   * @param length the duration of each frame in the animation (seconds)
   * @param seq    the ordered list of frame names comprising the animation
   */
  public AnimationRow(String name, double length, List<String> seq) {
    this.name = new SimpleStringProperty(name);
    this.frameLength = new SimpleDoubleProperty(length);
    this.frames = new SimpleStringProperty(String.join(",", seq));
  }

  /**
   * Returns the property containing the animation’s name.
   * <p>
   * Can be used for binding or for use as a cell value in a TableColumn.
   * </p>
   *
   * @return the StringProperty for the animation name
   */
  public StringProperty nameProperty() {
    return name;
  }

  /**
   * Returns the property containing the frame length (duration) of the animation.
   * <p>
   * Can be used for binding or for use as a cell value in a TableColumn.
   * </p>
   *
   * @return the DoubleProperty for the frame length
   */
  public DoubleProperty frameLengthProperty() {
    return frameLength;
  }

  /**
   * Returns the property containing the comma‑separated list of frame names.
   * <p>
   * Can be used for binding or for use as a cell value in a TableColumn.
   * </p>
   *
   * @return the StringProperty for the frame sequence
   */
  public StringProperty framesProperty() {
    return frames;
  }

  /**
   * Converts this table row back into a runtime {@link AnimationData} object.
   * <p>
   * Splits the comma‑separated frame names into a List and uses the current
   * name and frame length properties.
   * </p>
   *
   * @return a new AnimationData instance reflecting the current row values
   */
  public AnimationData toAnimationData() {
    List<String> seq = List.of(frames.get().split(","));
    return new AnimationData(name.get(), frameLength.get(), seq);
  }
}
