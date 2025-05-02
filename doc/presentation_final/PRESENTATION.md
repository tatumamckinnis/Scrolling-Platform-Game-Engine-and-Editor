# It's Thyme to Eat: SALAD Final Demo
## Alana Zinkin, Aksel Bell, Billy McCune, Gage Garcia, Jacob You, Luke Nam, Tatum McKinnis  

---

## DINOSAUR JUMP

[VIDEO LINK]()
---

## DOODLE JUMP

[VIDEO LINK]()
---

## CROSSY ROAD

[VIDEO LINK]()

---

## DOODLE JUMP WIN

[FILE LINK]()

---

## DOODLE JUMP LOSE

[FILE LINK]()

---

## LET'S RUN THE PROGRAM!

---

## DATA FILES

* Internal Resources
    * [Language bundles](![language_bundles_image.png](language_bundles_image.png))
    * [Pre-fabs]()

* Sample Data File
    * [Dinosaur Jump]()

* How to: Power-ups

---

# LET'S DISCUSS DESIGN

---

## Flexibility + Open To Extension

* Original goal: allow user to create any type of game object, rules, and visuals - ACHIEVED
* However, making games is significantly more challenging than we had previously expected
* We underestimated the design challenges associated with the editor.
    * Lacked the appropriate front-end abstractions for making games easy to build
* You can add any type of object, event, and goal, but it does take time to build a game
    * Events have an Or & And system
    * Objects have string + double properties sections
    * Sprites can be imported
    * Can add new file types
    * Can add new languages

---

## Closed for Modification

* Files are encapsulated within the Level Data - we don't care how files are being parsed
* Events do not need to be changed as new ones are added
* Sprite Data, HitBox is abstracted
* New Camera Types can be created via reflection and the factory design pattern
* Buttons actions are defined within the factory
* New overlay screens with different actions (win/lose/pause/restart) can be extended from the
  GameOverlayScreen and the use of a different properties file

---

# APIs

---

# API #1: InputProvider

---

# USE CASE #1: Player presses a key

---

# API #2: UserDataAPI

---

# USE CASE #2: Saving Player Statistics

---

# DESIGN 1 (STABLE): "Everything's A Node"

---

# DESIGN 2 (CHANGED): Events System

* Discussed the changes by meeting in person consistently and Jacob and Gage each presented their
  own design proposals
* Team discussed options and weighed pros/cons

* Trade-offs:
    * Old Event System
        * Events and Event Chains connected through a registry and an ID lookup
        * Physics, Input, Collision handlers handle each type of event
    * New Event System:
        * Events consist of conditions and outcomes, which are paired together and directly tied to
          each game object

---

# SPRINT PLAN AND PRIORITIES

* EXPECTATIONS:
    * Intended to create the basic game structure and integrate the features during week 1 to get a
      basic version of the game functioning
    * The following weeks were dedicated to building features that expanded the game engine
      functionality
    * Originally believed the editor would be fully implemented in 3-weeks worth of sprints
* REALITY:
    * First 2 sprints were way slower than anticipated - we did extensive data reformatting and
      refactoring this week
    * The Editor took MUCH MORE TIME than anticipated - and we should have had more people working
      on it originally

---

# WHAT WE EACH LEARNED ABOUT AGILE/SCRUM

---

# SIGNIFICANT EVENTS TIMELINE

1. Palo Dropped :(
2. We did a second refactoring of the data format during sprint 2. We all met up over the course of
   the week to figure out the actual implementation details of the Game Objects
3. SIG EVENT 4: File saving achieved - needed much communication between different team members,
   Billy called Aksel, jacob talked with Aksel, and we were able to achieve file saving
4. Week 4: The Editor needed more work than we anticipated, and we did all-hands on deck for the
   editor - met as a group multiple times to try and configure it

---

# WHAT WE LEARNED MANAGING A LARGE PROJECT

---

# IMPROVEMENT

* SOMETHING WE IMPROVED DURING PROJECT: We all improved adhering to Agile framework
* AREA FOR IMPROVEMENT: understanding and prioritizing team goals rather than individual goals

---

# POSITIVE TEAM CULTURE

---

# TEAM CONTRACT DOC

* USEFUL PARTS
    * People took on no more issues that they could complete, generally everyone was able to do the
      issues they took
* NEEDS TO BE ADDED/CHANGED
    * Require more standups
    * Add a note that if a teammate is unable to attend a meeting, they must send an update in the
      group chat expressing why they could not attend and sharing what they worked on, what they are
      planning to do, and anything blocking them - the same way a stand-up would go
      [LINK TO DOC]()

---

# COMMUNICATION AND SOLVING PROBLEMS COLLECTIVELY

---

# THANK YOU!

Our team learned so much from this project about design, team work, collaboration, and leveraging
Agile Framework

---