package oogasalad.server;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import javafx.scene.input.KeyCode;
import oogasalad.engine.view.ViewState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

class MessageHandlerFactoryTest extends ApplicationTest {

  private ViewState mockViewState;

  @BeforeEach
  void setup() {
    mockViewState = mock(ViewState.class);
  }

  @Test
  void handleMessage_StartGame_ReturnsNonNullRunnableForKnownType() {
    ServerMessage startGameMessage = new ServerMessage("startGame", "Starting...");

    Runnable action = MessageHandlerFactory.handleMessage(mockViewState, startGameMessage);

    assertNotNull(action);
  }

  @Test
  void handleMessage_UnknownType_ReturnsEmptyRunnableForUnknownType() {
    ServerMessage unknownMessage = new ServerMessage("unknownType", "Some message");

    Runnable action = MessageHandlerFactory.handleMessage(mockViewState, unknownMessage);

    assertNotNull(action);
    assertDoesNotThrow(action::run);
  }

  @Test
  void pressKey_PressKey_TriggersPressKeyOnViewState() {
    ServerMessage message = new ServerMessage("pressKey", "SPACE");
    Runnable r = MessageHandlerFactory.handleMessage(mockViewState, message);
    r.run();

    WaitForAsyncUtils.waitForFxEvents();

    verify(mockViewState).pressKey(KeyCode.SPACE);
  }

  @Test
  void releaseKey_ReleaseKey_TriggersReleaseKeyOnViewState() {
    ServerMessage message = new ServerMessage("releaseKey", "SPACE");
    Runnable r = MessageHandlerFactory.handleMessage(mockViewState, message);
    r.run();

    WaitForAsyncUtils.waitForFxEvents();

    verify(mockViewState).releaseKey(KeyCode.SPACE);
  }
}
