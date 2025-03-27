# OOGA Backlog

### It's Thyme to Eat: Salad

### Alana, Aksel, Tatum, Gage, Jacob, Billy, Luke

## ENGINE MODEL

| ID | Name | Priority | Description |
|----|------|----------|-------------|
| OOGA-1 | Select new game to play from existing games | Core | User is able to select a new game to play from list of 6 dropdown games |
| OOGA-2 | Game 1: Easy to Play Game (Dinosaur Jump) | Core | User can play Dinosaur Jump game with basic ruleset |
| OOGA-3 | Game 2: Complete Game 1 (Super Mario Bros) | Core | User can play Super Mario Bros game with basic ruleset |
| OOGA-4 | Game 3: Complete Game 1 (Doodle Jump) | Core | Users can play doodle jump |
| OOGA-5 | MOD game: Geometry Dash | Core | Users can play geometry dash |
| OOGA-6 | Alana Game | Core |  |
| OOGA-7 | Tatum Game | Core |  |
| OOGA-8 | Luke Game | Core |  |
| OOGA-9 | Gage Game | Core |  |
| OOGA-10 | Aksel Game | Core |  |
| OOGA-12 | Jacob Game | Core |  |
| OOGA-13 | Billy Game | Core |  |
| OOGA-14 | Game Loop | Core | Game loop can be played, paused, and reset |
| OOGA-15A | Camera Definition | Core | Define a camera that defines the scope of the scene displayed to the user |
| OOGA-15B | Camera (Player Centric) Movement | Core | When player moves, Camera moves in that direction |
| OOGA-15C | Camera (Time Centric) Movement | Core | When time progresses, Camera moves at defined pace |
| OOGA-15D | Camera (Player Centric) Pause | Core | When Player position does not change, Camera position does not change |
| OOGA-15E | Camera (Time Centric) speedup/slow down | Extension | When player speeds/up slows down game, Camera movement speeds up |
| OOGA-16A | Collision: Player and Enemy | Core | When a player collides with an enemy, a specific action should occur depending on the game. |
| OOGA-16B | Collision: Player and Block | Core | When a player collides with a block or non-enemy object, the player’s position should not change according to some ruleset |
| OOGA-16C | Collision: Player and Wall | Core | A player should remain within the bounds of the walls |
| OOGA-17 | User Progress | Core | User can track their progress, including score, lives, and progress towards game goal |
| OOGA-18 | Win/Lose Game | Core | Progress is compared against game goal to determine when game is won |
| OOGA-19 | Physics: Player Move Horizontally | Core | Player can move to the left and right |
| OOGA-20 | Physics: Player Move Vertically | Core | Player can jump up and down (simultaneously with horizontal movement) |
| OOGA-21 | Physics: Player Stand On Objects | Core | Player vertical position remains above the block |
| OOGA-22 | Physics: Gravity | Core | Player vertical position decreases when not supported |
| OOGA-23 | Physics: Platform Object Moves Horizontally | Core | Platform object moves left and right |
| OOGA-24 | Move to Next Level - Discrete Levels | Core | When player completes a level, the game pauses and moves onto next level |
| OOGA-25 | Level Difficulty Increases | Core | Each level becomes progressively more challenging |
| OOGA-26 | Power-up 1 | Core | Player horizontal movement speed-up |
| OOGA-27 | Power-up 2 | Core | Player horizontal jump increase |
| OOGA-28 | Power-up 3 | Core | Score multiplier |
| OOGA-29 | Player receives power-ups when hitting a specific object | Core |  |
| OOGA-30 | Player receives power-ups upon collision | Core |  |
| OOGA-31 | Interactable Objects Initialized for Each Game | Core | Foreground objects initialized based on configuration |
| OOGA-32 | Action Effects | Core | Actions built into the scene |
| OOGA-33 | Define a goal for a game | Core | Each game has a specific goal |
| OOGA-34 | Randomization: Continuous Level | Extension? | Objects randomly placed for continuous gameplay |
| OOGA-35 | Define different player avatars | Extension | Players can choose avatars |

## DATA
| ID | Name | Priority | Description |
|----|------|----------|-------------|
| OOGA-36 | Parse level file | Core | Parses config file into Record format |
| OOGA-37 | Save level file | Core | Converts editor graph to config file |
| OOGA-38 | Handle Errors | Core | Display error messages for invalid config |

