package oogasalad.engine.model.event;

public interface EventExecutor {

  public void execute(EventChain chain);

}
