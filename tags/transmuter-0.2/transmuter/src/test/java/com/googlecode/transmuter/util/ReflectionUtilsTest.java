package com.googlecode.transmuter.util;

import com.googlecode.transmuter.converter.exception.MethodOwnerTypeIncompatibilityException;
import com.googlecode.transmuter.type.TypeToken;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

import static com.googlecode.transmuter.TestUtils.extractDeclaredMethod;
import static com.googlecode.transmuter.util.ReflectionUtils.*;
import static org.junit.Assert.*;

public class ReflectionUtilsTest {
  private static final Type ARRAY_OF_ARRAY_OF_LIST_OF_SUPER_STRING = 
    new TypeToken<List<? super String>[][]>() { /**/ }.getType();

  @Test
  public void testGetTypeName() {
    assertEquals("null", getTypeName(null));
    assertEquals("double", getTypeName(double.class));
    assertEquals("java.lang.Double", getTypeName(Double.class));
    assertEquals("java.lang.Class", getTypeName(Class.class));
    assertEquals("java.lang.Class<?>", getTypeName(new TypeToken<Class<?>>() { /**/ }.getType()));
    assertEquals("java.util.List<java.lang.String>", getTypeName(new TypeToken<List<String>>() { /**/ }.getType()));
    assertEquals("java.util.List<? super java.lang.String>[][]", getTypeName(ARRAY_OF_ARRAY_OF_LIST_OF_SUPER_STRING));
  }
  
  @Test
  public void testSimpleMethodToString() throws SecurityException, NoSuchMethodException {
    assertEquals(
        "<null>",
        simpleMethodToString(null));
    
    assertEquals(
        "public java.lang.String substring(int)",
        simpleMethodToString(extractDeclaredMethod(String.class, "substring", int.class)));
    
    assertEquals(
        "public static java.lang.String valueOf(char[], int, int)",
        simpleMethodToString(extractDeclaredMethod(String.class, "valueOf", char[].class, int.class, int.class)));
    
    assertEquals(
        "public java.lang.String toString()",
        simpleMethodToString(extractDeclaredMethod(String.class, "toString")));
    
    assertEquals(
        "public static transient <T> java.util.List<T> asList(T...)",
        simpleMethodToString(extractDeclaredMethod(Arrays.class, "asList", Object[].class)));
    
    assertEquals(
        "public void testSimpleMethodToString() throws java.lang.SecurityException, java.lang.NoSuchMethodException",
        simpleMethodToString(extractDeclaredMethod(ReflectionUtilsTest.class, "testSimpleMethodToString")));
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
  
  @Test
  public void testIsCompatible() throws SecurityException, NoSuchMethodException {
    Method substring1 = extractDeclaredMethod(String.class, "substring", int.class);
    assertTrue(isCompatible(substring1, String.class));
    assertFalse(isCompatible(substring1, Object.class));
    
    assertFalse(isCompatible(substring1, null));
    assertFalse(isCompatible(null, Object.class));
    assertFalse(isCompatible(null, null));
    
    Method contains = extractDeclaredMethod(Collection.class, "contains", Object.class);
    assertTrue(isCompatible(contains, Collection.class));
    assertTrue(isCompatible(contains, Set.class));
    assertTrue(isCompatible(contains, List.class));
    assertTrue(isCompatible(contains, ArrayList.class));
  }
  
  @Test
  public void testGetOwnerType() throws SecurityException, NoSuchMethodException {
    Method substring1 = extractDeclaredMethod(String.class, "substring", int.class);
    assertEquals(String.class, getOwnerType("", substring1));
    
    assertEquals(String.class, getOwnerType(null, substring1));
    assertNull(getOwnerType("", null));
    assertNull(getOwnerType(null, null));
    
    Method contains = extractDeclaredMethod(Collection.class, "contains", Object.class);
    assertEquals(HashSet.class, getOwnerType(new HashSet<Object>(), contains));
    assertEquals(HashSet.class, getOwnerType(new HashSet<String>(), contains));
    assertEquals(ArrayList.class, getOwnerType(new ArrayList<Object>(), contains));
    assertEquals(Collection.class, getOwnerType(null, contains));
    
    try {
      getOwnerType(new Object(), contains);
      fail();
    } catch (MethodOwnerTypeIncompatibilityException e) {
      assertEquals(contains, e.getMethod());
      assertEquals(Object.class, e.getOwnerType());
    }
  }
}
