# OOGA Backlog

### It's Thyme to Eat: Salad

### Alana, Aksel, Tatum, Gage, Jacob, Billy, Luke

## ENGINE MODEL


| ID       | Name                                                     | Priority   | Description                                                                                                                 |
| -------- | -------------------------------------------------------- | ---------- | --------------------------------------------------------------------------------------------------------------------------- |
| OOGA-1   | Select new game to play from existing games              | Core       | User is able to select a new game to play from list of 6 dropdown games                                                     |
| OOGA-2   | Game 1: Easy to Play Game (Dinosaur Jump)                | Core       | User can play Dinosaur Jump game with basic ruleset                                                                         |
| OOGA-3   | Game 2: Complete Game 1 (Super Mario Bros)               | Core       | User can play Super Mario Bros game with basic ruleset                                                                      |
| OOGA-4   | Game 3: Complete Game 1 (Doodle Jump)                    | Core       | Users can play doodle jump                                                                                                  |
| OOGA-5   | MOD game: Geometry Dash                                  | Core       | Users can play geometry dash                                                                                                |
| OOGA-6   | Alana Game                                               | Core       |                                                                                                                             |
| OOGA-7   | Tatum Game                                               | Core       |                                                                                                                             |
| OOGA-8   | Luke Game                                                | Core       |                                                                                                                             |
| OOGA-9   | Gage Game                                                | Core       |                                                                                                                             |
| OOGA-10  | Aksel Game                                               | Core       |                                                                                                                             |
| OOGA-12  | Jacob Game                                               | Core       |                                                                                                                             |
| OOGA-13  | Billy Game                                               | Core       |                                                                                                                             |
| OOGA-14  | Game Loop                                                | Core       | Game loop can be played, paused, and reset                                                                                  |
| OOGA-15A | Camera Definition                                        | Core       | Define a camera that defines the scope of the scene displayed to the user                                                   |
| OOGA-15B | Camera (Player Centric) Movement                         | Core       | When player moves, Camera moves in that direction                                                                           |
| OOGA-15C | Camera (Time Centric) Movement                           | Core       | When time progresses, Camera moves at defined pace                                                                          |
| OOGA-15D | Camera (Player Centric) Pause                            | Core       | When Player position does not change, Camera position does not change                                                       |
| OOGA-15E | Camera (Time Centric) speedup/slow down                  | Extension  | When player speeds/up slows down game, Camera movement speeds up                                                            |
| OOGA-16A | Collision: Player and Enemy                              | Core       | When a player collides with an enemy, a specific action should occur depending on the game.                                 |
| OOGA-16B | Collision: Player and Block                              | Core       | When a player collides with a block or non-enemy object, the player’s position should not change according to some ruleset |
| OOGA-16C | Collision: Player and Wall                               | Core       | A player should remain within the bounds of the walls                                                                       |
| OOGA-17  | User Progress                                            | Core       | User can track their progress, including score, lives, and progress towards game goal                                       |
| OOGA-18  | Win/Lose Game                                            | Core       | Progress is compared against game goal to determine when game is won                                                        |
| OOGA-19  | Physics: Player Move Horizontally                        | Core       | Player can move to the left and right                                                                                       |
| OOGA-20  | Physics: Player Move Vertically                          | Core       | Player can jump up and down (simultaneously with horizontal movement)                                                       |
| OOGA-21  | Physics: Player Stand On Objects                         | Core       | Player vertical position remains above the block                                                                            |
| OOGA-22  | Physics: Gravity                                         | Core       | Player vertical position decreases when not supported                                                                       |
| OOGA-23  | Physics: Platform Object Moves Horizontally              | Core       | Platform object moves left and right                                                                                        |
| OOGA-24  | Move to Next Level - Discrete Levels                     | Core       | When player completes a level, the game pauses and moves onto next level                                                    |
| OOGA-25  | Level Difficulty Increases                               | Core       | Each level becomes progressively more challenging                                                                           |
| OOGA-26  | Power-up 1                                               | Core       | Player horizontal movement speed-up                                                                                         |
| OOGA-27  | Power-up 2                                               | Core       | Player horizontal jump increase                                                                                             |
| OOGA-28  | Power-up 3                                               | Core       | Score multiplier                                                                                                            |
| OOGA-29  | Player receives power-ups when hitting a specific object | Core       |                                                                                                                             |
| OOGA-30  | Player receives power-ups upon collision                 | Core       |                                                                                                                             |
| OOGA-31  | Interactable Objects Initialized for Each Game           | Core       | Foreground objects initialized based on configuration                                                                       |
| OOGA-32  | Action Effects                                           | Core       | Actions built into the scene                                                                                                |
| OOGA-33  | Define a goal for a game                                 | Core       | Each game has a specific goal                                                                                               |
| OOGA-34  | Randomization: Continuous Level                          | Extension? | Objects randomly placed for continuous gameplay                                                                             |
| OOGA-35  | Define different player avatars                          | Extension  | Players can choose avatars                                                                                                  |

