# OOGA Model Diagram
## It's Thyme to Eat: SALAD
## Aksel Bell, Gage Garcia, Billy McCune, Tatum McKinnis, Luke Nam, Palo Silva, Jacob You, Alana Zinkin

### Game Engine Back-End
```
                    +----------------------+        
                    |        Game         |      
                    +----------------------+
                               |
                               v
            ----------------------------------------
            |                                   |
            v                                   v
+----------------------+            +----------------------+
|   Level Selector    |             |     Game Loop       |  
+----------------------+            |     - update()      |
            |                       |     - render()      |
            v                       +----------------------+        
                                       
            |
            v
+----------------------+            +----------------------+
|  LevelProcessor      |   <----    |  LevelLoader         | ---> communicates with editor 
+----------------------+            +----------------------+
|                      |            |   - loadLevelFile()  |  
|   - generateGame()   |            +----------------------+
|                      |                       ^
|                      |                       |
|                      |                       v
|                      |            +----------------------+
+----------------------+            |  LevelSaver          | ---> communicates with editor
            |                       +----------------------+
            |                       |   - saveLevelFile()  |
            |                       +----------------------+
            |
            v                       
            
+----------------------+             +----------------------+
|       Level         |              |         Goal         |
+----------------------+             +----------------------+
| - gameObjects: List |              |                      |
| - gameProperties    |    ---->     |                      |
| - update()          |              +----------------------+
| - render()          |
+----------------------+
                |
                v
    +---------------------------+               +-------------------------------+
    |    Game Object            |               |   Handlers                    |
    +---------------------------+               +-------------------------------+
    | - transform               |               | - init()                      |
    | - sprite                  | --------->    | - update()                    | 
    | - components: List        |               | - render()                    |
    |                           |               | - destroy()                   |              
    +---------------------------+               +-------------------------------+
       |         |            |                     |             |               |
       v         v            v                     v             v               v
+---------+ +-----------+ +--------+            +-----------+ +-----------+ +------------+
| Entity  | |  Player   | | Block  |            |  Physics  | | Collision | | Input      |
+---------+ +-----------+ +--------+            |  Handler  | | Handler   | | Handler    |
                                                +-----------+ +-----------+ +------------+
                                                                                    ^
                                                                                    |
                                                                                    v
                                                                            +-----------------+
                                                                            |  Sprite Handler |
                                                                            +-----------------+        
```

### Game Engine Front-End

```
                +----------------------+
                |     GameApp          |
                +----------------------+
                |  extends Application |
                +----------------------+
                            |
                            v
                +----------------------+
                |   Game Controller   |
                +----------------------+
                | - Takes API calls   |
                | - Updates game      |
                +----------------------+
                            |
                            v
                +------------------------------------------+
                |             Game View                    |
                +------------------------------------------+
                |           - Holds UI Elements            |
                +------------------------------------------+
                  |               |                       |
                  v               v                       v
               +---------+    +-----------+      +--------------+
               | Title   |    |  HUD View |      | Level View   |
               +---------+    +-----------+      +--------------+
               | Pause   |    |           |      |              |
               |         |    | - Save()  |      | - Draw()     |
               +---------+    |           |      |              |
                              +-----------+      |              | 
                                                 +--------------+
```

## Rendering System
```
    +----------------------+
    |     Renderer       |
    +----------------------+
    | - Draws individual |
    |   game objects     |
    +----------------------+


            +----------------------+
            |   Editor Backend    |
            +----------------------+
            | - Editor Processor  |
            | - Grid objects      |
            +----------------------+
                        |
                        v
            +----------------------+
            |   Input Handler API |
            +----------------------+
            | - Takes input       |
            | - Sends to view     |
            +----------------------+
```
## Shared Data Structures
```
        +----------------------+
        |  Level Processor    |
        +----------------------+
        | - Converts raw data |
        | - Keeps track of    |
        |   object states     |
        +----------------------+
                    |
                    v
        +----------------------+
        |  GameObjectData     |
        +----------------------+
        | - Map of key, value |
        | - List of shapes    |
        | - Object paths      |
        +----------------------+

```

### Game Editor Back-End

```
+----------------------+
|    Game Editor      |
+----------------------+
| - Launch           |
| - Create/show stage|
+----------------------+

+----------------------+
|   Editor Controller |
+----------------------+
| - Mediator between  |
|   UI and Backend    |
| - Event Handlers    |
| - Updates objects   |
+----------------------+
```
### Game Editor Front-End
