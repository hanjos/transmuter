package transmuter.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static transmuter.type.TypeToken.ValueType.BOOLEAN;
import static transmuter.type.TypeToken.ValueType.BYTE;
import static transmuter.type.TypeToken.ValueType.CHARACTER;
import static transmuter.type.TypeToken.ValueType.DOUBLE;
import static transmuter.type.TypeToken.ValueType.FLOAT;
import static transmuter.type.TypeToken.ValueType.INTEGER;
import static transmuter.type.TypeToken.ValueType.LONG;
import static transmuter.type.TypeToken.ValueType.SHORT;
import static transmuter.type.TypeToken.ValueType.VOID;
import static transmuter.type.TypeToken.ValueType.isPrimitive;
import static transmuter.type.TypeToken.ValueType.isWrapper;
import static transmuter.type.TypeToken.ValueType.matching;
import static transmuter.type.TypeToken.ValueType.valueOf;

import java.lang.reflect.Type;

import org.junit.Test;

import transmuter.type.TypeToken.ValueType;

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
}
