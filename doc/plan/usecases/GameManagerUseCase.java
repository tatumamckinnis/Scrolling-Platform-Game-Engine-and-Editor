/**
 * Use Case: Managing game loop
 *
 * This use case demonstrates how the GameManager API is used to:
 * 1. Pause/play the game loop
 *
 *
 * API Collaboration:
 * - used by GameAppView: To handle gameLoop related behavior coming from the view(pause, resume, etc.)
 * - uses GameAppView: To render updated game objects each frame
 * - uses GameController: To update Game state each frame
 * - uses LevelManager: to handle view calls to change level data
 * -
 * @author Gage Garcia
 */
public interface GameManagerInterface {
    void play();
    void pause();
    void resetLevel();
    void loadLevel(Level);
}



public class GameManagerUseCase  {
    private GameManager gameManager;
    private EventAPI eventAPI;
    private GameView gameView;
    private EventChainRegistry eventChainRegistry;

    public GameManagerUseCase() {
        GameManager gameManager = new GameManager();
        GameView gameView = new GameView();
        //Unsure of how represent registered event chain of button -> pauseGame()/playGame()
    }

    /**
     * Representing chain of events required to pause running game instance
     */
    public void pause() {
        KeyCode key = GameView.getUserInput();
        EventChain eventChain = eventChainRegistry.getChain(key);
        EventAPI.execute(eventChain);
        //Event execution will call
        gameManager.pause();
    }

    /**
     * Representing chain of events required to run game instance
     */
    public void pause() {
        KeyCode key = GameView.getUserInput();
        EventChain eventChain = eventChainRegistry.getChain(key);
        EventAPI.execute(eventChain);
        //Event execution will call
        gameManager.play();
    }



}