## OOGASalad API Changes

### It's Thyme to Eat: Salad

### Alana, Billy, Gage, Aksel, Tatum, Jacob, Luke

### Changes Made

#### API #1: EngineFileAPI --> EngineFileConverterAPI

* Method changed: added a loadCamera() method

    * Why was the change made?
        * EngineFileAPI was renamed to EngineFileConverterAPI to better reflect the purpose of the
          API
        * Originally we had assumed that the camera would be a game object and would be loaded in
          with the default loadEngineToFile method
        * However, the camera differed significantly from the other game objects, so we created an
          entirely new camera object

    * Major or Minor (how much they affected your team mate's code)
        * The change was minor, it did not affect my team mate's code dramatically
        * The main change was the renderGameObjects of the DefaultView class required an additional
          parameter (the Camera object), but this was an easy change

    * Better or Worse (and why)
        * This design is better because adding the loadCamera method respects the Single
          Responsibility Principle and makes
          testing easier
        * Returning the Camera object separately from the loadEngineFile method makes it easier to
          test that the camera object is being instantiated correctly and isolates this
          functionality from the rest of the loading of the game objects

* Method changed: loadFileToEngine()

    * Why was the change made?
        * The return value of the loadFileToEngine method is no longer void, but rather it returns a
          Map<String, GameObject>
        * Furthermore, the method now takes in a LevelData parameter
        * The return value was changed for two reasons: to make testing easier to ensure that the
          object map that would be created within the method could be used by other methods easily
        * The return value was also changed for design purposes: a developer should not implicitly
          set this object map, but should rather set the object map themself. This makes debugging
          easier as well and is more intuitive and reduces dependencies between the
          EngineFileConverterAPI and GameControllerAPI
        * The parameter values were changed because the method should function as a "converter"
          which takes in some LevelData input and turns it into a usable data structure rather than
          the "selectLevel" method of the GameManagerAPI class

    * Major or Minor (how much they affected your team mate's code)
        * The change was pretty minor in that we made the change at the very beginning of the coding
          cycle, which meant that everyone adopted the new design immediately

    * Better or Worse (and why)
        * This design is better because not only does it reduce dependencies in our code base (
          methods are no longer implicitly calling on other APIs within the method) but it also
          makes testing easier for developers since the method actually returns a map

#### API #2: File saver

* Method changed: added new public API to save file

    * Why was the change made? Needed a new API to allow saving 

    * Major or Minor (how much they affected your team mate's code)
      * major. Allowed saving of files into 

    * Better or Worse (and why)
      * Better, the strategies allowed extension of exporting other files.


* Method changed:

    * Why was the change made?

    * Major or Minor (how much they affected your team mate's code)

    * Better or Worse (and why)

#### API #3

* Method changed:

    * Why was the change made?

    * Major or Minor (how much they affected your team mate's code)

    * Better or Worse (and why)


* Method changed:

    * Why was the change made?

    * Major or Minor (how much they affected your team mate's code)

    * Better or Worse (and why)

#### API #4

* Method changed:

    * Why was the change made?

    * Major or Minor (how much they affected your team mate's code)

    * Better or Worse (and why)


* Method changed:

    * Why was the change made?

    * Major or Minor (how much they affected your team mate's code)

    * Better or Worse (and why)

