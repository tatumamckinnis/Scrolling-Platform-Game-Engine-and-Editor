package oogasalad.engine.model.object;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HitBoxTest {
  HitBox hitBox;

  @BeforeEach
  void setUp() {
    hitBox = new HitBox(10, 20, 30, 40);

  }

  @Test
  void getX_BasicTest_ReturnTrue() {
    assertEquals(10, hitBox.getX());
  }

  @Test
  void getY_BasicTest_ReturnTrue() {
    assertEquals(20, hitBox.getY());
  }

  @Test
  void getWidth_BasicTest_ReturnTrue() {
    assertEquals(30, hitBox.getWidth());
  }

  @Test
  void getHeight_BasicTest_ReturnTrue() {
    assertEquals(40, hitBox.getHeight());
  }

  @Test
  void setX_BasicTest_UpdatesXPosition() {
    HitBox hb = new HitBox(0, 0, 10, 10);
    hb.setX(100);
    assertEquals(100, hb.getX());
  }

  @Test
  void setYUpdatesYPosition() {
    HitBox hb = new HitBox(0, 0, 10, 10);
    hb.setY(200);
    assertEquals(200, hb.getY());
  }

  @Test
  void getWidthAndHeight_NegativeWidthAndHeight_ThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> {
      HitBox hb = new HitBox(0, 0, -10, -20);
    });
  }

}