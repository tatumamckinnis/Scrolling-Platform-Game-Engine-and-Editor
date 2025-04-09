package oogasalad.fileparser;

import java.util.ArrayList;
import java.util.List;
import oogasalad.fileparser.records.GameObjectData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The {@code LayerDataParser} class is responsible for parsing layer data from an XML document.
 * <p>
 * It reads the layers of game objects defined in the XML and groups them into a map keyed by the
 * layer's z-index, where each entry contains a list of {@link GameObjectData} objects that belong
 * to that layer.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 *   Element root = ...; // obtain your XML root element
 *   LayerDataParser parser = new LayerDataParser();
 *   Map&lt;Integer, List&lt;GameObjectData&gt;&gt; layerMap = parser.getGameObjectDataMap(root);
 * </pre>
 * </p>
 *
 * @author Billy
 */
public class LayerDataParser {

  /**
   * A parser used to extract game object data from XML elements.
   */
  private GameObjectDataParser myGameObjectDataParser;

  /**
   * Parses the provided XML root element to extract game object data for each layer.
   *
   * <p>
   * This method locates the <code>layers</code> element, then iterates through each
   * <code>layer</code> element. It reads the z-index attribute of the layer and uses it as
   * the key in the returned map. All game objects defined within the layer are added as a list.
   * </p>
   *
   * @param root the XML {@link Element} that contains the layer definitions.
   * @return a List of game objects with their z coordinates by layer
   */
  public List<GameObjectData> getGameObjectDataList(Element root) {
    Element layersElement = (Element) root.getElementsByTagName("layers").item(0);
    List<GameObjectData> gameObjects = new ArrayList<>();
    myGameObjectDataParser = new GameObjectDataParser();

    NodeList layers = layersElement.getElementsByTagName("layer");

    for (int i = 0; i < layers.getLength(); i++) {
      Element layerElement = (Element) layers.item(i);
      int z = Integer.parseInt(layerElement.getAttribute("z"));
      gameObjects.addAll(readLayerData(layerElement, z));
    }
    return gameObjects;
    }

  /**
   * Reads and extracts {@link GameObjectData} objects from a given layer element.
   *
   * <p>
   * This method first retrieves the <code>data</code> element from the layer, then iterates through
   * all <code>object</code> elements within the <code>data</code> element. The game object data is
   * extracted using {@link GameObjectDataParser#getGameObjectData(Element, int)}.
   * </p>
   *
   * @param layerElement the XML {@link Element} representing a single layer.
   * @param z the z-index of the layer, used to assign the appropriate layering to the game objects.
   * @return a {@link List} of {@link GameObjectData} objects parsed from the layer element.
   */
  private List<GameObjectData> readLayerData(Element layerElement, int z) {
    List<GameObjectData> gameObjects = new ArrayList<>();
    Element dataNode = (Element) layerElement.getElementsByTagName("data").item(0);
    NodeList gameObjectNodes = dataNode.getElementsByTagName("object");
    for (int i = 0; i < gameObjectNodes.getLength(); i++) {
      if (gameObjectNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
        Element gameObjectElement = (Element) gameObjectNodes.item(i);
        gameObjects.addAll(myGameObjectDataParser.getGameObjectData(gameObjectElement, z));
      }
    }
    return gameObjects;
  }
}
