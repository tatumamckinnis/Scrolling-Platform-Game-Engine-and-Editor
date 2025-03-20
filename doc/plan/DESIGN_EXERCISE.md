# Fluxx Lab Discussion
## Alana abz8, Tatum tam85, Aksel adb117, Palo pbs27, Gage gbd7, Billy wrm29, Jacob jay27, Luke lsn12



### High Level Design Ideas


### CRC Card Classes

This class's purpose or value is to manage orders based on data:
```java
public class Something {
     // sums the numbers in the given data
     public int getTotal (Collection<Integer> data)
     // creates an order from the given data
     public Order makeOrder (String structuredData)
 }
```

This class's purpose or value is to mange an Order based on the given data:
```java
public class Order {
     // updates the information based on new data 
     public void update (int data)
 }
```


### Use Cases

* A player plays a Goal card, changing the current goal, and wins the game.
```java
 Something thing = new Something();
 Order o = thing.makeOrder(sformattedStringData);
 p.update(13);
```

* A player plays an Action card, allowing him to choose cards from another player's hand and play them.
```java
 Something thing = new Something();
 Order o = thing.makeOrder(sformattedStringData);
 p.update(13);
```

* A player plays a Rule card, adding to the current rules to set a hand-size limit, requiring all players to immediately drop cards from their hands if necessary.
```java
 Something thing = new Something();
 Order o = thing.makeOrder(sformattedStringData);
 p.update(13);
```

* A player plays a Rule card, changing the current rule from Play 1 to Play All, requiring the player to play more cards this turn.
```java
 Something thing = new Something();
 Order o = thing.makeOrder(sformattedStringData);
 p.update(13);
```

* A player plays a card, fulfilling the current Ungoal, and everyone loses the game.
```java
 Something thing = new Something();
 Order o = thing.makeOrder(sformattedStringData);
 p.update(13);
```

* A new theme for the game is designed, creating a different set of Rule, Keeper, and Creeper cards.
```java
 Something thing = new Something();
 Value v = thing.getValue();
 v.update(13);
```