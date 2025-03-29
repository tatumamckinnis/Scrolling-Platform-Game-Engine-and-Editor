# AudioAPI

## Design Goals
- Provide a unified interface for playing, pausing, stopping, and resuming audio without tying the game logic directly to a specific audio library.
- Only handle audio playback commands, leaving game logic, file paths, and event handling to the game’s model or managers.
- Allows future addition of advanced audio features without modifying existing method signatures.
- Different audio engines can implement the same AudioAPI interface without changing game logic code.

### How Programmers Should Use It
- **Trigger Sound Effects**: From game events by calling `playSound("explosion", 1.0, false)`.
- **Manage Background Music**: Start or stop continuous music with `playMusic("theme", true)`.
- **No Knowledge of Implementation**: The model or manager does not care if this uses JavaFX’s MediaPlayer, OpenAL, or any other library.

## Classes

```java
package managers.audio;

public interface AudioAPI {

    /**
     * Plays a sound effect once or in a loop.
     *
     * @param soundId identifier for the sound effect
     * @param volume volume level from 0.0 (mute) to 1.0 (full volume)
     * @param loop if true, the sound effect loops continuously
     */
    void playSound(String soundId, double volume, boolean loop);

    /**
     * Stops a currently playing sound effect.
     *
     * @param soundId identifier for the sound effect
     */
    void stopSound(String soundId);

    /**
     * Pauses a currently playing sound effect.
     *
     * @param soundId identifier for the sound effect
     */
    void pauseSound(String soundId);

    /**
     * Resumes a paused sound effect.
     *
     * @param soundId identifier for the sound effect
     */
    void resumeSound(String soundId);

    /**
     * Plays background music, optionally looping.
     *
     * @param musicId identifier for the music track
     * @param loop if true, the track loops continuously
     */
    void playMusic(String musicId, boolean loop);

    /**
     * Stops the currently playing background music, if any.
     */
    void stopMusic();

    /**
     * Pauses the currently playing background music, if any.
     */
    void pauseMusic();

    /**
     * Resumes a paused background music track, if any.
     */
    void resumeMusic();
}
```

### Details
- **Playing a One-Off Sound Effect**: When a player jumps or a button is clicked, the game logic calls `playSound("jump", 0.8, false)`. The `AudioAPI` implementation handles looking up the file path and playing it at the requested volume.
- **Looping Background Music**: On game start, call `playMusic("mainTheme", true)`. The music loops until `stopMusic()` is invoked.
- **Pausing/Resuming**: If the user pauses the game, call `pauseMusic()` and `pauseSound("ambient")`. On unpausing, call `resumeMusic()` and `resumeSound("ambient")`.

### Collaboration
- **GameManagerAPI/GameManager**: May trigger audio events during gameplay.
- Actual audio files stored in a resource directory.
- A registry or config for mapping soundId / musicId to file paths.

### Considerations
- **Volume Management**: The interface currently sets volume per sound effect. A global volume or advanced mixing could be a future extension.
- **Assumptions**: The game logic always knows the correct `soundId` or `musicId`; the AudioAPI doesn’t do validation of IDs.