# OOGASalad Stand Up and Retrospective Discussion

### It's Thyme to Eat: Salad

### Alana, Billy, Gage, Aksel, Luke, Tatum, Jacob

## Stand Up Meeting

### Team Member 1: Alana

* Work done this Sprint

    * Refactored the view to add exceptions when appropriate
    * Allow for the ability to destroy view objects from the front-end
    * Refactored the abstraction of the view object to use just interfaces of getters rather than
      creating an entirely new object
    * Allow for the instantiation of a camera based on a data file
    * Created a camera factory for the camera instantiation and added new type of camera
    * Add tests for the event converter classes
    * Organized team through git issues and led sprint planning
    * Resolved various pipeline issues through adding comments, documentation, and using abstract
      classes
    * Add delete button and functionality for editor
    * Style editor using CSS
* Plan for next Sprint?

    * Refactor: Camera Factory: Change into 1 Factory
    * Level Selecting: change level selecting method
    * Add Authorship Tags for Camera Factory
    * Engine: Remove hit boxes from the view
    * Editor: Add clear screen button
    * Add highlighting when hovering for delete
    * Display the score and lives
* Blockers/Issues in your way

    * Nothing!

### Team Member 2: Tatum

* Work done this Sprint (worked on editor view)
    * Allowed for condition/outcome parameters to be displayed
    * Resolved the large amounnt of blocker pipeline issues for editor view
    * Resolved the large amount of critical pipeline issues for editor view
    * Created the pre-fab pane
    * Refactored the editor view heavily to allow for displaying the new event system
    * Implemented new tests for the new event system with the editor view
    * Allowed for storing pre-fab data
    * Allowed for showing pre-fab data
    * Resolved code quality issues with large concrete editor class, private variables
    * Heavily refactored editor view to pull text from resource files instead
* Plan for next Sprint?
    * Fully implement the pre-fabs
    * Resolve test errors in editor view
    * Add a save button
    * Allow for sprite rotation in the view
    * Pull in event parameters from the properties file
    * Extensions:
        * Grid lines toggle
        * Hitbox toggle
* Blockers/Issues in your way
    * Saving functionality needs to be finalized for save button to be completed

### Team Member 3 - Aksel

* Work done this Sprint
  * Wrote the file saver class which takes level data and turns it into an XML file
  * Set up infrastructure to allow easy extension using strategies and components
* Plan for next Sprint?
  * Support multiplayer games over servers
* Blockers/Issues in your way
  * Learn how to set up a server, how to connect server. Don't know anything about the topic
  * Need to do initial research and learning on how multiplayer works

### Team Member 4: Jacob

* Work done this Sprint
  * Finished adding extra level customization such as more tools, and access to more properties
  * Began work on implementing sprite sheets as well as a sprite sheet editor to load sprites and make the sprite atlas file
  * Implemented the ability to easily lock and unlock grid and toggle hitboxes in code, awaiting future implementation
  * Built basic infrastructure to save from editor data to level data in the fileparser class
* Plan for next Sprint?
  * Finish the implementation of sprite sheets and allow the saving and loading of their XMLs and polish it.
  * Implement the ability to assign an object a sprite based on the currently loaded objects
  * Potentially create an editor to allow the user to make templates of sprites to assign to a specific object.
  * Polish the editor with new implementations, tools, and quality of life additions.
* Blockers/Issues in your way
  * Need events and camera properties to be listed in some file that the editor can pull to display in the front end.

### Team Member 5

* Work done this Sprint
* Plan for next Sprint?
* Blockers/Issues in your way

### Team Member 6

* Work done this Sprint
* Plan for next Sprint?
* Blockers/Issues in your way

### Team Member 7

* Work done this Sprint
* Plan for next Sprint?
* Blockers/Issues in your way

### Team Member 8

* Work done this Sprint
* Plan for next Sprint?
* Blockers/Issues in your way

## Project's current progress

* The project is far along, we have most features we want done up to this point
* Still need the editor to save a file - this is a core requirement
* Still need to add more events to create more unique games
* Team is working well together, but some teammates are doing less than others

## Current level of communication

* Communication is relatively strong - we meet very frequently and primarily in-person
* We have communicated within small groups as well, which has worked well for the team
* Some teammates have been less communicative, however
* Most teammates are able to meet for our weekly sprint planning and team meetings

## Satisfaction with team roles

* Most teammates are happy with their roles and have been able to switch roles, which is exciting
* We have experts for different parts of the project, and some team members, like Alana, who move
  around the project directory
* Some teammates who werent super excited originally have found parts of the project they enjoy more

## Teamwork that worked well

* Thing #1: We all met frequently for our project and everyone on the team was able to contribute
* Thing #2: We closed most of our git issues and followed Agile framework better

## Teamwork that could be improved

* Thing #1: Some teammates did not communicate as frequently as they normally do with the team due
  to other obligations
* Thing #2: Some teammates did more work than others - responsibilities were not balanced

## Teamwork to improve next Sprint

* Going forward, we want all members to be excited about their parts of the project and to have more
  pair programming 
