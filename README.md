# oogasalad

## It's Thyme to Eat: SALAD

## Billy McCune, Alana Zinkin

This project implements an authoring environment and player for multiple related games.

### Timeline

* Start Date: 3/25/25
* Finish Date: 4/27/25
* Hours Spent: 25 hours/week across ~5 weeks

### Attributions

* Resources used for learning (including AI assistance)
  * [SOLID Design Principles](https://www.digitalocean.com/community/conceptual-articles/s-o-l-i-d-the-first-five-principles-of-object-oriented-design#single-responsibility-principle)
    used to understand design principles
  * [Refactoring Guru](https://refactoring.guru/design-patterns/factory-method) used for
    understanding design patterns - specifically factory
  * [Undoing Git commits](https://stackoverflow.com/questions/22682870/how-can-i-undo-pushed-commits-using-git)
* Resources used directly (including AI assistance)
  * ChatGPT and Claude AI assisted in writing code
  * Reused old CS316 (Intro to Database Systems) CSS code for inspiration for the OOGASalad CSS styling

### Running the Program

* Main class: Main.java
* Data files needed:

  * Level Data: selected level/game file within the doc folder which is separated by game type
  * Resource Properties files (config, css, gameIcons, i18n, server, and shared folders)
* Interesting data files:

  * [Server.properties](src/main/resources/oogasalad/server/Server.properties)
    * Links the OOGASalad application to a WebSocket server address stored in this `properties` file for multiplayer support
* Key/Mouse inputs:

  * Key and Mouse inputs are entirely decided by the user within the level file, allowing for
    total flexibility

### Configuring OpenAI API Key

The chat assistant feature requires an OpenAI API key. You can configure it in one of the following
ways (in order of precedence):

1. **Environment Variable**: Set the `OPENAI_API_KEY` environment variable

   ```bash
   export OPENAI_API_KEY=your_key_here
   ```
2. **Java System Property**: Pass the API key as a system property

   ```bash
   java -DOPENAI_API_KEY=your_key_here -jar oogasalad.jar
   ```
3. **Config File**: Add your API key to `config.properties` in the resources directory

   ```properties
   OPENAI_API_KEY=your_key_here
   ```
4. **Dot Env File**: Create a `.env` file in the project root

   ```
   OPENAI_API_KEY=your_key_here
   ```

For security reasons, options #1 and #2 are recommended for production use.

### Notes/Assumptions

* Assumptions or Simplifications:

  * Assumed that each level was discrete and that users would play one level at a time rather than
    progressing through a series of continuous levels. This simplified the process of level
    progression by removing the need to store the next level within each file.
* Known Bugs:

  * Old game level images are only removed when the "start engine button" is selected rather than
    just calling the selectGame method of the game manager - the UUID map and level scene must be
    cleared
  * When a file in the editor is saved with the incorrect "game name" matching the sprite data
    selected, the file will save but will not render when it is run in the engine
  * Events are not being properly saved within the Editor
* Features implemented:

  * Users can play a variety of scrolling platform games with a variety of different behaviors
  * The editor is able to save a basic game file and render it
* Features unimplemented:

  * Did not fully implement File Saving for the Engine
  * Did not fully implement CSS styling switching
  * Did not add a splash screen between levels for a given game
  * We did not implement audio for the game
* Noteworthy Features:

  * Networked players - players may play in multi-player mode
  * Animations
  * AI ChatBot to assist with building the editor
  * Players have profiles that they can make and update where their high scores are saved for each
    game
  * Explicit software for making new sprite sheets - allows them to be parsed and generated within
    the editor to be used
  * Prefabs are interesting because they introduce a powerful concept of reusability into game development. Instead of building every game object from scratch, designers can create 'templates' with pre-set properties and behaviors. This not only speeds up level creation but also allows for dynamic, consistent changes – tweaking a prefab updates all its instances.

### Assignment Impressions

* This assignment challenged our team to find a balance between creating an entirely customizable,
  flexible game and using common design patterns and abstraction hierarchies
* It was challenging to predict that the Engine and Editor would share extensive properties rather
  than simply sharing data through the level files
* The project was the largest team most of us had ever worked on. A large portion of the
  project was dedicated to learning Agile framework, teamwork, communication skills, and
  collaboration.
* This assignment was more complicated than we had previously assumed during our sprint planning,
  but each teammate was able to learn deeply about game design, data formatting, API usage,
  abstraction, externalizing data, encapsulation, and inheritance
