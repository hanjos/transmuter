package transmuter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Point;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.junit.Before;
import org.junit.Test;

import transmuter.converter.Binding;
import transmuter.converter.ConverterType;
import transmuter.converter.exception.ConverterTypeInstantiationException;
import transmuter.converter.exception.InvalidReturnTypeException;
import transmuter.converter.exception.MethodOwnerTypeIncompatibilityException;
import transmuter.converter.exception.WrongParameterCountException;
import transmuter.type.TypeToken;

public class ConverterTypeTest {
  private ConverterType object2string;
  private ConverterType int2boolean;
  
  @Before
  public void setUp() {
    object2string = new ConverterType(TypeToken.OBJECT, TypeToken.STRING);
    int2boolean = new ConverterType(int.class, boolean.class);
  }
  
  @Test
  public void testFromMethod() throws SecurityException, NoSuchMethodException {
    assertEquals(new ConverterType(int.class, String.class), ConverterType.fromMethod(String.class.getMethod("substring", int.class)));
    
    try {
      ConverterType.fromMethod(null);
    } catch(ConverterTypeInstantiationException e) {
      assertEquals(1, e.getCauses().size());
      assertEquals(IllegalArgumentException.class, e.getCauses().get(0).getClass());
    }
    
    try {
      ConverterType.fromMethod(null, Object.class);
      fail();
    } catch(ConverterTypeInstantiationException e) {
      assertEquals(1, e.getCauses().size());
      assertEquals(IllegalArgumentException.class, e.getCauses().get(0).getClass());
    }
    
    try {
      ConverterType.fromMethod(String.class.getMethod("substring", int.class), null);
      fail();
    } catch(ConverterTypeInstantiationException e) {
      assertEquals(1, e.getCauses().size());
      assertEquals(IllegalArgumentException.class, e.getCauses().get(0).getClass());
    }
    
    Method substring_2 = String.class.getMethod("substring", int.class, int.class);
    try {
      ConverterType.fromMethod(substring_2);
      fail();
    } catch(ConverterTypeInstantiationException e) {
      assertEquals(1, e.getCauses().size());
      assertEquals(WrongParameterCountException.class, e.getCauses().get(0).getClass());
      
      WrongParameterCountException ex = (WrongParameterCountException) e.getCauses().get(0);
      assertEquals(1, ex.getExpected());
      assertEquals(2, ex.getActual());
      assertEquals(substring_2, ex.getMethod());
    }
    
    Method toString = String.class.getMethod("toString");
    try {
      ConverterType.fromMethod(toString);
      fail();
    } catch(ConverterTypeInstantiationException e) {
      assertEquals(1, e.getCauses().size());
      assertEquals(WrongParameterCountException.class, e.getCauses().get(0).getClass());
      
      WrongParameterCountException ex = (WrongParameterCountException) e.getCauses().get(0);
      assertEquals(1, ex.getExpected());
      assertEquals(0, ex.getActual());
      assertEquals(toString, ex.getMethod());
    }
    
    Method wait_timeout = Object.class.getMethod("wait", long.class);
    try {
      ConverterType.fromMethod(wait_timeout);
      fail();
    } catch(ConverterTypeInstantiationException e) {
      assertEquals(1, e.getCauses().size());
      assertEquals(InvalidReturnTypeException.class, e.getCauses().get(0).getClass());
      
      InvalidReturnTypeException ex = (InvalidReturnTypeException) e.getCauses().get(0);
      assertEquals(void.class, ex.getType());
      assertEquals(wait_timeout, ex.getMethod());
    }
  }
  
  @Test
  public void testFromBinding() throws ConverterTypeInstantiationException, SecurityException, NoSuchMethodException {
    try {
      ConverterType.fromBinding(null);
      fail();
    } catch(ConverterTypeInstantiationException e) {
      assertEquals(1, e.getCauses().size());
      assertEquals(IllegalArgumentException.class, e.getCauses().get(0).getClass());
    }
    
    Method substring = String.class.getMethod("substring", int.class);
    
    assertEquals(ConverterType.fromMethod(substring), ConverterType.fromBinding(new Binding("0123", substring)));
  }
  
