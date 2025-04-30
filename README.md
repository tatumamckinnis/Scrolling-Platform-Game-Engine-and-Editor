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
   * [SOLID Design Principles](https://www.digitalocean.com/community/conceptual-articles/s-o-l-i-d-the-first-five-principles-of-object-oriented-design#single-responsibility-principle) used to understand design principles
   * [Refactoring Guru](https://refactoring.guru/design-patterns/factory-method) used for understanding design patterns - specifically factory
   * [Undoing Git commits](https://stackoverflow.com/questions/22682870/how-can-i-undo-pushed-commits-using-git) 
 * Resources used directly (including AI assistance)
   * ChatGPT and Claude AI assisted in writing code
   * 


### Running the Program

 * Main class: Main.java

 * Data files needed: 
   * Level Data: selected level/game file
   * Resource Properties files (config, css, gameIcons, i18n, server, and shared folders)

 * Interesting data files:

 * Key/Mouse inputs:

### Configuring OpenAI API Key

The chat assistant feature requires an OpenAI API key. You can configure it in one of the following ways (in order of precedence):

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

 * Known Bugs:

 * Features implemented:

 * Features unimplemented:

 * Noteworthy Features:



### Assignment Impressions


