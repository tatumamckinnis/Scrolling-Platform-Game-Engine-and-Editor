package oogasalad.fileparser;

import oogasalad.fileparser.records.AnimationData;
import oogasalad.fileparser.records.FrameData;
import oogasalad.fileparser.records.SpriteData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import oogasalad.fileparser.records.GameObjectData;
import org.w3c.dom.Element;

/**
 *
 *
 *
 * @author Billy McCune
 */
public class SpriteDataParser {
  //the path to graphic data is important as the sprite files are stored there.
  String pathToGraphicsData = System.getProperty("user.dir") + ("path.to.graphics.data");

  public SpriteData getSpriteData(String gameName, String group, String type, String spriteName, String spriteFile ) {
    SpriteData mySpriteData = null;


    return mySpriteData;
  }

  private FrameData parseFrameData(Element frameElement) {
    FrameData myFrameData = null;
    return myFrameData;
  }

  private AnimationData parseAnimationData(Element animationElement) {
    AnimationData myAnimationData = null;

    return myAnimationData;
  }
}
