package oogasalad.editor.model.data.object.sprite;

import java.util.List;

public class AnimationData {
  private double frameLength;
  private List<String> frameNames;

  public AnimationData(double frameLength, List<String> frameNames) {
    this.frameLength = frameLength;
    this.frameNames = frameNames;
  }

  public double getFrameLength() {
    return frameLength;
  }

  public List<String> getFrameNames() {
    return frameNames;
  }

  public void setFrameLength(double frameLength) {
    this.frameLength = frameLength;
  }

  public void addFrameName(String frameName) {
    this.frameNames.add(frameName);
  }

  public void removeFrameName(String frameName) {
    this.frameNames.remove(frameName);
  }
}
