# DESIGN Document for PROJECT_NAME

### It's Thyme to Eat: SALAD

### Alana Zinkin, Billy McCune, Tatum McKinnis, Jacob You, Gage Garcia, Aksel Bell, Luke Nam

## Team Roles and Responsibilities

* Team Member #1: Alana Zinkin

  * Product management: assigning of git issues, sprint planning, documentation (including writing
    JavaDoc comments), group organization and coordination
  * Engine Controller: integrating the back-end and front-end endpoints (GameManager and
    GameController APIs)
  * Language Selecting and Resource Management Refactoring
  * Level Selecting mechanism
  * Camera creation and object
  * LevelData conversion to Engine and Editor Objects (loading)
  * Rendering game objects in the front-end
  * Adding Engine and Editor View features such as buttons and actions, help views, editor tools,
    image rendering
  * Rendering Player Statistics
  * Refactoring view and display inheritance
* Team Member #2: Billy McCune

  * FileParser: developed and designed the fileParser package and modified the parsing when
    changes to the data system were made.
  * Data Designer: designed the sprite sheet data system and the level data xml
    formating. I also was the main designer for the blueprint system and layer system.
  * Rendering game objects in the front-end: Wrote the original code for handling front end object rendering and
    wrote the code for animation handling in the front end.
  * Event co-creator: Worked with Gage and by myself to create event outcomes and conditions and make the event system
    handle the condition set system to handle logical "or" and logical "and" conditions.
  * File saving: Edited the file saving system for the object properties and event properties.
  * User Data System designer and Implementor: Designed the User Data system allowing for users to create and modify profiles.
    I also made the userData tracking system. The session manager also makes sure session data is constant across panes. I
    also made the UserData front in panes as well.
  * Chat Bot: Designed and Built the chatBot Api and the Chat Bot pane in the editor.
  * Animation System Designer and Implementor: Designed the animationHandler, how animations are stored, the events for the animations, and the view handling of animations.
  * Refactoring Editor: During the last week, I spent time adding the editor settings windows. I also created the editors drag feature.
* Team Member #3: Jacob You

  * Engine Design: Spent time designing the layout of the engine, the data files, parameters, and
    events.
  * Editor Data Storage System
  * Editor Grid Zoom and Pan
  * Grid Line Toggle
  * Grid Snap To Grid
  * Editor Properties input tab
  * Editor Sprite Sheet Parsing, Loading, and Saving
  * Editor Sprite Sheet Rendering and Assigment
  * Creation of sprites, frames, and animations
  * Saving Data from Editor Level to XML
  * Level Property Editing
  * Managers to communicate between front end and editor data storage
  * Refactor Event Data Storage according to updated Event System
* Team Member #4: Tatum McKinnis

  * **Editor View Development:**
    * Implemented core components of the editor game view, including the canvas setup for grid and object rendering.
    * Designed and developed the grid visualization system, handling the display of grid lines and background.
    * Managed object rendering within the editor view, ensuring objects are displayed correctly based on their properties.
    * Developed the input handling logic for the editor view, processing user interactions such as object selection and tool usage. Specifically, worked on mouse event handling and translating screen coordinates to game world coordinates.
    * Integrated image management to efficiently load, cache, and display object images within the editor.
    * Addressed object display updates based on notifications from the controller, ensuring the view reflects changes in object properties and the addition/removal of objects.
  * **Editor Controller Development:**
    * Implemented the concrete editor controller, serving as the central mediator between the editor view and the data model.
    * Managed the editor's state, including the active tool, selected objects, and grid settings.
    * Handled user requests from the view, such as object placement, modification, and deletion, delegating to appropriate handler classes.
    * Developed the event notification system to communicate changes in the editor's state to registered view listeners.
    * Implemented prefab placement functionality, enabling users to add prefab instances to the editing environment.
  * **Event Listener System:** Developed and refined the event listener notification system, enabling communication between the editor controller and view components. This system ensures that the view accurately reflects changes in the editor's state.
  * **Prefab Integration:** Implemented prefab placement and handling within the editor, allowing users to place pre-designed objects into the level. This involved managing prefab selection, display, and interaction.
  * **Extensive Testing:**
    * Wrote numerous unit tests for the editor view components and editor controller logic.
    * Utilized the **TestFX** library to create and execute JavaFX UI tests, verifying the behavior of interactive elements within the editor view. Focused on ensuring the reliability and correctness of the editor's behavior.
  * **Editor Refactoring:** Undertook significant refactoring of major editor classes, specifically the editor game view and the concrete editor controller, breaking them down into more manageable subclasses. This improved the organization, maintainability, and extensibility of the editor's codebase.
