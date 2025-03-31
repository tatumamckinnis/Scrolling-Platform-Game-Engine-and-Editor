package old_editor_example.save;

import old_editor_example.CollisionData;
import old_editor_example.DynamicVariable;
import old_editor_example.EditorObject;
import old_editor_example.InputData;
import old_editor_example.KeyPressType;
import java.io.File;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import old_editor_example.SpriteData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SaveController {
    public static final File file = new File("save.xml");
    public static void save(EditorObject obj) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            // Root element: <GameObject>
            Element root = doc.createElement("GameObject");
            doc.appendChild(root);

            // Identity
            Element identity = doc.createElement("Identity");
            Element idElem = doc.createElement("ID");
            idElem.setTextContent(obj.getIdentity().getId().toString());
            Element groupElem = doc.createElement("Group");
            groupElem.setTextContent(obj.getIdentity().getGroup());
            identity.appendChild(idElem);
            identity.appendChild(groupElem);
            root.appendChild(identity);

            // Sprite Data
            SpriteData sprite = obj.getSpriteData();
            Element spriteElem = doc.createElement("SpriteData");
            Element spritePathElem = doc.createElement("SpritePath");
            spritePathElem.setTextContent(sprite.getSpritePath());
            Element spriteXElem = doc.createElement("X");
            spriteXElem.setTextContent(String.valueOf(sprite.getX()));
            Element spriteYElem = doc.createElement("Y");
            spriteYElem.setTextContent(String.valueOf(sprite.getY()));
            Element spriteWidthElem = doc.createElement("Width");
            spriteWidthElem.setTextContent(String.valueOf(sprite.getWidth()));
            Element spriteHeightElem = doc.createElement("Height");
            spriteHeightElem.setTextContent(String.valueOf(sprite.getHeight()));
            spriteElem.appendChild(spritePathElem);
            spriteElem.appendChild(spriteXElem);
            spriteElem.appendChild(spriteYElem);
            spriteElem.appendChild(spriteWidthElem);
            spriteElem.appendChild(spriteHeightElem);
            root.appendChild(spriteElem);

            // Collision Data
            CollisionData collision = obj.getCollisionData();
            Element collisionElem = doc.createElement("CollisionData");
            Element collisionXElem = doc.createElement("X");
            collisionXElem.setTextContent(String.valueOf(collision.getX()));
            Element collisionYElem = doc.createElement("Y");
            collisionYElem.setTextContent(String.valueOf(collision.getY()));
            Element collisionWidthElem = doc.createElement("Width");
            collisionWidthElem.setTextContent(String.valueOf(collision.getWidth()));
            Element collisionHeightElem = doc.createElement("Height");
            collisionHeightElem.setTextContent(String.valueOf(collision.getHeight()));
            Element collisionShapeElem = doc.createElement("Shape");
            collisionShapeElem.setTextContent(collision.getShape());
            collisionElem.appendChild(collisionXElem);
            collisionElem.appendChild(collisionYElem);
            collisionElem.appendChild(collisionWidthElem);
            collisionElem.appendChild(collisionHeightElem);
            collisionElem.appendChild(collisionShapeElem);
            root.appendChild(collisionElem);

            // Dynamic variables!!!
            Element dynamicVarsElem = doc.createElement("DynamicVariables");
            for (DynamicVariable var : obj.getDynamicVariables().getAllVariables()) {
                Element varElem = doc.createElement("Variable");
                varElem.setAttribute("name", var.getName());
                varElem.setAttribute("type", var.getType());
                varElem.setAttribute("description", var.getDescription());
                varElem.setTextContent(var.getValue().toString());
                dynamicVarsElem.appendChild(varElem);
            }
            root.appendChild(dynamicVarsElem);

            InputData inputData = obj.getInputData();  // Assuming your EditorObject has an InputData field.
            Element inputDataElem = doc.createElement("InputData");

            // Iterate over each KeyPressType.
            for (KeyPressType type : inputData.getInputMapping().keySet()) {
                Element typeElem = doc.createElement(type.name());
                // For each key mapping under that type.
                for (Map.Entry<String, String> entry : inputData.getInputMapping().get(type).entrySet()) {
                    Element mappingElem = doc.createElement("Mapping");
                    mappingElem.setAttribute("key", entry.getKey());
                    mappingElem.setAttribute("eventChainID", String.valueOf(entry.getValue()));
                    typeElem.appendChild(mappingElem);
                }
                inputDataElem.appendChild(typeElem);
            }
            root.appendChild(inputDataElem);

            // Write the content into an XML file.
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            // Format the XML output
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);
            System.out.println("Saved GameObject to " + file.getAbsolutePath());
        } catch (ParserConfigurationException | javax.xml.transform.TransformerException e) {
            e.printStackTrace();
        }
    }
}
