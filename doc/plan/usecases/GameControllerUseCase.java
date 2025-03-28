/**
 * Use Case: User selects a new game to play from list of games
 * <p>
 * This use case demonstrates how the GameController API is used to: 1. Generate a new set of
 * GameObjects for a given level 2. Pass the set of Game Objects to the View for rendering purposes
 * 3. Update the state of the game engine one tick at a time on each iteration of the game
 * <p>
 * API Collaboration: - GameController ↔ GameObject: GameController will update the list of current
 * GameObjects on each "tick" of the game engine - GameController ↔ EngineFileAPI: loadLevel()
 * invokes EngineFileAPI to load new GameObjects when a level is selected - GameController ↔
 * LevelView: GameAppView calls getUpdatedObjects() to determine what objects and how to render
 * them
 *
 * @author Alana Zinkin
 */

public class GameControllerUseCase {

  private GameAppView gameView;
  private GameControllerAPI gameController;
  private List<GameObject> gameObjects;
  private Level level;

  /**
   * Create and render opening Splash Screen
   */
  public void setup() {
    // Mmock implementations
    gameView = new MockGameAppView();
    gameManager = new MockGameManager();
    gameController = new MockGameController();
    fileParser = new MockFileParser();
    gameObjects = new ArrayList<>();
    createStartSplashScreen();
  }

  /**
   * This is a mock method, which initializes the opening game splash screen Users can select a game
   * from a list of drop downs and once selected, the selectLevel method is called
   */
  private void createStartSplashScreen() {
    // The game view creates opening splash screen
    ComboBox<String> levelOptions = new ComboBox<>();
    String level = levelOptions.getValue();
    // COLLABORATION #1: calls GameManager to select a level based on the drop down option selected
    levelOptions.setOnAction(GameManager.selectLevel(level));
  }

  /**
   * Method of the GameManager class to select a level and update the vew accordingly
   *
   * @param level - the level selected
   */
  @Override
  public void selectLevel(String level) {
    LevelData levelData = fileParser.loadFileToEngine(level);
    gameController.loadLevel(levelData);
    gameView.updateView();
    gameController.updateGameState();
  }


  /**
   * GameView mock updateView method, which gets updated objects from the gameController
   */
  @Override
  public void updateView() {
    List<GameObject> gameObjects = gameController.getUpdatedObjects();
    // COLLABORATION #2: collaborates with the gameView class to render the updated game objects
    gameView.renderGameObjects(gameObjects);
  }

  /**
   * Initializes the mock timeline
   */
  private void initTimeline() {
    Timeline timeline = new Timeline();
    timeline.setCycleCount(Timeline.INDEFINITE);
    timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(secondDelay), e -> step())
  }

  /**
   * When a user clicks the play button, the step method will be called on each tick The timeline
   * will be initialized in a private method of the GameManager class
   */
  @Override
  public void play() {
    timeline.play();
  }

  /**
   * Internal method of the GameManager class, which is called by the timeline Controlled by the
   * GameManagerAPI
   */
  private void step() {
    gameController.updateGameState();  // Update game logic
    gameView.updateView() // Render updated game state
  }

  /**
   * Mock GameObject class In a real implementation, this would implement a GameObject interface
   */
  private class MockGameObject implements GameObject {

    String type;
    int x;
    int y;

    MockGameObject(String type, int x, int y) {
      this.type = type;
      this.x = x;
      this.y = y;
    }
  }

  /**
   * Mock implementation of GameControllerAPI
   */
  private class MockGameControllerAPI implements GameControllerAPI {

    private List<GameObject> gameObjects;
    private LevelData levelData;

    MockGameControllerAPI() {
      levelData = new MockLevelData();
      gameObjects = levelData.getGameObjects();
    }

    @Override
    public List<GameObject> getUpdatedObjects() {
      List<GameObject> updatedObjects = new ArrayList<>();
      for (GameObject obj : gameObjects) {
        if (obj.isUpdated()) {
          updatedObjects.add(obj);
        }
      }
      gameObjects = updatedObjects;
      return gameObjects;
    }

    @Override
    public void updateGameState() {
      for (GameObject obj : gameObjects) {
        inputHandler.update(obj);
        physicsHandler.update(obj);
        collisionHandler.update(obj);
      }
      System.out.println("GameControllerAPI: Game state updated");
    }

    @Override
    public void loadLevel(LevelData data) {
      levelData = level.getLevelObjects();
      System.out.println("GameControllerAPI: Level initialized with " + data);
    }
  }
}