## DATA


| ID      | Name             | Priority | Description                               |
| ------- | ---------------- | -------- | ----------------------------------------- |
| OOGA-36 | Parse level file | Core     | Parses config file into Record format     |
| OOGA-37 | Save level file  | Core     | Converts editor graph to config file      |
| OOGA-38 | Handle Errors    | Core     | Display error messages for invalid config |

## ENGINE VIEW


| ID      | Name                        | Priority  | Description                              |
| ------- | --------------------------- | --------- | ---------------------------------------- |
| OOGA-39 | Splash Screen               | Core      | Display splash screen                    |
| OOGA-40 | Win/Lose Game Splash Screen | Core      | Display win/lose game status             |
| OOGA-41 | Player View                 | Core      | Display player object                    |
| OOGA-42 | Objects                     | Core      | Display game objects                     |
| OOGA-43 | Background                  | Core      | Load non-collidable background           |
| OOGA-44 | Pause                       | Core      | User can pause game                      |
| OOGA-45 | Play                        | Core      | User can play game                       |
| OOGA-46 | Restart                     | Core      | User can restart game                    |
| OOGA-47 | Select new game             | Core      | User selects new game from list          |
| OOGA-48 | User Progress               | Core      | Display user progress                    |
| OOGA-49 | Score                       | Core      | Display user score                       |
| OOGA-50 | Object Changes              | Core      | Sprite changes when object partially hit |
| OOGA-51 | Object Destroyed            | Core      | Object removed when destroyed            |
| OOGA-52 | Enemy Collision             | Core      | Player loses life upon enemy collision   |
| OOGA-75 | Add Sound Effects           | Extension | Play sound effects                       |
| OOGA-53 | Open/Close Control Bar      | Extension | User controls control panel visibility   |
| OOGA-54 | Change player avatar        | Extension | Change player sprite                     |
| OOGA-55 | Change Volume               | Extension | Adjust sound effect volume               |

## GAME EDITOR MODEL


| ID      | Name                        | Priority  | Description                                                                 |
| ------- | --------------------------- | --------- | --------------------------------------------------------------------------- |
| OOGA-56 | Represent a Game Object     | Core      | Represent underlying structure of object                                    |
| OOGA-57 | Represent a Level Object    | Core      | Represent level details                                                     |
| OOGA-58 | Convert Editor Model to XML | Core      | Save objects/level as XML file                                              |
| OOGA-73 | Help Documentation          | Extension | Provide Web-based HTML help documentation to list different games and rules |
| OOGA-76 | Style Non-Game Elements     | Core      | Style the non-game elements (buttons, text, etc.)                           |

## GAME EDITOR VIEW


| ID      | Name                     | Priority  | Description                                                              |
| ------- | ------------------------ | --------- | ------------------------------------------------------------------------ |
| OOGA-59 | Display Game View        | Core      | Display a grid where visual game elements can be added and updated       |
| OOGA-60 | Add Entity Object        | Core      | Allow users to add a new game object to the scene                        |
| OOGA-61 | Add Enemy Object         | Core      | Allow users to add a new enemy object to the scene                       |
| OOGA-62 | Add Collision Properties | Core      | Allow users to determine the collision property of an object             |
| OOGA-63 | Add Movement Properties  | Core      | Allow users to determine the movement pattern of an object               |
| OOGA-64 | Add Position Property    | Core      | Allow users to determine the position of an object within the game scene |
| OOGA-65 | Save                     | Core      | Allow user to save level as new file                                     |
| OOGA-66 | Reset                    | Core      | Allow user to reset the level scene                                      |
| OOGA-67 | Lives                    | Extension | Allow the user to set the number of lives for the player                 |
| OOGA-68 | Key Strategies           | Extension | Allow the user to determine input key strategies                         |
| OOGA-74 | Help Documentation       | Extension | Provide web-based HTML help documentation about how to use the editor    |
| OOGA-69 | Dynamic Game Rules       | Extension | Game rules can change during the game                                    |
| OOGA-70 | Player Profiles          | Extension | Users can change their profile image or name                             |
| OOGA-71 | Easy MOD Editing         | Extension |                                                                          |
| OOGA-72 | Save Game Data in Web    | Extension |                                                                          |

## GAME SPECIFIC FEATURES


| ID      | Name                  | Priority | Description                                         |
| ------- | --------------------- | -------- | --------------------------------------------------- |
| OOGA-2A | Dinosaur Game Sprites | Core     | Create Dinosaur Game Front-End objects              |
| OOGA-3A | Super Mario Sprites   | Core     | Create Super Mario Front-End objects                |
| OOGA-3B | Super Mario           | Core     | Create Super Mario Back-End Objects                 |
| OOGA-5A | Geometry Dash Objects | Core     | Create Geometry Dash Front-End objects              |
| OOGA-4A | Doodle Jump Objects   | Core     | Create Doodle Jump Back-End Objects                 |
| OOGA-4B | Doodle Jump Sprites   | Core     | Create Doodle Jump Front-End Objects                |
| OOGA-4C | Doodle Jump Camera    | Core     | Camera can move vertically rather than horizontally |

