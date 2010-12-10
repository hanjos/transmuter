package com.googlecode.transmuter.type;

import static com.googlecode.transmuter.type.TypeToken.ValueType.BOOLEAN;
import static com.googlecode.transmuter.type.TypeToken.ValueType.BYTE;
import static com.googlecode.transmuter.type.TypeToken.ValueType.CHARACTER;
import static com.googlecode.transmuter.type.TypeToken.ValueType.DOUBLE;
import static com.googlecode.transmuter.type.TypeToken.ValueType.FLOAT;
import static com.googlecode.transmuter.type.TypeToken.ValueType.INTEGER;
import static com.googlecode.transmuter.type.TypeToken.ValueType.LONG;
import static com.googlecode.transmuter.type.TypeToken.ValueType.SHORT;
import static com.googlecode.transmuter.type.TypeToken.ValueType.VOID;
import static com.googlecode.transmuter.type.TypeToken.ValueType.isPrimitive;
import static com.googlecode.transmuter.type.TypeToken.ValueType.isWrapper;
import static com.googlecode.transmuter.type.TypeToken.ValueType.matching;
import static com.googlecode.transmuter.type.TypeToken.ValueType.valueOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Type;

import org.junit.Test;

import com.googlecode.transmuter.type.TypeToken;
import com.googlecode.transmuter.type.TypeToken.ValueType;
import com.googlecode.transmuter.util.ObjectUtils;


public class TypeTokenValueTypeTest {
  private static final Class<?>[] PRIMITIVE_TYPES;
  private static final Class<?>[] WRAPPER_TYPES;

  private static final TypeToken<?>[] PRIMITIVE_TOKENS;
  private static final TypeToken<?>[] WRAPPER_TOKENS;
  
  private static final ValueType<?>[] VALUE_TYPES;
  
  static {
    PRIMITIVE_TYPES = new Class<?>[] {
      boolean.class, byte.class, char.class, double.class, float.class, 
      int.class, long.class, short.class, void.class
    };
    
    WRAPPER_TYPES = new Class<?>[] {
      Boolean.class, Byte.class, Character.class, Double.class, Float.class, 
      Integer.class, Long.class, Short.class, Void.class
    };
    
    PRIMITIVE_TOKENS = new TypeToken<?>[] {
      TypeToken.get(boolean.class), 
      TypeToken.get(byte.class), 
      TypeToken.get(char.class), 
      TypeToken.get(double.class), 
      TypeToken.get(float.class), 
      TypeToken.get(int.class), 
      TypeToken.get(long.class), 
      TypeToken.get(short.class), 
      TypeToken.get(void.class)
    };
    
    WRAPPER_TOKENS = new TypeToken<?>[] {
      TypeToken.get(Boolean.class), 
      TypeToken.get(Byte.class), 
      TypeToken.get(Character.class), 
      TypeToken.get(Double.class), 
      TypeToken.get(Float.class), 
      TypeToken.get(Integer.class), 
      TypeToken.get(Long.class), 
      TypeToken.get(Short.class), 
      TypeToken.get(Void.class)
    };
    
    VALUE_TYPES = new ValueType<?>[] {
      BOOLEAN, BYTE, CHARACTER, DOUBLE, FLOAT, INTEGER, LONG, SHORT, VOID
    };
  }
  @Test
  public void testIsPrimitive() {
    assertIsPrimitiveForAll(true, PRIMITIVE_TYPES);
    assertIsPrimitiveForAll(true, PRIMITIVE_TOKENS);
    assertIsPrimitiveForAll(false, WRAPPER_TYPES);
    assertIsPrimitiveForAll(false, WRAPPER_TOKENS);
    
    assertFalse(isPrimitive(TypeToken.OBJECT));
    assertFalse(isPrimitive(String.class));
    assertFalse(isPrimitive((Type) null));
    assertFalse(isPrimitive((TypeToken<?>) null));
  }
  
  private void assertIsPrimitiveForAll(boolean result, Class<?>... classes) {
    for(Class<?> cls : classes)
      assertEquals(result, isPrimitive(cls));
  }
  
  private void assertIsPrimitiveForAll(boolean result, TypeToken<?>... tokens) {
    for(TypeToken<?> token : tokens)
      assertEquals(result, isPrimitive(token));
  }
  
  @Test
  public void testIsWrapper() {
    assertIsWrapperForAll(false, PRIMITIVE_TYPES);
    assertIsWrapperForAll(false, PRIMITIVE_TOKENS);
    assertIsWrapperForAll(true, WRAPPER_TYPES);
    assertIsWrapperForAll(true, WRAPPER_TOKENS);
    
    assertFalse(isWrapper(TypeToken.OBJECT));
    assertFalse(isWrapper(String.class));
    assertFalse(isWrapper((Type) null));
    assertFalse(isWrapper((TypeToken<?>) null));
  }
  
