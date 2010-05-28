package transmuter.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

import transmuter.util.exception.BindingInvocationException;

public class BindingTest {
  private Binding substring;
  private String string;
  private Method substringMethod;

  @Before
  public void setUp() throws SecurityException, NoSuchMethodException {
    string = "0123456789";
    substringMethod = extractMethod(String.class, "substring", int.class, int.class);
    substring = new Binding(string, substringMethod);
  }
  
  @Test
  public void constructor() {
    assertEquals(string, substring.getInstance());
    assertEquals(substringMethod, substring.getMethod());
  }
  
  @Test
  public void constructorWithStaticMethod() throws SecurityException, NoSuchMethodException {
    Binding b = new Binding(null, extractMethod(String.class, "valueOf", Object.class));
    
    assertNull(b.getInstance());
    assertNotNull(b.getMethod());
    assertEquals("null", b.invoke((Object) null));
    assertEquals("something", b.invoke("something"));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void constructorWithNullMethod() {
    new Binding(new Object(), null);
  }

  @Test
  public void equals() throws SecurityException, NoSuchMethodException {
    assertEquals(substring, substring);
    assertEquals(substring, new Binding(string, substringMethod));
    assertEquals(substring, new Binding(new String(string), substringMethod));
    
    assertFalse(substring.equals(null));
    assertFalse(substring.equals(new Binding(string, substringMethod) {}));
    assertFalse(substring.equals(new Binding("woeihoiwefn", substringMethod) {}));
    assertFalse(substring.equals(new Binding(string, extractMethod(String.class, "valueOf", Object.class)) {}));
  }
  
  @Test
  public void invoke() {
    assertEquals("012", substring.invoke(0, 3));
    assertEquals("34", substring.invoke(3, 5));
    assertEquals(string, substring.invoke(0, string.length()));
  }
  
  @Test
  public void invokeWithIllegalArguments() {
    try {
      substring.invoke(false, 0.0);
      fail();
    } catch(Exception e) {
      assertType(BindingInvocationException.class, e);
      assertType(IllegalArgumentException.class, e.getCause());
    }
  }
  
  @Test
  public void invokeWithTargetException() {
    try {
      substring.invoke(-1, 9);
      fail();
    } catch(Exception e) {
      assertType(BindingInvocationException.class, e);
      assertType(InvocationTargetException.class, e.getCause());
      assertType(StringIndexOutOfBoundsException.class, e.getCause().getCause());
    }
  }
  
  private Method extractMethod(Class<?> cls, String name, Class<?>... parameterTypes) 
  throws NoSuchMethodException, SecurityException {
    return cls.getMethod(name, parameterTypes);
  }
  
  private void assertType(Class<?> cls, Object object) {
    assertNotNull(object);
    assertEquals(cls, object.getClass());
  }
}
