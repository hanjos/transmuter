package com.googlecode.transmuter.util;

import static com.googlecode.transmuter.util.ReflectionUtils.getTypeName;
import static com.googlecode.transmuter.util.ReflectionUtils.getTypeNames;
import static com.googlecode.transmuter.util.ReflectionUtils.simpleMethodToString;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.googlecode.transmuter.type.TypeToken;


public class ReflectionUtilsTest {
  private static final Type ARRAY_OF_ARRAY_OF_LIST_OF_SUPER_STRING = new TypeToken<List<? super String>[][]>() {}.getType();

  @Test
  public void testGetTypeName() {
    assertEquals("null", getTypeName(null));
    assertEquals("double", getTypeName(double.class));
    assertEquals("java.lang.Double", getTypeName(Double.class));
    assertEquals("java.lang.Class", getTypeName(Class.class));
    assertEquals("java.lang.Class<?>", getTypeName(new TypeToken<Class<?>>() {}.getType()));
    assertEquals("java.util.List<java.lang.String>", getTypeName(new TypeToken<List<String>>() {}.getType()));
    assertEquals("java.util.List<? super java.lang.String>[][]", getTypeName(ARRAY_OF_ARRAY_OF_LIST_OF_SUPER_STRING));
  }
  
  @Test
  public void testSimpleMethodToString() throws SecurityException, NoSuchMethodException {
    assertEquals(
        "<null>",
        simpleMethodToString(null));
    
    assertEquals(
        "public java.lang.String substring(int)",
        simpleMethodToString(extractMethod(String.class, "substring", int.class)));
    
    assertEquals(
        "public static java.lang.String valueOf(char[], int, int)",
        simpleMethodToString(extractMethod(String.class, "valueOf", char[].class, int.class, int.class)));
    
    assertEquals(
        "public java.lang.String toString()",
        simpleMethodToString(extractMethod(String.class, "toString")));
    
    assertEquals(
        "public static transient <T> java.util.List<T> asList(T...)",
        simpleMethodToString(extractMethod(Arrays.class, "asList", Object[].class)));
    
    assertEquals(
        "public void testSimpleMethodToString() throws java.lang.SecurityException, java.lang.NoSuchMethodException",
        simpleMethodToString(extractMethod(ReflectionUtilsTest.class, "testSimpleMethodToString")));
  }
  
  @Test
  public void testGetTypeNames() {
    assertArrayEquals(
        new String[] { "double", "java.lang.String", "null", "java.util.List<? super java.lang.String>[][]" },
        getTypeNames(double.class, String.class, null, ARRAY_OF_ARRAY_OF_LIST_OF_SUPER_STRING));
    assertArrayEquals(
        new String[0], 
        getTypeNames((Type[]) null));
    assertArrayEquals(
        new String[0], 
        getTypeNames());
  }
  
  private static Method extractMethod(Class<?> cls, String name,
      Class<?>... parameterTypes) throws NoSuchMethodException,
      SecurityException {
    return cls.getDeclaredMethod(name, parameterTypes);
  }
}
