package com.googlecode.transmuter.util;

import org.junit.Test;

import java.util.*;

import static com.googlecode.transmuter.util.ObjectUtils.*;
import static org.junit.Assert.*;

public class ObjectUtilsTest {
  @Test
  public void testNonNullWithNonNull() {
    final Object object = new Object();
    
    assertSame(object, nonNull(object));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testNonNullWithNull() {
    nonNull(null);
  }
  
  @Test
  public void testHashCodeOf() {
    final Object object = new Object();
    
    assertEquals(object.hashCode(), hashCodeOf(object));
    assertEquals(0, hashCodeOf(null));
  }
  
  @Test
  public void testAreEqual() {
    final Object object = new Object();
    final Object equalsNull = new Object() {
      @Override
      public boolean equals(Object o) {
        return o == this || o == null;
      }
      
      @Override
      public int hashCode() { return 1; }
    };
    
    assertTrue(areEqual(object, object));
    assertTrue(areEqual(equalsNull, equalsNull));
    assertTrue(areEqual(equalsNull, null));
    assertTrue(areEqual(null, equalsNull));
    assertTrue(areEqual(null, null));
    assertTrue(areEqual("abcdef", "abcdef"));
    
    assertFalse(areEqual(object, new Object()));
    assertFalse(areEqual(object, null));
    assertFalse(areEqual(null, object));
  }
  
  @Test
  public void testAreEqualForPrimitives() {
    assertTrue(areEqual(42, new Integer(42)));
    
    // Java doesn't do automatic casting for wrappers
    assertFalse(areEqual(0.0, 0.0f));
    assertFalse(areEqual(42L, new Integer(42)));
  }
  
  @Test
  public void testClassOf() {
    assertNull(classOf(null));
    assertEquals(Object.class, classOf(new Object()));
    assertEquals(String.class, classOf("aoiashni"));
    assertEquals(Long.class, classOf(42L));
    assertEquals(ArrayList.class, classOf(new ArrayList<String>()));
  }
  
  @Test
  public void testIsEmptyWithArray() {
    assertTrue(isEmpty());
    assertTrue(isEmpty((Object[]) null));
    assertTrue(isEmpty(new String[0]));
    assertTrue(isEmpty(new String[] {}));
    assertTrue(isEmpty(new String[][] {}));
    
    assertFalse(isEmpty((Object) null));
    assertFalse(isEmpty(new String[][] { {} }));
    assertFalse(isEmpty(""));
    assertFalse(isEmpty(0));
    assertFalse(isEmpty(false));
  }
  
  @SuppressWarnings("serial")
  @Test
  public void testIsIterableEmpty() {
    assertTrue(isEmpty((Iterable<?>) null));
    assertTrue(isEmpty(new HashSet<Object>()));
    assertTrue(isEmpty(Collections.emptyList()));
    assertTrue(isEmpty(Collections.emptyMap().keySet()));
    
    assertFalse(isEmpty(Arrays.asList((Object) null)));
    assertFalse(isEmpty(new HashMap<String, String>() {{ put("", ""); }}));
  }
}
