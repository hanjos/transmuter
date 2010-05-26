package transmuter.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static transmuter.util.ObjectUtils.areEqual;
import static transmuter.util.ObjectUtils.classOf;
import static transmuter.util.ObjectUtils.hashCodeOf;
import static transmuter.util.ObjectUtils.nonNull;

import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Test;

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
  
  // TODO primitive/wrapper problem
  @Ignore
  public void testAreEqualForPrimitives() {
    assertTrue(areEqual(42, new Integer(42)));
    assertTrue(areEqual(0.0, 0.0f));
    assertTrue(areEqual(42L, new Integer(42)));
  }
  
  @Test
  public void testClassOf() {
    assertNull(classOf(null));
    assertEquals(Object.class, classOf(new Object()));
    assertEquals(String.class, classOf("abcdef"));
    assertEquals(ArrayList.class, classOf(new ArrayList<String>()));
  }
}
