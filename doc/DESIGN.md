# DESIGN Document for PROJECT_NAME

### It's Thyme to Eat: SALAD

### Alana Zinkin, Billy McCune, Tatum McKinnis, Jacob You, Gage Garcia, Aksel Bell, Luke Nam

## Team Roles and Responsibilities

* Team Member #1: Alana Zinkin
    * Product management: assigning of git issues, sprint planning, documentation, group
      organization
    * Engine Controller: integrating the back-end and front-end endpoints (GameManager and
      GameController APIs)
    * Language Selecting and Resource Management Refactoring
    * Level Selecting mechanism
    * Camera creation and object
    * LevelData conversion to Engine and Editor Objects (loading)
    * Rendering game objects in the front-end
    * Adding Engine and Editor View features such as buttons and actions, help views, editor tools,
      image rendering

* Team Member #2: Billy McCune

* Team Member #3: Jacob You

* Team Member #4: Tatum McKinnis

* Team Member #5: Gage Garcia

* Team Member #6: Aksel Bell
  * Full stack engineer: worked on frontend of the engine, worked on backend file saving, and worked on challenge feature networked players which was backend but separate from the model.
  * Set up initial infrastructure for the frontend with the button action factory, view components, the setup of view screens, the abstract display class, the viewstate to keep MVC and classes tight
  * Worked on file saving. Added classes to convert leveldata to XML and also allow easy extension support for other types of files
  * Worked on multiplayer networked support. Created a replit javscript server (with a link in external properties file), set up network protocol of what information is passed through the server, set up client sockets to connect to the external javascript server, set up server message data structure, set up handling of the server messages with the server message factory.

* Team Member #7: Luke Nam

## Design Goals

* Goal #1

* Goal #2

* Goal #3

#### How were Specific Features Made Easy to Add

* Feature #1

* Feature #2

* Feature #3

## High-level Design

#### Core Classes and Abstractions, their Responsibilities and Collaborators

* Class #1

* Class #2

* Class #3

* Class #4: Button action factory. This factory was core to any actions that were triggered from user mouse clicks. It abstracted how the button action was implemented, and allowed easier testing of the actions. Also, it used reflection so adding new buttons was simple (and didn't require modifications but just additions) if you had the button id from the resource files. Aksel, Alana, Billy, Luke all worked on this class. It demostrates understanding of the factory design patter as well.

## Assumptions or Simplifications

* Decision #1

* Decision #2

* Decision #3

* Decision #4: Assumed that for multiplayer networked games there would be no restrictions on which keys each user can press. For example, if one user presses a key that controls another user's player, this would move the other player's characters. This assumption was made to facilitate games that needed player cooperation rather than player competition. It had a significant impact on the design because it meant we didn't have to implement checks on the key pressed in a server message before triggering the corresponding action.

## Changes from the Original Plan

* Change #1: 

* Change #2

* Change #3

* Change #4: Implemented an immutable game object class to keep the MVC architecture consistent. This was neglected from the original plan and as we were coding we realized the frontend had access to sensitive backend game objects so we needed a way to make sure the frontend only had access to immutable game objects. We created an immutable game object which only had access to certain get methods that the frontend needed. 

## How to Add New Features

#### Features Designed to be Easy to Add

* Feature #1: Social center. Since we already have a server implemented, this would not be too difficult. Use the client socket and message handler factory to handle when you receive messages from the server about chats (can make a new type handler in the factory for the server messages). Use the client websocket to broadcast a user's message to the server. You would need a real time database that the server is connected to, and upon receiving a message from a client through the websocket connection, the server would update the database and send a message to all other clients in the lobby saying that a new message was posted with the message body containing the chat.

* Feature #2

* Feature #3

* Feature #4

#### Features Not Yet Done

* Feature #1

* Feature #2

* Feature #3

* Feature #4
 