* Team Member #5: Gage Garcia
* Team Member #6: Aksel Bell

  * Full stack engineer: worked on frontend of the engine, worked on backend file saving, and
    worked on challenge feature networked players which was backend but separate from the model.
  * Set up initial infrastructure for the frontend with the button action factory, view
    components, the setup of view screens, the abstract display class, the viewstate to keep MVC
    and classes tight
  * Worked on file saving. Added classes to convert leveldata to XML and also allow easy extension
    support for other types of files
  * Worked on multiplayer networked support. Created a replit javscript server (with a link in
    external properties file), set up network protocol of what information is passed through the
    server, set up client sockets to connect to the external javascript server, set up server
    message data structure, set up handling of the server messages with the server message
    factory.
* Team Member #7: Luke Nam

  * Front end developer:
    * Designed the splash screen for the OOGASalad application, from the JavaFX layout to the button styling
    * Drafted mock-up user interfaces throughout sprints to show new screens to add to the application, hosted on [Google Slides](https://docs.google.com/presentation/d/1nyUn-waZneKaREQaGtTVb95sLoyW5idqhHNOG3ka63g/edit?usp=sharing) for sharable editing
    * Helped design the engine button layout and icons
    * Refactored the win/pause/lose screens into an abstract class for reusability and adhering to DRY
    * Externalized pane element widths, heights, spacing, fonts, etc. using properties files
    * Similarly reduced reused code through CSS templates
    * Designed the multiplayer lobby front but wasn't implemented in final tag due to camera bugs
  * WebSocket support:
    * Helped integrate JavaScript functionality for multiplayer support on repl.it
    * Tested WebSocket functionality through System log experiments between two computers.

## Design Goals

* Goal #1: Our primary design goal was to make our architecture so flexible, that a user can play
  any time of scrolling-platform game they like, including Doodle Jump, Super Mario Bros, or
  Geometry Dash. Users should be able to mix game elements together and easily construct new games
  within our editor
* Goal #2: The underlying representation of all game objects should be encapsulated and separate
  from the view such that the View only receives the data necessary for rendering game objects. The
  front-end of both the editor and the engine should not know how objects are represented in the
  back-end
* Goal #3: Only the file parser should know the format of the configuration files such that future
  developers could use different file formats

#### How were Specific Features Made Easy to Add

* Feature #1: Easy to add new Events because the Event System is separated into conditions and
  outcomes, condition sets, and outcome sets, such that conditions and outcomes function as
  lego-like building blocks that can be put together to create events. Adding a new event requires
  extending either the Condition or Outcome class
* Feature #2: Easy to add new types of game objects because game objects have different string and
  double params maps for any other qualities needed and each game object has different Events,
  HitBox, and SpriteData such that they can look and act with any style and behavior
* Feature #3: Easy to add new types of data formats since the FileParserAPI is a functional
  interface and is the only class that knows what the file format is and the Saver uses a strategy
  pattern that is chosen based on the tag of the file

## High-level Design

#### Core Classes and Abstractions, their Responsibilities and Collaborators

* Class #1: The GameManagerAPI is responsible for transmitting information between the Model Game
  Controller which manages the engine model and the ViewAPI which handles the View of the Game
  Engine
* Class #2: The FileParserAPI parses each level file and transmits the data to the
  EngineFileConverterAPI and the EditorFileConverterAPI
* Class #3: The EditorDataAPI is responsible for managing editor data and communicating updates in
  the view to the model in the back-end and uses the EditorListenerNotifier API to transmit updates
  to different components of the view
* Class #4: Button action factory. This factory was core to any actions that were triggered from
  user mouse clicks. It abstracted how the button action was implemented, and allowed easier testing
  of the actions. Also, it used reflection so adding new buttons was simple (and didn't require
  modifications but just additions) if you had the button id from the resource files. It
  demonstrates understanding of the factory design pattern as well.
* Class #5: The ConcreteEditorController serves as the central coordinator within the editor environment. Its responsibilities include managing the active editing tool, handling user interactions within the editor view, and maintaining the selection state of objects. The controller delegates specific tasks to specialized handlers for object placement, prefab management, and event modifications. It also communicates with the EditorDataAPI to persist changes to the game data and uses an EditorListenerNotifier to keep the editor view synchronized with the current editor state.

## Assumptions or Simplifications

* Decision #1: Assumed that Editor and Engine would only be connected through the level files
  themselves rather than directly at runtime. This assumption led us to create two versions of Game
  Object data, GameObject for the Engine and EditorObject for the Editor. This resulted in
  duplicated code and unnecessary extra conversions and more work.
* Decision #2: We originally assumed that Players would be separate from Entities as they contained
  different important data and created two separate types of objects to reduce the amount of unused
  data, but the importance of this distinction grew less clear as the project continued.
* Decision #3: Assumed that each level was discrete and that users would play one level at a time
  rather than progressing through a series of continuous levels. This simplified the process of
  level progression by removing the need to store the next level within each file.
* Decision #4: Assumed that for multiplayer networked games there would be no restrictions on which
  keys each user can press. For example, if one user presses a key that controls another user's
  player, this would move the other player's characters. This assumption was made to facilitate
  games that needed player cooperation rather than player competition. It had a significant impact
  on the design because it meant we didn't have to implement checks on the key pressed in a server
  message before triggering the corresponding action.

## Changes from the Original Plan

* Change #1: We changed our original event system and game object abstraction hierarchy. We
  originally planned to create a complex abstraction hierarchy for the game object, but soon
  realized game objects differed slightly from one another, and primary differed in terms of
  behavior, which could be encapsulated within EventData. We originally planned to have an event
  registry system where game objects could access events through a lookup table of Events and Event
  Chains (groups of events), but this was overly complicated for our design, so we simplified the
  model to store Events directly within the game object to allow GameObjects to have separate event
  instances. Separate event instances would allow game objects to update their respective events
  without affecting other game objects.
* Change #2: We decided to separate the camera from the generic game objects because it did not
  require the properties we had originally thought it would need. Separating the camera and treating
  it as its own distinct object changed some of our API calls for loading in the camera.
* Change #3: Created a resource management system rather than instantiating in the files directly in
  the class that required them. This management system made language conversion straightforward and
  efficient and the addition of future languages simple. Furthermore, the resource management system
  meant that a developer could use any bundle they needed without ever instantiating the bundle in
  the class itself. Because this design changed later in the course, it was not fully used by all
  editor classes.
* Change #4: Implemented an immutable game object class to keep the MVC architecture consistent.
  This was neglected from the original plan and as we were coding we realized the frontend had
  access to sensitive backend game objects so we needed a way to make sure the frontend only had
  access to immutable game objects. We created an immutable game object which only had access to
  certain get methods that the frontend needed.

## How to Add New Features

#### Features Designed to be Easy to Add

* Feature #1: Social center. Since we already have a server implemented, this would not be too
  difficult. Use the client socket and message handler factory to handle when you receive messages
  from the server about chats (can make a new type handler in the factory for the server messages).
  Use the client websocket to broadcast a user's message to the server. You would need a real time
  database that the server is connected to, and upon receiving a message from a client through the
  websocket connection, the server would update the database and send a message to all other clients
  in the lobby saying that a new message was posted with the message body containing the chat.
* Feature #2: It would be fairly easy to add a new type of data storage format like JSON as we
  abstracted the file parser. The only class that knows the format is the FileParserAPI, which
  can be changed according to the file format with a little refactoring. Because we knew we were
  exclusively handling XML files for this project, we instantiated the default file parser
  directly,but if we parameterize the File Converter within the setLevelData() method of the
  GameControllerAPI, it would be fairly simple to add a new type of File Parser, which implements
  the parser API
* Feature #3: Events are easy to add because they are separated into conditions and outcomes and
  inherit from the same class - adding new events does not require adding both conditions and
  outcomes because they serve as building blocks that can be paired together.
* Feature #4: It is simple to change the server that the clients can connect to by just changing the
  link in the server properties file. This way once we host the server, it is easy to connect to the
  hosted server.

#### Features Not Yet Done

* Feature #1: We did not fully implement File Saving for the Engine
* Feature #2: We did not fully implement CSS styling switching
* Feature #3: There is no splash screen between levels for a given game
* Feature #4: We did not implement audio
* Feature #5: We did not add extra tools to make editing easier