## ENGINE VIEW
| ID | Name | Priority | Description |
|----|------|----------|-------------|
| OOGA-39 | Splash Screen | Core | Display splash screen |
| OOGA-40 | Win/Lose Game Splash Screen | Core | Display win/lose game status |
| OOGA-41 | Player View | Core | Display player object |
| OOGA-42 | Objects | Core | Display game objects |
| OOGA-43 | Background | Core | Load non-collidable background |
| OOGA-44 | Pause | Core | User can pause game |
| OOGA-45 | Play | Core | User can play game |
| OOGA-46 | Restart | Core | User can restart game |
| OOGA-47 | Select new game | Core | User selects new game from list |
| OOGA-48 | User Progress | Core | Display user progress |
| OOGA-49 | Score | Core | Display user score |
| OOGA-50 | Object Changes | Core | Sprite changes when object partially hit |
| OOGA-51 | Object Destroyed | Core | Object removed when destroyed |
| OOGA-52 | Enemy Collision | Core | Player loses life upon enemy collision |
| OOGA-75 | Add Sound Effects | Extension | Play sound effects |
| OOGA-53 | Open/Close Control Bar | Extension | User controls control panel visibility |
| OOGA-54 | Change player avatar | Extension | Change player sprite |
| OOGA-55 | Change Volume | Extension | Adjust sound effect volume |

## GAME EDITOR MODEL
| ID | Name                        | Priority | Description |
|----|-----------------------------|----------|-------------|
| OOGA-56 | Represent a Game Object     | Core | Represent underlying structure of object |
| OOGA-57 | Represent a Level Object    | Core | Represent level details |
| OOGA-58 | Convert Editor Model to XML | Core | Save objects/level as XML file |
| OOGA-73 | Help Documentation          | Extension | Provide Web-based HTML help documentation to list different games and rules|
| OOGA-76 | Style Non-Game Elements     | Core | Style the non-game elements (buttons, text, etc.)|


## GAME EDITOR VIEW
| ID | Name | Priority | Description |
|----|------|----------|-------------|
| OOGA-59 | Display Game View | Core | Display a grid where visual game elements can be added and updated |
| OOGA-60 | Add Entity Object | Core | Allow users to add a new game object to the scene |
| OOGA-61 | Add Enemy Object | Core | Allow users to add a new enemy object to the scene |
| OOGA-62 | Add Collision Properties | Core | Allow users to determine the collision property of an object |
| OOGA-63 | Add Movement Properties | Core | Allow users to determine the movement pattern of an object |
| OOGA-64 | Add Position Property | Core | Allow users to determine the position of an object within the game scene |
| OOGA-65 | Save | Core | Allow user to save level as new file |
| OOGA-66 | Reset | Core | Allow user to reset the level scene |
| OOGA-67 | Lives | Extension | Allow the user to set the number of lives for the player |
| OOGA-68 | Key Strategies | Extension | Allow the user to determine input key strategies |
| OOGA-74 | Help Documentation | Extension | Provide web-based HTML help documentation about how to use the editor |
| OOGA-69 | Dynamic Game Rules | Extension | Game rules can change during the game |
| OOGA-70 | Player Profiles | Extension | Users can change their profile image or name |
| OOGA-71 | Easy MOD Editing | Extension |  |
| OOGA-72 | Save Game Data in Web | Extension |  |


## GAME SPECIFIC FEATURES
| ID | Name | Priority | Description |
|----|------|----------|-------------|
| OOGA-2A | Dinosaur Game Sprites | Core | Create Dinosaur Game Front-End objects |
| OOGA-3A | Super Mario Sprites | Core | Create Super Mario Front-End objects |
| OOGA-3B | Super Mario | Core | Create Super Mario Back-End Objects |
| OOGA-5A | Geometry Dash Objects | Core | Create Geometry Dash Front-End objects |
| OOGA-4A | Doodle Jump Objects | Core | Create Doodle Jump Back-End Objects |
| OOGA-4B | Doodle Jump Sprites | Core | Create Doodle Jump Front-End Objects |
| OOGA-4C | Doodle Jump Camera | Core | Camera can move vertically rather than horizontally |




