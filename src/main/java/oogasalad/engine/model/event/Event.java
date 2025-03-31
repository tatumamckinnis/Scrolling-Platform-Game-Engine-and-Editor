package oogasalad.engine.model.event;

import java.util.List;

public abstract class Event {

  private String eventName;
  private Integer eventID;
  private List<ParameterDefinition> parameterDefinitionList;

}
