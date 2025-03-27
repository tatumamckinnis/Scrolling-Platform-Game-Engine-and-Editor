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



### MODEL DIAGRAM