  private void assertIsWrapperForAll(boolean result, Class<?>... classes) {
    for(Class<?> cls : classes)
      assertEquals(result, isWrapper(cls));
  }
  
  private void assertIsWrapperForAll(boolean result, TypeToken<?>... tokens) {
    for(TypeToken<?> token : tokens)
      assertEquals(result, isWrapper(token));
  }

  @Test
  public void testMatching() {
    assertMatching(PRIMITIVE_TYPES, WRAPPER_TYPES);
    assertMatching(PRIMITIVE_TOKENS, WRAPPER_TOKENS);
    
    assertNull(matching((Class<?>) null));
    assertNull(matching((Type) null));
    assertNull(matching((TypeToken<?>) null));
    assertNull(matching(String.class));
    assertNull(matching(TypeToken.OBJECT));
  }
  
  private void assertMatching(Class<?>[] primitiveTypes, Class<?>[] wrapperTypes) {
    assertEquals(primitiveTypes.length, wrapperTypes.length);
    
    final int length = primitiveTypes.length;
    for(int i = 0; i < length; i++) {
      for(int j = 0; j < length; j++) {
        assertEquals(i == j, primitiveTypes[i].equals(matching(wrapperTypes[j])));
        assertEquals(i == j, wrapperTypes[i].equals(matching(primitiveTypes[j])));
      }
    }
  }
  
  private void assertMatching(TypeToken<?>[] primitiveTypes, TypeToken<?>[] wrapperTypes) {
    assertEquals(primitiveTypes.length, wrapperTypes.length);
    
    final int length = primitiveTypes.length;
    for(int i = 0; i < length; i++) {
      for(int j = 0; j < length; j++) {
        assertEquals(i == j, primitiveTypes[i].equals(matching(wrapperTypes[j])));
        assertEquals(i == j, wrapperTypes[i].equals(matching(primitiveTypes[j])));
      }
    }
  }

  @Test
  public void testValueOf() {
    assertValueOf(VALUE_TYPES, PRIMITIVE_TYPES);
    assertValueOf(VALUE_TYPES, WRAPPER_TYPES);
    
    assertValueOf(VALUE_TYPES, PRIMITIVE_TOKENS);
    assertValueOf(VALUE_TYPES, WRAPPER_TOKENS);
    
    assertNull(valueOf((Class<?>) null));
    assertNull(valueOf((Type) null));
    assertNull(valueOf((TypeToken<?>) null));
    assertNull(valueOf(String.class));
    assertNull(valueOf(TypeToken.OBJECT));
  }
  
  private void assertValueOf(ValueType<?>[] valueTypes, Class<?>[] classes) {
    assertEquals(valueTypes.length, classes.length);
    
    final int length = valueTypes.length;
    for(int i = 0; i < length; i++) {
      final ValueType<?> valueType = valueTypes[i];
      
      for(int j = 0; j < length; j++)
        assertEquals(i == j, valueType.equals(valueOf(classes[j])));
    }
  }
  
  private void assertValueOf(ValueType<?>[] valueTypes, TypeToken<?>[] tokens) {
    assertEquals(valueTypes.length, tokens.length);
    
    final int length = valueTypes.length;
    for(int i = 0; i < length; i++) {
      final ValueType<?> valueType = valueTypes[i];
      
      for(int j = 0; j < length; j++)
        assertEquals(i == j, valueType.equals(valueOf(tokens[j])));
    }
  }

  @Test
  public void testMatches() {
    assertMatches(VALUE_TYPES, PRIMITIVE_TYPES);
    assertMatches(VALUE_TYPES, WRAPPER_TYPES);
    
    assertMatches(VALUE_TYPES, PRIMITIVE_TOKENS);
    assertMatches(VALUE_TYPES, WRAPPER_TOKENS);
    
    assertFalseMatches(VALUE_TYPES);
  }
  
  private void assertMatches(ValueType<?>[] valueTypes, Class<?>[] classes) {
    assertEquals(valueTypes.length, classes.length);
    
    final int length = valueTypes.length;
    
    for(int i = 0; i < length; i++) {
      final ValueType<?> valueType = valueTypes[i];
      
      for(int j = 0; j < length; j++)
        assertEquals(i == j, valueType.matches(classes[j]));
    }
  }
  
