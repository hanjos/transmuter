package transmuter.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Point;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.junit.Before;
import org.junit.Test;

import transmuter.exception.InvalidReturnTypeException;
import transmuter.exception.PairCreationException;
import transmuter.exception.WrongParameterCountException;
import transmuter.type.TypeToken;

public class PairTest {
  private Pair object2string;
  private Pair int2boolean;
  
  @Before
  public void setUp() {
    object2string = new Pair(TypeToken.OBJECT, TypeToken.STRING);
    int2boolean = new Pair(int.class, boolean.class);
  }
  
  @Test
  public void constructorWithNulls() {
    try {
      new Pair(null, Class.class);
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
    
    try {
      new Pair(TypeToken.OBJECT, null);
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
    
    try {
      new Pair((Type) null, null);
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
    
    try {
      new Pair((TypeToken<?>) null, null);
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
  }
  
  @Test
  public void fromTypeAndToType() {
    assertEquals(TypeToken.OBJECT, object2string.getFromType());
    assertEquals(TypeToken.STRING, object2string.getToType());
    
    assertEquals(TypeToken.ValueType.INTEGER.primitive, int2boolean.getFromType());
    assertEquals(TypeToken.ValueType.BOOLEAN.primitive, int2boolean.getToType());
  }
  
  @Test
  public void equals() {
    assertEquals(object2string, object2string);
    assertEquals(int2boolean, int2boolean);
    assertEquals(new Pair(Object.class, String.class), object2string);
    assertEquals(new Pair(TypeToken.ValueType.INTEGER.primitive, TypeToken.ValueType.BOOLEAN.primitive), int2boolean);
    
    assertFalse(object2string.equals(int2boolean));
    assertFalse(int2boolean.equals(object2string));
    assertFalse(object2string.equals(new Pair(String.class, String.class)));
    assertFalse(object2string.equals(null));
    assertFalse(new Pair(int.class, int.class).equals(new Point(0, 0)));
  }
  
  @Test
  public void isAssignableFrom() {
    assertTrue(object2string.isAssignableFrom(object2string));
    assertTrue(int2boolean.isAssignableFrom(int2boolean));
    assertTrue(object2string.isAssignableFrom(new Pair(Object.class, String.class)));
    assertTrue(int2boolean.isAssignableFrom(new Pair(TypeToken.ValueType.INTEGER.primitive, TypeToken.ValueType.BOOLEAN.primitive)));
    
    assertTrue(object2string.isAssignableFrom(new Pair(String.class, String.class)));
    assertFalse(object2string.isAssignableFrom(new Pair(String.class, Object.class)));
    assertFalse(object2string.isAssignableFrom(null));
  }

  @Test
  public void testFromMethod() throws PairCreationException, SecurityException, NoSuchMethodException {
    assertEquals(new Pair(int.class, String.class), Pair.fromMethod(String.class.getMethod("substring", int.class)));
    
    try {
      Pair.fromMethod(null);
      fail();
    } catch(PairCreationException e) {
      assertEquals(1, e.getCauses().size());
      assertEquals(IllegalArgumentException.class, e.getCauses().get(0).getClass());
    }
    
    Method substring_2 = String.class.getMethod("substring", int.class, int.class);
    try {
      Pair.fromMethod(substring_2);
      fail();
    } catch(PairCreationException e) {
      assertEquals(1, e.getCauses().size());
      assertEquals(WrongParameterCountException.class, e.getCauses().get(0).getClass());
      
      WrongParameterCountException ex = (WrongParameterCountException) e.getCauses().get(0);
      assertEquals(1, ex.getExpected());
      assertEquals(2, ex.getActual());
      assertEquals(substring_2, ex.getMethod());
    }
    
    Method toString = String.class.getMethod("toString");
    try {
      Pair.fromMethod(toString);
      fail();
    } catch(PairCreationException e) {
      assertEquals(1, e.getCauses().size());
      assertEquals(WrongParameterCountException.class, e.getCauses().get(0).getClass());
      
      WrongParameterCountException ex = (WrongParameterCountException) e.getCauses().get(0);
      assertEquals(1, ex.getExpected());
      assertEquals(0, ex.getActual());
      assertEquals(toString, ex.getMethod());
    }
    
    Method wait_timeout = Object.class.getMethod("wait", long.class);
    try {
      Pair.fromMethod(wait_timeout);
      fail();
    } catch(PairCreationException e) {
      assertEquals(1, e.getCauses().size());
      assertEquals(InvalidReturnTypeException.class, e.getCauses().get(0).getClass());
      
      InvalidReturnTypeException ex = (InvalidReturnTypeException) e.getCauses().get(0);
      assertEquals(TypeToken.ValueType.VOID.primitive, ex.getReturnType());
      assertEquals(wait_timeout, ex.getMethod());
    }
  }
  
  // TODO primitive/wrapper problem
  @Test
  public void isAssignableFromWithPrimitives() {
    final Pair Integer2Boolean = new Pair(Integer.class, Boolean.class);
    assertTrue(int2boolean.isAssignableFrom(Integer2Boolean));
    assertTrue(Integer2Boolean.isAssignableFrom(int2boolean));
    
    final Pair int2Boolean = new Pair(int.class, Boolean.class);
    assertTrue(int2boolean.isAssignableFrom(int2Boolean));
    assertTrue(int2Boolean.isAssignableFrom(int2boolean));
    
    final Pair Integer2boolean = new Pair(Integer.class, boolean.class);
    assertTrue(int2boolean.isAssignableFrom(Integer2boolean));
    assertTrue(Integer2boolean.isAssignableFrom(int2boolean));
  }
}

