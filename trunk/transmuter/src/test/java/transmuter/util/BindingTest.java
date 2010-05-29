package transmuter.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.junit.Before;
import org.junit.Test;

import transmuter.type.TypeToken;
import transmuter.util.exception.BindingInstantiationException;
import transmuter.util.exception.BindingInvocationException;
import transmuter.util.exception.InaccessibleMethodException;
import transmuter.util.exception.MethodInstanceIncompatibilityException;
import transmuter.util.exception.NullInstanceWithNonStaticMethodException;

public class BindingTest {
  private Binding substring;
  private String string;
  private Method substringMethod;

  @Before
  public void setUp() throws SecurityException, NoSuchMethodException {
    string = "0123456789";
    substringMethod = ReflectionUtils.extractMethod(String.class, "substring", int.class, int.class);
    substring = new Binding(string, substringMethod);
  }
  
  @Test
  public void constructor() {
    assertEquals(string, substring.getInstance());
    assertEquals(substringMethod, substring.getMethod());
  }
  
  @Test
  public void constructorWithStaticMethod() throws SecurityException, NoSuchMethodException {
    Binding b = new Binding(null, ReflectionUtils.extractMethod(String.class, "valueOf", Object.class));
    
    assertNull(b.getInstance());
    assertNotNull(b.getMethod());
    assertEquals("null", b.invoke((Object) null));
    assertEquals("something", b.invoke("something"));
  }
  
  @Test
  public void constructorWithNullMethod() {
    try {
      new Binding(new Object(), null);
      fail();
    } catch(BindingInstantiationException e) {
      assertEquals(1, e.getCauses().size());
      assertEquals(IllegalArgumentException.class, e.getCauses().get(0).getClass());
    }
  }

  @Test
  public void constructorWithInstanceAndInheritedMethod() throws SecurityException, NoSuchMethodException {
    final Method waitMethod = ReflectionUtils.extractMethod(Object.class, "wait");
    Binding b = new Binding(string, waitMethod);
    
    assertEquals(string, b.getInstance());
    assertEquals(waitMethod, b.getMethod());
  }
  
  @Test
  public void constructorWithIncompatibleInstanceAndMethod() throws SecurityException, NoSuchMethodException {
    try {
      new Binding("0123456789", ReflectionUtils.extractMethod(Pair.class, "getFromType"));
      fail();
    } catch(BindingInstantiationException e) {
      assertEquals(1, e.getCauses().size());
      assertEquals(MethodInstanceIncompatibilityException.class, e.getCauses().get(0).getClass());
      
      MethodInstanceIncompatibilityException ex = (MethodInstanceIncompatibilityException) e.getCauses().get(0);
      assertEquals("0123456789", ex.getInstance());
      assertEquals(ReflectionUtils.extractMethod(Pair.class, "getFromType"), ex.getMethod());
    }
  }
  
  @Test
  public void constructorWithNullInstanceAndNonStaticMethod() throws SecurityException, NoSuchMethodException {
    try {
      new Binding(null, ReflectionUtils.extractMethod(Pair.class, "getFromType"));
      fail();
    } catch(BindingInstantiationException e) {
      assertEquals(1, e.getCauses().size());
      assertEquals(NullInstanceWithNonStaticMethodException.class, e.getCauses().get(0).getClass());
      
      NullInstanceWithNonStaticMethodException ex = (NullInstanceWithNonStaticMethodException) e.getCauses().get(0);
      assertEquals(ReflectionUtils.extractMethod(Pair.class, "getFromType"), ex.getMethod());
    }
  }
  
  @Test
  public void constructorWithNonPublicMethod() throws SecurityException, NoSuchMethodException {
    try {
      new Binding(null, ReflectionUtils.extractMethod(TypeToken.class, "getRawType", Type.class));
      fail();
    } catch(BindingInstantiationException e) {
      assertEquals(1, e.getCauses().size());
      assertEquals(InaccessibleMethodException.class, e.getCauses().get(0).getClass());
      
      InaccessibleMethodException ex = (InaccessibleMethodException) e.getCauses().get(0);
      assertEquals(ReflectionUtils.extractMethod(TypeToken.class, "getRawType", Type.class), ex.getMethod());
    }
  }
  
  @Test
  public void equals() throws SecurityException, NoSuchMethodException {
    assertEquals(substring, substring);
    assertEquals(substring, new Binding(string, substringMethod));
    assertEquals(substring, new Binding(new String(string), substringMethod));
    
    assertFalse(substring.equals(null));
    assertFalse(substring.equals(new Binding(string, substringMethod) {}));
    assertFalse(substring.equals(new Binding("woeihoiwefn", substringMethod) {}));
    assertFalse(substring.equals(new Binding(string, ReflectionUtils.extractMethod(String.class, "valueOf", Object.class)) {}));
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
  
  private void assertType(Class<?> cls, Object object) {
    assertNotNull(object);
    assertEquals(cls, object.getClass());
  }
}