  private void assertMatches(ValueType<?>[] valueTypes, TypeToken<?>[] tokens) {
    assertEquals(valueTypes.length, tokens.length);
    
    final int length = valueTypes.length;
    
    for(int i = 0; i < length; i++) {
      final ValueType<?> valueType = valueTypes[i];
      
      for(int j = 0; j < length; j++)
        assertEquals(i == j, valueType.matches(tokens[j]));
    }
  }
  
  private void assertFalseMatches(ValueType<?>[] valueTypes) {
    for(ValueType<?> vt : valueTypes) {
      assertFalse(vt.matches((Type) null));
      assertFalse(vt.matches((TypeToken<?>) null));
      assertFalse(vt.matches(String.class));
      assertFalse(vt.matches(TypeToken.OBJECT));
    }
  }
  
  @Test
  public void testToString() {
    assertEquals("ValueType<boolean>", ValueType.BOOLEAN.toString());
    assertEquals("ValueType<byte>", ValueType.BYTE.toString());
    assertEquals("ValueType<char>", ValueType.CHARACTER.toString());
    assertEquals("ValueType<double>", ValueType.DOUBLE.toString());
    assertEquals("ValueType<float>", ValueType.FLOAT.toString());
    assertEquals("ValueType<int>", ValueType.INTEGER.toString());
    assertEquals("ValueType<long>", ValueType.LONG.toString());
    assertEquals("ValueType<short>", ValueType.SHORT.toString());
    assertEquals("ValueType<void>", ValueType.VOID.toString());
  }
  
