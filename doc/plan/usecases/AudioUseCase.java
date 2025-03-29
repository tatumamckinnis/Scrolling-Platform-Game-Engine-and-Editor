/**
 * Use Case: User selects a new game and can configure to play, pause, stop, and resume audio
 * <p>
 * API Collaboration:
 * 1. The Audio API works with GameManager to coordinate audio events during gameplay for special
 * events such as "game over," or activating a power-up.
 * 2. To a lesser extent, the Audio API will be linked to the GameView, since clicking certain
 * buttons on the view, such as clicking a play/pause audio button with be linked to the Audio
 * API using the GameManager to communicate between the Audio and GameView API. Of course, player
 * actions shown in the view should be reflected by the manager.
 * 3. The Audio API should also collaborate with other APIs for file management, since there will
 * be resource folders containing audio files for each game.
 *
 * @author Luke Nam
 */

public interface AudioUseCase {

  private GameView gameView;
  private GameManager gameManager;
  private Audio gameAudio;

  /**
   * When we load the files for the Audio API, we have to communicate with file managing classes
   * to verify that all the audio files have been properly loaded.
   */
  public void setup() {
    gameView = new MockGameView();
    gameManager = new MockGameManager();
    gameAudio = new MockAudio();
  }

  /**
   * First, we can try to simulate sound effects as if we were playing a game. If we take the
   * Chrome Dinosaur game as an example, let's say that we press "Space" to get the dinosaur to
   * "jump". Consequently, that explains the line gameAudio.playSound("jump", 0.85, false);
   * <p>
   * This provides an opportunity to collaborate. The GameManager, after processing the "Space"
   * input, can call the Audio API to play the selected sound effect. We can repeat this process
   * for other sound effects like "powerup" (which can be looped multiple times, depending on the
   * powerup duration).
   * <p>
   * Similarly, we can test the "pause" and "resume" functionalities when the user pauses their
   * game, as well as stopping all sounds when the user ends the game, whether by reaching the end
   * screen or closing their application.
   */
  public void simulateSoundEffectControls() {
    try {
      gameAudio.playSound("jump", 0.85, false);
      gameAudio.playSound("powerup", 0.9, true);
      gameAudio.pauseSound("jump");
      gameAudio.resumeSound("jump");
      gameAudio.stopSound("powerup");
      gameAudio.stopSound("jump");
      gameAudio.playSound("levelComplete", 1, false);
    } catch (AudioNotFoundException e) {
      gameView.showErrorPopUp(e.getMessage());
    }
  }

  /**
   * Similar to the documentation for the sound effect simulation method, we can collaborate with
   * the GameManager class to play and stop the background "theme" music while the user plays the
   * game. Of course, we can pause and resume the music if the user pauses their menu or chooses
   * the respective button on the GameView.
   */
  public void simulateBackgroundMusicControls() {
    try {
      gameAudio.playMusic("theme", true);
      gameAudio.pauseMusic("theme");
      gameAudio.resumeMusic("theme");
      gameAudio.stopMusic("theme");
    } catch (AudioNotFoundException e) {
      gameView.showErrorPopUp(e.getMessage());
    }
  }

  private class MockAudio implements Audio {
    private static final String soundEffectsFilepath = "src/main/resources/soundEffects/";
    private static final String backgroundMusicFilepath = "src/main/resources/backgroundMusic/";
    Map<String, AudioClip> soundEffects = new Map();
    Map<String, MediaPlayer> backgroundMusic = new Map();

    public MockAudio() {
      loadAndMapSoundEffects(soundEffectsFilepath, soundEffects);
      loadAndMapBackgroundMusic(backgroundMusicFilepath, backgroundMusic);
    }

    /**
     * Plays a sound effect once or in a loop.
     *
     * @param soundId identifier for the sound effect
     * @param volume  volume level from 0.0 (mute) to 1.0 (full volume)
     * @param loop    if true, the sound effect loops continuously
     */
    @Override
    public void playSound(String soundId, double volume, boolean loop) throws AudioNotFoundException {
      AudioClip selectedSound = soundEffects.get(soundId);
      if (selectedSound == null) {
        throw new AudioNotFoundException("No sound effect found with id " + soundId);
      } else {
        selectedSound.setCycleCount(loop ? MediaPlayer.INDEFINITE : 1);
        selectedSound.setVolume(volume);
        selectedSound.play();
      }
    }

    /**
     * Stops a currently playing sound effect.
     *
     * @param soundId identifier for the sound effect
     */
    @Override
    public void stopSound(String soundId) throws AudioNotFoundException {
      AudioClip selectedSound = soundEffects.get(soundId);
      if (selectedSound == null) {
        throw new AudioNotFoundException("No sound effect found with id " + soundId);
      } else {
        selectedSound.stop();
      }
    }

    /**
     * Pauses a currently playing sound effect.
     *
     * @param soundId identifier for the sound effect
     */
    @Override
    public void pauseSound(String soundId) throws AudioNotFoundException {
      AudioClip selectedSound = soundEffects.get(soundId);
      if (selectedSound == null) {
        throw new AudioNotFoundException("No sound effect found with id " + soundId);
      } else {
        applyPauseOnAudio(selectedSound);
      }
    }

    /**
     * Resumes a paused sound effect.
     *
     * @param soundId identifier for the sound effect
     */
    public void resumeSound(String soundId) throws AudioNotFoundException {
      AudioClip selectedSound = soundEffects.get(soundId);
      if (selectedSound == null) {
        throw new AudioNotFoundException("No sound effect found with id " + soundId);
      } else {
        applyResumeOnAudio(selectedSound);
      }
    }

    /**
     * Plays background music, optionally looping.
     *
     * @param musicId identifier for the music track
     * @param loop    if true, the track loops continuously
     */
    @Override
    public void playMusic(String musicId, boolean loop) throws AudioNotFoundException {
      MediaPlayer selectedMusic = backgroundMusic.get(musicId);
      if (selectedMusic == null) {
        throw new AudioNotFoundException("No background music found with id " + musicId);
      } else {
        selectedMusic.start();
      }
    }

    /**
     * Stops the currently playing background music, if any.
     */
    @Override
    public void stopMusic() throws AudioNotFoundException {
      MediaPlayer selectedMusic = backgroundMusic.get(musicId);
      if (selectedMusic == null) {
        throw new AudioNotFoundException("No background music found with id " + musicId);
      } else {
        selectedMusic.stop();
      }
    }

    /**
     * Pauses the currently playing background music, if any.
     */
    @Override
    public void pauseMusic() throws AudioNotFoundException {
      MediaPlayer selectedMusic = backgroundMusic.get(musicId);
      if (selectedMusic == null) {
        throw new AudioNotFoundException("No background music found with id " + musicId);
      } else {
        applyPauseOnAudio(selectedMusic);
      }
    }

    /**
     * Resumes a paused background music track, if any.
     */
    @Override
    public void resumeMusic() throws AudioNotFoundException {
      MediaPlayer selectedMusic = backgroundMusic.get(musicId);
      if (selectedMusic == null) {
        throw new AudioNotFoundException("No background music found with id " + musicId);
      } else {
        applyResumeOnAudio(selectedMusic);
      }
    }
  }
}