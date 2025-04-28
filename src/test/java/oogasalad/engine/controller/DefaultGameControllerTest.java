package oogasalad.engine.controller;

import java.util.NoSuchElementException;
import oogasalad.engine.controller.api.GameManagerAPI;
import oogasalad.engine.controller.api.InputProvider;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.fileparser.records.LevelData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultGameControllerTest {

  private DefaultGameController controller;
  private InputProvider mockInputProvider;
  private GameManagerAPI mockGameManager;

  @BeforeEach
  void setUp() {
    mockInputProvider = mock(InputProvider.class);
    mockGameManager = mock(GameManagerAPI.class);
    controller = new DefaultGameController(mockInputProvider, mockGameManager);
  }

  @Test
  void getGameObjects_InitiallyEmpty_IsEmpty() {
    assertTrue(controller.getGameObjects().isEmpty());
  }


  @Test
  void getImmutablePlayers_InitiallyEmpty_IsEmpty() {
    GameObject player = mock(GameObject.class);
    when(player.getType()).thenReturn("player");

    GameObject enemy = mock(GameObject.class);
    when(enemy.getType()).thenReturn("enemy");

    controller.getGameObjects().addAll(List.of(player, enemy));

    List<ImmutableGameObject> players = controller.getImmutablePlayers();

    assertEquals(1, players.size());
    assertEquals(player, players.get(0));
  }

  @Test
  void endGame_Basic_CallsManager() {
    controller.endGame(true);
    verify(mockGameManager).endGame(true);
  }

  @Test
  void restartLevel_Basic_CallsManager() throws Exception {
    controller.restartLevel();
    verify(mockGameManager).restartGame();
  }

  @Test
  void selectLevel_Basic_CallsManager() throws Exception {
    controller.selectLevel("test-level.xml");
    verify(mockGameManager).selectGame("test-level.xml");
  }

  @Test
  void getViewObjectByUUID_Missing_ThrowsException() {
    Exception exception = assertThrows(NoSuchElementException.class, () -> {
      controller.getViewObjectByUUID("nonexistent-uuid");
    });
    assertTrue(exception.getMessage().contains("nonexistent-uuid"));
  }

  @Test
  void getCamera_InitiallyNull_IsNull() {
    assertNull(controller.getCamera());
  }

  @Test
  void setLevelData_InitializesFields_NotNull() {
    LevelData mockLevelData = mock(LevelData.class);
    when(mockLevelData.minX()).thenReturn(0);
    when(mockLevelData.minY()).thenReturn(0);
    when(mockLevelData.maxX()).thenReturn(1000);
    when(mockLevelData.maxY()).thenReturn(1000);

    controller.setLevelData(mockLevelData);

    assertNotNull(controller.getMapObject());
    assertNotNull(controller.getGameObjects());
  }

}
