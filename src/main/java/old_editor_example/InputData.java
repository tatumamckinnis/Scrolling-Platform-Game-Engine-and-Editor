package old_editor_example;

import java.util.HashMap;
import java.util.Map;

public class InputData {
  // Mapping: KeyPressType -> (Key identifier -> event chain ID)
  private Map<KeyPressType, Map<String, String>> inputMapping;

  public InputData() {
    inputMapping = new HashMap<>();
    for (KeyPressType type : KeyPressType.values()) {
      inputMapping.put(type, new HashMap<>());
    }
  }

  /**
   * Adds a new input mapping.
   * @param pressType The type of key event (PRESSED, HELD, RELEASED).
   * @param keyIdentifier The key name (e.g., "A", "SPACE")—this would be chosen from a dropdown in the UI.
   * @param eventChainID The event chain ID to trigger.
   */
  public void addInputMapping(KeyPressType pressType, String keyIdentifier, String eventChainID) {
    inputMapping.get(pressType).put(keyIdentifier, eventChainID);
  }

  public Map<KeyPressType, Map<String, String>> getInputMapping() {
    return inputMapping;
  }
}