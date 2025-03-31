package oogasalad.engine.model.event;

import java.util.HashMap;
import java.util.Map;

public final class EventChainRegistry {

  private static final Map<String, EventChain> registry = new HashMap<>();

  // Private constructor prevents instantiation
  private EventChainRegistry() {
  }

  public static void registerChain(String id, EventChain chain) {
    registry.put(id, chain);
  }

  public static EventChain getChain(String id) {
    return registry.get(id);
  }

  public static void unregisterChain(String id) {
    registry.remove(id);
  }

  public static boolean containsChain(String id) {
    return registry.containsKey(id);
  }

}

