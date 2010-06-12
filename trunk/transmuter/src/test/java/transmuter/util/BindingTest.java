package transmuter.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import transmuter.Converts;
import transmuter.type.TypeToken;
import transmuter.util.exception.BindingInstantiationException;
import transmuter.util.exception.BindingInvocationException;
import transmuter.util.exception.InaccessibleMethodException;
import transmuter.util.exception.MethodInstanceIncompatibilityException;
import transmuter.util.exception.NullInstanceWithNonStaticMethodException;

public class BindingTest {
  private String string;
  private Method substringMethod;
  private Method valueOfMethod;
  private Binding substring;
  private Binding valueOf;
  
  @Before
  public void setUp() throws SecurityException, NoSuchMethodException {
    string = "0123456789";
    substringMethod = extractMethod(String.class, "substring", int.class, int.class);
    valueOfMethod = extractMethod(String.class, "valueOf", Object.class);
    
    substring = new Binding(string, substringMethod);
    valueOf = new Binding(null, valueOfMethod);
  }
  
  @Test
  public void constructor() {
    assertEquals(string, substring.getInstance());
    assertEquals(substringMethod, substring.getMethod());
    assertNull(valueOf.getInstance());
    assertEquals(valueOfMethod, valueOf.getMethod());
  }
  
  @Test
  public void unaryConstructor() {
    Binding b = new Binding(valueOfMethod);
    
    assertNull(b.getInstance());
    assertNotNull(b.getMethod());
    assertEquals("null", b.invoke((Object) null));
    assertEquals("something", b.invoke("something"));
    
    assertEquals(valueOf, b);
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
    final Method waitMethod = extractMethod(Object.class, "wait");
    Binding b = new Binding(string, waitMethod);
    
    assertEquals(string, b.getInstance());
    assertEquals(waitMethod, b.getMethod());
  }
  
  @Test
  public void constructorWithIncompatibleInstanceAndMethod() throws SecurityException, NoSuchMethodException {
    try {
      new Binding("0123456789", extractMethod(Pair.class, "getFromType"));
      fail();
    } catch(BindingInstantiationException e) {
      assertEquals(1, e.getCauses().size());
      assertEquals(MethodInstanceIncompatibilityException.class, e.getCauses().get(0).getClass());
      
      MethodInstanceIncompatibilityException ex = (MethodInstanceIncompatibilityException) e.getCauses().get(0);
      assertEquals("0123456789", ex.getInstance());
      assertEquals(extractMethod(Pair.class, "getFromType"), ex.getMethod());
    }
  }
  
  @Test
  public void constructorWithNullInstanceAndNonStaticMethod() throws SecurityException, NoSuchMethodException {
    try {
      new Binding(null, extractMethod(Pair.class, "getFromType"));
      fail();
    } catch(BindingInstantiationException e) {
      assertEquals(1, e.getCauses().size());
      assertEquals(NullInstanceWithNonStaticMethodException.class, e.getCauses().get(0).getClass());
      
      NullInstanceWithNonStaticMethodException ex = (NullInstanceWithNonStaticMethodException) e.getCauses().get(0);
      assertEquals(extractMethod(Pair.class, "getFromType"), ex.getMethod());
    }
  }
  
  @Test
  public void constructorWithNonPublicMethod() throws SecurityException, NoSuchMethodException {
    try {
      new Binding(null, extractDeclaredMethod(TypeToken.class, "getRawType", Type.class));
      fail();
    } catch(BindingInstantiationException e) {
      assertEquals(1, e.getCauses().size());
      assertEquals(InaccessibleMethodException.class, e.getCauses().get(0).getClass());
      
      InaccessibleMethodException ex = (InaccessibleMethodException) e.getCauses().get(0);
      assertEquals(extractDeclaredMethod(TypeToken.class, "getRawType", Type.class), ex.getMethod());
    }
  }
  
  @Test
  public void constructWithAnonymousClass() throws SecurityException, NoSuchMethodException {
    Object inner = new Object() {
      @SuppressWarnings("unused") // just to shut up Eclipse's warnings
      @Converts
      public String stringify(Object o) {
        return String.valueOf(o);
      }
    };
    
    Binding binding = new Binding(inner, extractMethod(inner.getClass(), "stringify", Object.class));
    assertEquals("42", binding.invoke(42));
    assertEquals("sbrubbles", binding.invoke("sbrubbles"));
    assertEquals("true", binding.invoke(true));
    assertEquals("java.lang.Object -> java.lang.Object", binding.invoke(new Pair(Object.class, Object.class)));
  }
  
  
  @Test
  public void equals() {
    assertEquals(substring, substring);
    assertEquals(substring, new Binding(string, substringMethod));
    assertEquals(substring, new Binding(new String(string), substringMethod));
    
    assertFalse(substring.equals(null));
    assertFalse(substring.equals(new Binding(string, substringMethod) { /* empty block */ }));
    assertFalse(substring.equals(new Binding("woeihoiwefn", substringMethod) { /* empty block */ }));
    assertFalse(substring.equals(new Binding(string, valueOfMethod) { /* empty block */ }));
  }
  
  @Test
  public void invoke() {
    assertEquals("012", substring.invoke(0, 3));
    assertEquals("34", substring.invoke(3, 5));
    assertEquals(string, substring.invoke(0, string.length()));
  }
  
  @Test
  public void invokeWithWrappers() {
    assertEquals("012", substring.invoke(new Integer(0), new Integer(3)));
    assertEquals("34", substring.invoke(new Integer(3), 5)); // mixing it up
    assertEquals(string, substring.invoke(new Integer(0), new Integer(string.length())));
  }
  
  @Test
  public void invokeWithIllegalArguments() {
    try {
      substring.invoke(false, 0.0);
      fail();
    } catch(Exception e) {
      assertType(BindingInvocationException.class, e);
      assertEquals(substring, ((BindingInvocationException) e).getBinding());
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
      assertEquals(substring, ((BindingInvocationException) e).getBinding());
      assertType(InvocationTargetException.class, e.getCause());
      assertType(StringIndexOutOfBoundsException.class, e.getCause().getCause());
    }
  }
  
  @Test
  public void getDeclaringType() throws SecurityException, NoSuchMethodException {
    assertEquals(String.class, substring.getInstanceClass());
    assertEquals(String.class, valueOf.getInstanceClass());
    
    final Method listEquals = extractMethod(List.class, "equals", Object.class);
    assertEquals(ArrayList.class, new Binding(new ArrayList<Object>(), listEquals).getInstanceClass());
    assertEquals(LinkedList.class, new Binding(new LinkedList<Object>(), listEquals).getInstanceClass());
  }
  
  private void assertType(Class<?> cls, Object object) {
    assertNotNull(object);
    assertEquals(cls, object.getClass());
  }
  
  private static Method extractMethod(Class<?> cls, String name,
      Class<?>... parameterTypes) throws NoSuchMethodException,
      SecurityException {
    return cls.getMethod(name, parameterTypes);
  }
  
  private static Method extractDeclaredMethod(Class<?> cls, String name,
      Class<?>... parameterTypes) throws NoSuchMethodException,
      SecurityException {
    return cls.getDeclaredMethod(name, parameterTypes);
  }
}
