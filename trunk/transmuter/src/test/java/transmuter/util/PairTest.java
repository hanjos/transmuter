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
import transmuter.exception.PairInstantiationException;
import transmuter.exception.WrongParameterCountException;
import transmuter.type.TypeToken;
import transmuter.util.exception.MethodOwnerTypeIncompatibilityException;

public class PairTest {
  private Pair object2string;
  private Pair int2boolean;
  
  @Before
  public void setUp() {
    object2string = new Pair(TypeToken.OBJECT, TypeToken.STRING);
    int2boolean = new Pair(int.class, boolean.class);
  }
  
  @Test
  public void testFromMethod() throws SecurityException, NoSuchMethodException {
    assertEquals(new Pair(int.class, String.class), Pair.fromMethod(String.class.getMethod("substring", int.class)));
    
    try {
      Pair.fromMethod(null, Object.class);
      fail();
    } catch(PairInstantiationException e) {
      assertEquals(1, e.getCauses().size());
      assertEquals(IllegalArgumentException.class, e.getCauses().get(0).getClass());
    }
    
    try {
      Pair.fromMethod(String.class.getMethod("substring", int.class), null);
      fail();
    } catch(PairInstantiationException e) {
      assertEquals(1, e.getCauses().size());
      assertEquals(IllegalArgumentException.class, e.getCauses().get(0).getClass());
    }
    
    Method substring_2 = String.class.getMethod("substring", int.class, int.class);
    try {
      Pair.fromMethod(substring_2);
      fail();
    } catch(PairInstantiationException e) {
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
    } catch(PairInstantiationException e) {
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
    } catch(PairInstantiationException e) {
      assertEquals(1, e.getCauses().size());
      assertEquals(InvalidReturnTypeException.class, e.getCauses().get(0).getClass());
      
      InvalidReturnTypeException ex = (InvalidReturnTypeException) e.getCauses().get(0);
      assertEquals(void.class, ex.getType());
      assertEquals(wait_timeout, ex.getMethod());
    }
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
  public void constructorWithVoid() {
    try {
      new Pair(void.class, Integer.class);
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
    
    try {
      new Pair(Object.class, void.class);
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
    
    try {
      new Pair(Void.class, Integer.class);
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
    
    try {
      new Pair(Object.class, Void.class);
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
  }
  
  @Test
  public void testFromMethodWithIncompatibleOwnerType() throws SecurityException, NoSuchMethodException {
    final Method valueOfBoolean = String.class.getMethod("valueOf", boolean.class);
    try {
      Pair.fromMethod(valueOfBoolean, Object.class);
    } catch(PairInstantiationException e) {
      assertEquals(1, e.getCauses().size());
      
      MethodOwnerTypeIncompatibilityException ex0 = (MethodOwnerTypeIncompatibilityException) e.getCauses().get(0);
      assertEquals(valueOfBoolean, ex0.getMethod());
      assertEquals(Object.class, ex0.getOwnerType());
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
    assertEquals(new Pair(String.class, boolean.class), new Pair(String.class, Boolean.class));
    assertEquals(new Pair(byte.class, Object.class), new Pair(Byte.class, Object.class));
    assertEquals(new Pair(int.class, Character.class), new Pair(Integer.class, char.class));
    
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
  public void isAssignableFromWithPrimitives() {
    final Pair Integer2Boolean = new Pair(Integer.class, Boolean.class);
    assertTrue(int2boolean.isAssignableFrom(Integer2Boolean));
    assertTrue(Integer2Boolean.isAssignableFrom(int2boolean));
    assertTrue(Integer2Boolean.equals(int2boolean));
    assertTrue(int2boolean.equals(Integer2Boolean));
    
    final Pair int2Boolean = new Pair(int.class, Boolean.class);
    assertTrue(int2boolean.isAssignableFrom(int2Boolean));
    assertTrue(int2Boolean.isAssignableFrom(int2boolean));
    assertTrue(int2Boolean.equals(int2boolean));
    assertTrue(int2boolean.equals(int2Boolean));
    
    final Pair Integer2boolean = new Pair(Integer.class, boolean.class);
    assertTrue(int2boolean.isAssignableFrom(Integer2boolean));
    assertTrue(Integer2boolean.isAssignableFrom(int2boolean));
    assertTrue(Integer2boolean.equals(int2boolean));
    assertTrue(int2boolean.equals(Integer2boolean));
    
    final Pair Double2String = new Pair(Double.class, String.class);
    final Pair d2s = new Pair(double.class, String.class);
    assertTrue(Double2String.isAssignableFrom(d2s));
    assertTrue(d2s.isAssignableFrom(Double2String));
    assertTrue(d2s.equals(Double2String));
    assertTrue(Double2String.equals(d2s));
  }
}