# Use Cases


### Team Member #1 

Name: Jacob

Use Case 1: Load a level file

Use Case 2: Handle a collision between two objects

Use Case 3:  Handle movement of player

Use Case 4: Change object sprite

Use Case 5: Win game

Use Case 6: Destroy an entity

### Team Member #2

Name: Billy

Use Case 1: Camera Zoom

The camera should have the ability to zoom in and out within the window.

Use Case 2: Game Editor Sprites

All the Static Sprites should be showed in a panel within the game editor, allowing a user to select a sprite to place

Use Case 3: Lose a game

Use Case 4: Pause game

Use Case 5: Reset game

Use Case 6: Update game one tick

### Team Member #3

Name: Alana

Use Case 1: (Editor) add object to game

1. User clicks on outline of object in game editor lower panel
2. User clicks on the game view grid square for where they want to place object
3.

Use Case 2: (Editor) Show level scene

Use Case 3: (Editor) Save level

Use Case 4: (Editor) Change background

Use Case 5: (Editor) Change collision handler

Use Case 6: (Editor) Destroy an object

### Team Member #4

Name: Aksel

Use Case 1: (Engine) Show opening splash screen

Use Case 2: (Engine) Update score

Use Case 3: (Engine) Show game over splash screen

Use Case 4: (Engine) Update entity position

Use Case 5: (Engine) Change camera position

Use Case 6: (Engine) Entity animation

### Team Member #5:

Name: Tatum McKinnis

Use Case 1: (Engine) handle player input

1. User presses a key while playing the game
2. PlayerInputHandler captures this input
3. The handler translates the input into game actions (jump, move, etc.)
4. These actions are sent to the appropriate GameObject’s InputHandler
5. The GameObject responds based on its configured behavior

Implementation Approach:

* Use event listening system in GameApp to capture JavaFX input events
* Implement the PlayerInputHandler to use the mapping between key presses and game actions from the data files
* Connect the global PlayerInputHandler to the individual InputHandler components attached to GameObjects
* Process input events in the GameLoop update cycle

Use Case 2: (Engine) update player position

1. After input is pressed, the GameLoop.update() calls update on all active GameObjects
2. For the player GameObject, its PhysicsHandler calculates the new position
3. The CollisionHandler checks if the new position would cause collisions
4. Position is updated or adjusted based on collision results
5. The updated position is stored for rendering

Implementation Approach:

* PhysicsHandler handles movement calculations
* GameLoop update system processes all active GameObjects
* Update GameObject through component system

Use Case 3: (Engine) Parse level

1. User selects a game to play
2. LevelLoader reads the game’s (JSON?) file
3. The ProcessLevelStrategy with EngineStrategy implementation converts data to game objects
4. GameObjects are created with their appropriate Handlers
5. Level objects is populated with these GameObjects
6. GameLoop begins with the populated Level

Implementation Approach:

* LevelLoader should parse data file
* EngineStrategy implements ProccesLevelStrategy interface
* Level loading process is connected to game initialization in GameApp

Use Case 4: (Engine) Change camera movement pattern

1. The Level contains the Camera configuration from the level file
2. Camera has a Movement Type
3. During the GameLoop, the Camera’s position is updated based on its movement type
4. This information is used by GameRenderer to determine the viewport

Implementation Approach:

* Camera class has MovementType property
* Connect camera to rendering system in our architecture
* Ensure camera configuration is properly loaded from level files

Use Case 5: (Engine) Open editor

1. User selects “Edit” option while playing a game
2. Current game state is saved using LevelSaver
3. GameEditor application is launched
4. LevelLoader loads the game file
5. EditorStrategy implementation of ProcessLevelStrategy converts the game data to editor UI

Implementation Approach:

* Connection between GameApp and editor application
* Use FileManager classes that are shared between editor and engine
* Implement mechanism to launch editor from engine

Use Case 6: (Editor) Select new level

1. User selects “New level” in Game Editor
2. A dialog collects basic level settings
3. A new empty level grid is created using editor classes
4. When saved, LevelSaver creates a new level file

Implementation Approach

* Implement the UI for creating new levels in the editor
* Use editor controllers to manage creation process
* Connect LevelSaver component to persist the new level

### Team Member #6:

Name: Luke

Use Case 1: (Editor) Change between foreground and background

Use Case 2: (Editor) Quit game

Use Case 3: (Editor) assign sprite

Use Case 4: (Editor) Get level options

Use Case 5: (Editor) Get all game objects

Use Case 6: Create a front-end game object

### Team Member #7:

Name:

Use Case 1: Update front-end of game object

Use Case 2: Exploding event

Use Case 3: Make a new goal

Use Case 4: Add game checkpoint in the game

Use Case 5: Change level ordering

Use Case 6: Get a powerup
