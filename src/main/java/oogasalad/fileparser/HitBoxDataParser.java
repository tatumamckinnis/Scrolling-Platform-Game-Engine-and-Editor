package oogasalad.fileparser;

import oogasalad.fileparser.records.HitBoxData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oogasalad.fileparser.records.EventData;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class HitBoxDataParser {

  public HitBoxData getHitBoxData(Element objectNode) {
    String shape = objectNode.getAttribute("hitBoxShape");
    int hitBoxWidth = Integer.parseInt(objectNode.getAttribute("hitBoxWidth"));
    int hitBoxHeight = Integer.parseInt(objectNode.getAttribute("hitBoxHeight"));
    int spriteDx = Integer.parseInt(objectNode.getAttribute("spriteDx"));
    int spriteDy = Integer.parseInt(objectNode.getAttribute("spriteDy"));
    return new HitBoxData(shape, hitBoxWidth, hitBoxHeight, spriteDx, spriteDy);
  }

}