  @Test
  public void constructorWithNulls() {
    try {
      new ConverterType(null, Class.class);
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
    
    try {
      new ConverterType(TypeToken.OBJECT, null);
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
    
    try {
      new ConverterType((Type) null, null);
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
    
    try {
      new ConverterType((TypeToken<?>) null, null);
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
  }
  
  @Test
  public void constructorWithVoid() {
    try {
      new ConverterType(void.class, Integer.class);
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
    
    try {
      new ConverterType(Object.class, void.class);
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
    
    try {
      new ConverterType(Void.class, Integer.class);
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
    
    try {
      new ConverterType(Object.class, Void.class);
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
  }
  
  @Test
  public void testFromMethodWithIncompatibleOwnerType() throws SecurityException, NoSuchMethodException {
    final Method valueOfBoolean = String.class.getMethod("valueOf", boolean.class);
    try {
      ConverterType.fromMethod(valueOfBoolean, Object.class);
    } catch(ConverterTypeInstantiationException e) {
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
    assertEquals(new ConverterType(Object.class, String.class), object2string);
    assertEquals(new ConverterType(TypeToken.ValueType.INTEGER.primitive, TypeToken.ValueType.BOOLEAN.primitive), int2boolean);
    assertEquals(new ConverterType(String.class, boolean.class), new ConverterType(String.class, Boolean.class));
    assertEquals(new ConverterType(byte.class, Object.class), new ConverterType(Byte.class, Object.class));
    assertEquals(new ConverterType(int.class, Character.class), new ConverterType(Integer.class, char.class));
    
    assertFalse(object2string.equals(int2boolean));
    assertFalse(int2boolean.equals(object2string));
    assertFalse(object2string.equals(new ConverterType(String.class, String.class)));
    assertFalse(object2string.equals(null));
    assertFalse(new ConverterType(int.class, int.class).equals(new Point(0, 0)));
  }
  
  @Test
  public void isAssignableFrom() {
    assertTrue(object2string.isAssignableFrom(object2string));
    assertTrue(int2boolean.isAssignableFrom(int2boolean));
    assertTrue(object2string.isAssignableFrom(new ConverterType(Object.class, String.class)));
    assertTrue(int2boolean.isAssignableFrom(new ConverterType(TypeToken.ValueType.INTEGER.primitive, TypeToken.ValueType.BOOLEAN.primitive)));
    
    assertTrue(object2string.isAssignableFrom(new ConverterType(String.class, String.class)));
    assertFalse(object2string.isAssignableFrom(new ConverterType(String.class, Object.class)));
    assertFalse(object2string.isAssignableFrom(null));
  }

  @Test
  public void isAssignableFromWithPrimitives() {
    final ConverterType Integer2Boolean = new ConverterType(Integer.class, Boolean.class);
    assertTrue(int2boolean.isAssignableFrom(Integer2Boolean));
    assertTrue(Integer2Boolean.isAssignableFrom(int2boolean));
    assertTrue(Integer2Boolean.equals(int2boolean));
    assertTrue(int2boolean.equals(Integer2Boolean));
    
    final ConverterType int2Boolean = new ConverterType(int.class, Boolean.class);
    assertTrue(int2boolean.isAssignableFrom(int2Boolean));
    assertTrue(int2Boolean.isAssignableFrom(int2boolean));
    assertTrue(int2Boolean.equals(int2boolean));
    assertTrue(int2boolean.equals(int2Boolean));
    
    final ConverterType Integer2boolean = new ConverterType(Integer.class, boolean.class);
    assertTrue(int2boolean.isAssignableFrom(Integer2boolean));
    assertTrue(Integer2boolean.isAssignableFrom(int2boolean));
    assertTrue(Integer2boolean.equals(int2boolean));
    assertTrue(int2boolean.equals(Integer2boolean));
    
    final ConverterType Double2String = new ConverterType(Double.class, String.class);
    final ConverterType d2s = new ConverterType(double.class, String.class);
    assertTrue(Double2String.isAssignableFrom(d2s));
    assertTrue(d2s.isAssignableFrom(Double2String));
    assertTrue(d2s.equals(Double2String));
    assertTrue(Double2String.equals(d2s));
  }
}

