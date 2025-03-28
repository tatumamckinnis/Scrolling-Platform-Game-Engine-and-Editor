## Example Game Descriptions
## OOGASALAD TEAM 3

### DESIGN GOALS
1. Our primary design goals include making our architecture so flexible, that a user can play any time of scrolling-platform
game they like, including Doodle Jump, Super Mario Bros, or Geometry Dash.
2. Users should be able to mix game elements together and easily construct new games within our editor
3. Our design should be flexible enough that a new developer would be able to create an entirely new scrolling platform game and define new types of 
players, characters, physics, interaction rules, goals, and visual game elements (open for extension)
   1. We will achieve this through the use of factory design patterns, strategy design patterns, and reflection-based initialization
4. The code should be clean and readable such that a new developer could be easily onboarded to the project
5. The underlying representation of all game objects should be encapsulated and separate from the view
6. The front-end should encapsulate the type of JavaFX object used (everything is a "node")
7. Only the file parser should know the format of the configuration files
8. The front-end of both the editor and the engine should not know how objects are represented in the back-end

### PRIMARY ARCHITECTURE
We used the MVC architecture to encapsulate our backend and frontend. The game manager is the highest level class that contains the game timeline and controls the interactions between the model and view. We use a game controller API which calls all the phase controller (an abstract class) which has subclasses which execute a certain phase during the update (ie movement, collision, or physics). This is open to extension as developers can add a new type of phase controller which will call events when certain things happen. The events will be called in a chain called the event chain. This architecture allows developers to customize how different inputs are handled and define the way objects interact with each other.

Game objects are also abstracted so changing their animations, object type (ie player or block), behaviors, etc are changed by adding configuration files that define the features. Flags objects are used to flag a point when the game settings can change (ie changing physics rules). 

Our frontend works by rendering the whole map initially and then only displaying a certain part of the screen decided by the camera API. The camera API implements a strategy which tells it what behavior to do (ie continuously moving one direction or following the player). When objects are updated in the backend, the frontend is instructed to rerender the objects by their IDs.


### MODEL DIAGRAM
[Model Diagram](ModelDiagram.pdf)
