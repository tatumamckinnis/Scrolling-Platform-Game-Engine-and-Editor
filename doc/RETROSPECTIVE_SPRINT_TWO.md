# OOGASalad Stand Up and Retrospective Discussion

### It's Thyme To Eat: Salad

### Alana, Gage, Billy, Tatum, Jacob, Aksel, Luke

## Stand Up Meeting

### Alana Zinkin

* Work done this Sprint

    * Sprint Agile Planning
        * Git Issue Boards
        * Planning the Demo
    * Camera creation
    * Refactoring the game object
    * Creating an immutable game object for the view
    * Abstracting HitBox and Sprite data
    * Creating lose game condition
    * Writing tests
    * Cleaning up the pipeline
* Plan for next Sprint?

    * Display continuous floor
    * Select a game based on name and level and have continuous levels
    * Convert the Camera data to a Camera Back-end Object
    * Add tests for the event converter

* Blockers/Issues in your way

    * Need the front-end to match the back-end in terms of a player winning/losing a game
    * Need the front-end to be able to select a level with a simple user interface

### Billy McCune

* Work done this Sprint
* Plan for next Sprint?
* Blockers/Issues in your way

### Gage Garcia

* Work done this Sprint
  * Backend level selection work to support selecting + restarting levels
  * Destroy game object outcome
  * platform collision event
  * Large refactor through entire engine backend to inject new interfaces as dependencies
  * lose game outcome
* Plan for next Sprint?
  * properties file of events and their parameters
  * dynamic variable events
  * level selection events
  * lots of testing
* Blockers/Issues in your way
  * unsure of how to work with dynamic vars in backend

### Tatum McKinnis

* Work done this Sprint
    * Abstracting the object interaction tool in editor view
    * Refactor conditions section builder, events section builder, and outcomes section builder, as
      well as the properties and input tab component factories in editor view
    * Implemented various new classes to ensure editor view is properly functioning, such as editor
      view listener, and editor resource loader
    * Wrote tests for all editor view classes with 70% code coverage
    * Updated the editor view to make it more visually appealing
    * Updated the events, conditions, and outcomes selector to make it more visually appealing for
      editor view
* Plan for next Sprint?
    * Display outcome and condition parameters for editor view
    * Refactoring:
        * Resolve blocker pipeline issues in editor view (conditions section builder, editor game
          view, dyanmic variable dialog, input tab component factory, outcomes section builder,
          properties tab component factory)
        * Resolve critical issues in editor view (properties tab component factory)
    * Implement pre-fabricated objects
* Blockers/Issues in your way
    * Need the outcome and condition parameters to be implemented on the backend

### Aksel Bell

* Work done this Sprint:
  * Refactor the view by adding a View variable bridge but then replacing that with a viewState that is passed around
  * Refactor the buttons by adding a factory to make it easy to add new buttons

* Plan for next Sprint?
  * Work on backend by helping with the file saver
  * specifically, I am in charge of converting the level data into an XML file that the engine can play
* Blockers/Issues in your way
  * Working on the frontend had a steep learning curve for me and I didn't really enjoy it much.
  * So I decided to work on the backend instead so I can be a bigger contributor.

### Luke Nam

* Work done this Sprint
* Plan for next Sprint?
* Blockers/Issues in your way

### Jacob You

* Work done this Sprint
  * Adding properties input tabs
  * Adding zooming functionality to the camera
  * Adding panning functionality to the camera
  * Creating a selection tool and allowing deselection
  * Refactoring Collision, Input, and Physics classes to stem from a superclass
  * Implementing grid lock toggle
  * Implementing dynamic hitboxes and hitbox visual toggle
  * Wrote tests for most of the view backend
* Plan for next Sprint?
  * Implement save functionality from editor to the fileParser format
  * Implement a sprites properties tab
  * Implement software to automatically divide a sprite sheet into cells
  * Implement software to save FrameData, AnimationData and base Sprites
  * Refactor code to minimize pipeline issues
  * Fix camera zooming incorrectly when panned.
* Blockers/Issues in your way
  * Need the fileParser save functionality to be implemented for full saving

## Project's current progress

* We are slightly behind schedule since most members were very busy this past week
* We have a majority of the back-end completed
* Front-end is behind schedule and does not match the back-end progress
* Editor is nearly complete
* Must finish implementing editor saving

## Current level of communication

* Team meets frequently in person
* Team does not send many updates about what they're working on daily
* Need to add more Agile stand-up meetings

## Satisfaction with team roles

* Our team is working really well together and we all enjoy what were working on
* Going forward next week, we plan to transition roles so everyone gets a chance to touch different
  parts of the game

## Teamwork that worked well

* Thing #1:
    * We needed to do a TON of refactoring this sprint. I would say a large majority of the sprint
      focused on redesigning and implementing core features - which worked well because we all met
      up together consistently to agree upon what would be the best way.

* Thing #2
    * We met consistently and started doing more agile meetings over zoom so we could screen share (
      but in person)

## Teamwork that could be improved

* Thing #1:

    * We need to make our issues board more clear and assign all issues to individuals
    * Sprint planning will make our communication and roles easier

* Thing #2:

    * We want to make steady progress each day, rather than making tons of commits on one day
    * It was tricky with the basketball schedule and members traveling, but this sprint we will make
      progress each day!

## Teamwork to improve next Sprint

* The issues board needs to be much clearer
* We need to communicate what were working on within our group chat
