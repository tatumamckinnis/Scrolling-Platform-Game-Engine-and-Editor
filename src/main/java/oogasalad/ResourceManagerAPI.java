package oogasalad;

import java.util.Locale;

public interface ResourceManagerAPI {

  void setLocale(Locale language);

  String getText(String component, String key);

  String getConfig(String configFile, String key);
}
