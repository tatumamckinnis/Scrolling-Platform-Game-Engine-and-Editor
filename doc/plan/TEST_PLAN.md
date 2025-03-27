# OOGA Test Plan

### It's Thyme to Eat: Salad

#### Alana, Aksel, Tatum, Gage, Jacob, Billy, Luke

#### Strategy to make APIs more testable:

##### Strategy #1: Custom Model Exception Class

A custom exception hierarchy with ModelException as the base class. This allows for specific exception types that can be tested for, like InvalidGameObjectException or LevelLoadException. Each exception includes detailed error messages and error codes, making it easier to verify that the correct error was thrown during testing.

##### Strategy #2: Interface-Based Design

We designed our system around well-defined interfaces rather than concrete implementations. This allows us to create mock implementations of dependencies during testing, isolating the component under test. For example, our GameManager interacts with the view through the GameAppView interface, allowing us to substitute a TestGameView that verifies the correct method calls without rendering anything to the screen.

1. Editor File API Test Scenarios

Test Scenario #1: Successful Save

* Action: Call saveEditorDataToFile() with valid editor data
* Expected outcome: Method returns true, file exists at expected location
* Design support: Return value enables assertion that save was successful.

Test Scenario #2: Successful Load

* Action: Call loadFileToEditor() with path to valid file
* Expected Outcome: Editor state contains objects from file, method returns true
* Design support: Can verify editor objects match expected test file data

Test Scenario #3: Save Empty Project

* Action: Call saveEditorDataToFile() with no objects in editor
* Expected Outcome: Returns true, creates valid empty project file
* Design support: Edge case handling ensures minimum valid state

Test Scenario #4: Load Invalid File (Negative)

* Action: Call loadFileToEditor() with corrupt XML file
* Expected outcome: Throws DataFormat Exception with appropriate error message
* Design Support: Exception hierarchy provides specific failure details

2. Engine File API Test Scenarios

Test Scenario 1: Save Game Progress

* Action: Call saveLevelStatus() after game progression
* Expected Outcome: Returns true, creates file with correct game state
* Design Support: Return value confirms operation success

Test Scenario 2: Load Level

* Action: Call loadFileToEngine() with valid level file
* Expected Outcome: Engine loads level with correct objects and properties
* Design Support: Can verify engine state matches expected test level

Test Scenario 3: Save with Insufficient Permissions (Negative)

* Action: Call saveLevelStatus() to restricted location
* Expected Outcome: Throws IOException with "Permission denied" message
* Design Support: Exception handling provides specific error information

Test Scenario 4: Load Non-existent File (Negative)

* Action: Call loadFileToEngine() with non-existent file
* Expected Outcome: Throws FileNotFoundException
* Design Support: Exception type identifies specific error condition

3. Game Manager API Test Scenarios

Test Scenario 1: Game Loop Update

* Action: Call play() followed by checking updated objects
* Expected Outcome: Game state advances, updated objects list contains changes
* Design Support: getUpdatedObjects() provides testable list of changed objects

Test Scenario 2: Pause and Resume

- Action: Call play(), pause(), and play() in sequence
- Expected Outcome: Timeline pauses and resumes correctly
- Design Support: Can verify timeline status after each call

Test Scenario 3: Game Restart

- Action: Call restartGame() After Gameplay
- Expected Outcome: Game state resets to initial conditions
- Design Support: Can verify object positions, scores, and game state

Test Scenario 4: Load Invalid Level (Negative)

* Action: Call loadLevel() with malformed level data
* Expected Outcome: Throws LevelLoadException with details
* Design Support: Exception provides specific error information

4. Game App View Test Scenarios

Test Scenario 1: Update View

* Action: Call updateView() after making changes to game state
* Expected Outcome: Visual representation updates to reflect current game state
* Design Support: Rendering system refreshes display with latest object states

Test Scenario 2: Initialize View

* Action: Call initialize("Game Title", 800, 600) with valid parameters
* Expected Outcome: Game window created with specified title and dimensions
* Design Support: Configuration parameters properly set up the display system

Test Scenario 3: Render Game Objects

* Action: Call renderGameObjects(updatedObjects) with list of modified objects
* Expected Outcome: Only the changed objects are redrawn on screen
* Design Support: Selective rendering improves performance

Test Scenario 5: Get Current Inputs

* Action: Simulate key presses and call getCurrentInputs()
* Expected Outcome: Returns list containing currently active input identifiers
* Design Support: Input detection provides actionable input state

5. Editor Manager API Test Scenarios

Test Scenario 1: Add Object

* Action: Call addObject() with new game object
* Expected Outcome: Object appears in editor scene
* Design Support: Can verify object count and retrieve added object

Test Scenario 2: Get Object at Position

* Action: Call getObject(x, y) where test object was placed
* Expected Outcome: Returns the correct EditorObject
* Design Support: Return value can be compared to expected object

Test Scenario 3: Update Object Properties

* Action: Change object properties and verify with getters
* Expected Outcome: getX() and getName() return updated values
* Design Support: Property getters enable value verification

Test Scenario 4: Get Object at Empty Location (Negative)

* Action: Call getObject(x, y) at coordinates with no object
* Expected Outcome: Returns null
* Design Support: Null return value indicates absence clearly

6. Game Data Parser API Test Scenarios

Test Scenario 1: Parse Valid Level File

* Action: Call parseLevelFile(File file) with a well-formed XML file
* Expected Outcome: Returns GameLevel object populated with correct game objects and properties
* Design Support: Return value can be inspected to verify level structure matches input file

Test Scenario 2: Save Level To File

* Action: Call saveLevelToFile(GameLevel level, File file) with a valid level object
* Expected Outcome: Creates an XML file with structure corresponding to the level data
* Design Support: Generated file can be examined to verify XML structure

Test Scenario 3: Validate Well-Formed File

* Action: Call validateFormat(File file) on a valid level file
* Expected Outcome: Returns empty list indicating no errors found
* Design Support: Validation mechanism provides clear feedback on file validity

Test Scenario 4: Validate Malformed File

* Action: Call validateFormat(File file) on a file with syntax errors
* Expected Outcome: Returns list of strings describing specific format errors
* Design Support: Error detection provides actionable validation feedback

7. Audio API

Test Scenario 1: Basic Sound Effect Playback

* Action: Call playSound("explosion", 1.0, false) then check if sound is playing
* Expected Outcome: Explosion sound plays once at full volume, implementation logs show correct file loaded
* Design Support: Implementation can track active sounds and provide a method like isPlaying(soundId) to verify playback status

Test Scenario 2: Music Control Flow

* Action: Call sequence: playMusic("theme", true) → pauseMusic() → resumeMusic() → stopMusic()
* Expected Outcome: Music plays, pauses, resumes from same position, then stops completely
* Design Support: Implementation can provide getMusicState() and getMusicPosition() to verify state transitions

Scenario 3: Multiple Sound Management

* Action: Play ambient and footstep sounds concurrently, then pause footsteps and stop ambient
* Expected Outcome: Both sounds play simultaneously, then footsteps pause while ambient continues, and finally ambient stops
* Design Support: Implementation tracks each sound independently, provides getSoundState(soundId) to verify individual states

Scenario 4: Invalid Sound ID (Negative Case)

* Action: Call playSound("nonexistent", 1.0, false) with an ID that doesn't exist
* Expected Outcome: No sound plays, implementation logs warning, game continues normally
* Design Support: Error handling mechanism, error logging capabilities, sound registry validation
