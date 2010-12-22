package com.googlecode.transmuter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Collection;

public class TestUtils {
  public static Method extractMethod(Class<?> cls, String name, Class<?>... parameterTypes) 
      throws NoSuchMethodException, SecurityException {
    return cls.getMethod(name, parameterTypes);
  }

  public static Method extractDeclaredMethod(Class<?> cls, String name, Class<?>... parameterTypes) 
      throws NoSuchMethodException, SecurityException {
    return cls.getDeclaredMethod(name, parameterTypes);
  }

  public static void assertType(Class<?> cls, Object object) {
    assertNotNull(object);
    assertEquals(cls, object.getClass());
  }

  public static void assertMatchingCollections(final Collection<?> a, final Collection<?> b) {
    if(a == b)
      return;
    
    assertNotNull(a);
    assertNotNull(b);
    
    assertTrue(a.containsAll(b));
    assertTrue(b.containsAll(a));
  }
}