  @Test
  public void testCastingToBoolean() {
    assertEquals(true, ValueType.BOOLEAN.cast(true));
    assertEquals(false, ValueType.BOOLEAN.cast(false));
    
    try {
      ValueType.BOOLEAN.cast(null);
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
    
    try {
      ValueType.BOOLEAN.cast((byte) 1);
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
    
    try {
      ValueType.BOOLEAN.cast('a');
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
    
    try {
      ValueType.BOOLEAN.cast(1.0);
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
    
    try {
      ValueType.BOOLEAN.cast(1.0f);
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
    
    try {
      ValueType.BOOLEAN.cast(1);
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
    
    try {
      ValueType.BOOLEAN.cast(1L);
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
    
    try {
      ValueType.BOOLEAN.cast((short) 1);
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
    
    try {
      ValueType.BOOLEAN.cast(new Object());
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
  }
  
  @Test
  public void testCastingToByte() {
    assertEquals(new Byte((byte) 1), ValueType.BYTE.cast((byte) 1));
    assertTrue(ObjectUtils.areEqual((byte) 255, ValueType.BYTE.cast((byte) 255)));
    
    try {
      ValueType.BYTE.cast(null);
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
    
    try {
      ValueType.BYTE.cast(false);
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
    
    assertEquals(new Byte((byte) 'a'), ValueType.BYTE.cast('a'));
    assertEquals(new Byte((byte) 1.0), ValueType.BYTE.cast(1.0));
    assertEquals(new Byte((byte) 1.0f), ValueType.BYTE.cast(1.0f));
    assertEquals(new Byte((byte) 1), ValueType.BYTE.cast(1));
    assertEquals(new Byte((byte) 1L), ValueType.BYTE.cast(1L));
    assertEquals(new Byte((byte) 1), ValueType.BYTE.cast((short) 1));
    
    try {
      ValueType.BYTE.cast(new Object());
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
  }
  
  @Test
  public void testCastingToCharacter() {
    assertEquals(new Character('a'), ValueType.CHARACTER.cast('a'));
    
    try {
      ValueType.CHARACTER.cast(null);
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
    
    try {
      ValueType.CHARACTER.cast(false);
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
    
    assertEquals(new Character((char) ((byte) 1)), ValueType.CHARACTER.cast((byte) 1));
    assertEquals(new Character((char) 1.0), ValueType.CHARACTER.cast(1.0));
    assertEquals(new Character((char) 1.0f), ValueType.CHARACTER.cast(1.0f));
    assertEquals(new Character((char) 1), ValueType.CHARACTER.cast(1));
    assertEquals(new Character((char) 1L), ValueType.CHARACTER.cast(1L));
    assertEquals(new Character((char) ((short) 1)), ValueType.CHARACTER.cast((short) 1));
    
    try {
      ValueType.CHARACTER.cast(new Object());
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
  }
  
  @Test
  public void testCastingToDouble() {
    assertEquals(new Double(1.0), ValueType.DOUBLE.cast(1.0));
    
    try {
      ValueType.DOUBLE.cast(null);
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
    
    try {
      ValueType.DOUBLE.cast(false);
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
    
    assertEquals(new Double((byte) 1), ValueType.DOUBLE.cast((byte) 1));
    assertEquals(new Double('a'), ValueType.DOUBLE.cast('a'));
    assertEquals(new Double(1.0f), ValueType.DOUBLE.cast(1.0f));
    assertEquals(new Double(1), ValueType.DOUBLE.cast(1));
    assertEquals(new Double(1L), ValueType.DOUBLE.cast(1L));
    assertEquals(new Double((short) 1), ValueType.DOUBLE.cast((short) 1));
    
    try {
      ValueType.DOUBLE.cast(new Object());
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
  }
  
  @Test
  public void testCastingToFloat() {
    assertEquals(new Float(1.0f), ValueType.FLOAT.cast(1.0f));
    
    try {
      ValueType.FLOAT.cast(null);
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
    
    try {
      ValueType.FLOAT.cast(false);
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
    
    assertEquals(new Float((byte) 1), ValueType.FLOAT.cast((byte) 1));
    assertEquals(new Float('a'), ValueType.FLOAT.cast('a'));
    assertEquals(new Float(1.0), ValueType.FLOAT.cast(1.0));
    assertEquals(new Float(1), ValueType.FLOAT.cast(1));
    assertEquals(new Float(1L), ValueType.FLOAT.cast(1L));
    assertEquals(new Float((short) 1), ValueType.FLOAT.cast((short) 1));
    
    try {
      ValueType.FLOAT.cast(new Object());
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
  }
  
  @Test
  public void testCastingToInt() {
    assertEquals(new Integer(1), ValueType.INTEGER.cast(1));
    
    try {
      ValueType.INTEGER.cast(null);
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
    
    try {
      ValueType.INTEGER.cast(false);
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
    
    assertEquals(new Integer((byte) 1), ValueType.INTEGER.cast((byte) 1));
    assertEquals(new Integer('a'), ValueType.INTEGER.cast('a'));
    assertEquals(new Integer((int) 1.0), ValueType.INTEGER.cast(1.0));
    assertEquals(new Integer((int) 1.0f), ValueType.INTEGER.cast(1.0f));
    assertEquals(new Integer((int) 1L), ValueType.INTEGER.cast(1L));
    assertEquals(new Integer((short) 1), ValueType.INTEGER.cast((short) 1));
    
    try {
      ValueType.INTEGER.cast(new Object());
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
  }
  
  @Test
  public void testCastingToLong() {
    assertEquals(new Long(1L), ValueType.LONG.cast(1L));
    
    try {
      ValueType.LONG.cast(null);
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
    
    try {
      ValueType.LONG.cast(false);
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
    
    assertEquals(new Long((byte) 1), ValueType.LONG.cast((byte) 1));
    assertEquals(new Long('a'), ValueType.LONG.cast('a'));
    assertEquals(new Long((long) 1.0), ValueType.LONG.cast(1.0));
    assertEquals(new Long((long) 1.0f), ValueType.LONG.cast(1.0f));
    assertEquals(new Long(1), ValueType.LONG.cast(1));
    assertEquals(new Long((short) 1), ValueType.LONG.cast((short) 1));
    
    try {
      ValueType.LONG.cast(new Object());
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
  }
  
  @Test
  public void testCastingToShort() {
    assertEquals(new Short((short) 1), ValueType.SHORT.cast((short) 1));
    
    try {
      ValueType.SHORT.cast(null);
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
    
    try {
      ValueType.SHORT.cast(false);
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
    
    assertEquals(new Short((byte) 1), ValueType.SHORT.cast((byte) 1));
    assertEquals(new Short((short) 'a'), ValueType.SHORT.cast('a'));
    assertEquals(new Short((short) 1.0), ValueType.SHORT.cast(1.0));
    assertEquals(new Short((short) 1.0f), ValueType.SHORT.cast(1.0f));
    assertEquals(new Short((short) 1), ValueType.SHORT.cast(1));
    assertEquals(new Short((short) 1L), ValueType.SHORT.cast(1L));
    
    try {
      ValueType.SHORT.cast(new Object());
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
  }
  
  @Test
  public void testCastingToVoid() {
    assertNull(ValueType.VOID.cast(null));
    
    try {
      ValueType.VOID.cast(false);
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
    
    try {
      ValueType.VOID.cast((byte) 1);
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
    
    try {
      ValueType.VOID.cast('a');
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
    
    try {
      ValueType.VOID.cast(1.0);
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
    
    try {
      ValueType.VOID.cast(1.0f);
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
    
    try {
      ValueType.VOID.cast(1);
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
    
    try {
      ValueType.VOID.cast(1L);
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
    
    try {
      ValueType.VOID.cast((short) 1);
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
    
    try {
      ValueType.VOID.cast(new Object());
      fail();
    } catch(ClassCastException e) {
      // empty block
    }
  }
}
