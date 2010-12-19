package com.googlecode.transmuter.converter;

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

import com.googlecode.transmuter.Converts;
import com.googlecode.transmuter.TestUtils;
import com.googlecode.transmuter.converter.exception.BindingInvocationException;
import com.googlecode.transmuter.converter.exception.InaccessibleMethodException;
import com.googlecode.transmuter.converter.exception.MethodInstanceIncompatibilityException;
import com.googlecode.transmuter.converter.exception.NullInstanceWithNonStaticMethodException;
import com.googlecode.transmuter.type.TypeToken;
import com.googlecode.transmuter.util.exception.ObjectInstantiationException;

public class BindingTest {
  private String string;
  private Method substringMethod;
  private Method valueOfMethod;
  private Binding substring;
  private Binding valueOf;
  
  @Before
  public void setUp() throws SecurityException, NoSuchMethodException {
    string = "0123456789";
    substringMethod = TestUtils.extractMethod(String.class, "substring", int.class, int.class);
    valueOfMethod = TestUtils.extractMethod(String.class, "valueOf", Object.class);
    
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
    Object instance = new Object();
    try {
      new Binding(instance, null);
      fail();
    } catch(ObjectInstantiationException e) {
      assertEquals(Binding.class, e.getObjectType());
      assertEquals(instance, e.getArguments().get(0));
      assertNull(e.getArguments().get(1));
      
      assertEquals(1, e.getCauses().size());
      assertEquals(IllegalArgumentException.class, e.getCauses().get(0).getClass());
    }
  }

  @Test
  public void constructorWithInstanceAndInheritedMethod() throws SecurityException, NoSuchMethodException {
    final Method waitMethod = TestUtils.extractMethod(Object.class, "wait");
    Binding b = new Binding(string, waitMethod);
    
    assertEquals(string, b.getInstance());
    assertEquals(waitMethod, b.getMethod());
  }
  
  @Test
  public void constructorWithIncompatibleInstanceAndMethod() throws SecurityException, NoSuchMethodException {
    String instance = "0123456789";
    Method method = TestUtils.extractMethod(ConverterType.class, "getFromType");
    try {
      new Binding(instance, method);
      fail();
    } catch(ObjectInstantiationException e) {
      assertEquals(Binding.class, e.getObjectType());
      assertEquals(instance, e.getArguments().get(0));
      assertEquals(method, e.getArguments().get(1));
      
      assertEquals(1, e.getCauses().size());
      assertEquals(MethodInstanceIncompatibilityException.class, e.getCauses().get(0).getClass());
      
      MethodInstanceIncompatibilityException ex = (MethodInstanceIncompatibilityException) e.getCauses().get(0);
      assertEquals(instance, ex.getInstance());
      assertEquals(method, ex.getMethod());
    }
  }
  
  @Test
  public void constructorWithNullInstanceAndNonStaticMethod() throws SecurityException, NoSuchMethodException {
    Method method = TestUtils.extractMethod(ConverterType.class, "getFromType");
    try {
      new Binding(null, method);
      fail();
    } catch(ObjectInstantiationException e) {
      assertEquals(Binding.class, e.getObjectType());
      assertNull(e.getArguments().get(0));
      assertEquals(method, e.getArguments().get(1));
      
      assertEquals(1, e.getCauses().size());
      assertEquals(NullInstanceWithNonStaticMethodException.class, e.getCauses().get(0).getClass());
      
      NullInstanceWithNonStaticMethodException ex = (NullInstanceWithNonStaticMethodException) e.getCauses().get(0);
      assertEquals(method, ex.getMethod());
    }
  }
  
  @Test
  public void constructorWithNonPublicMethod() throws SecurityException, NoSuchMethodException {
    final Method getRawType = TestUtils.extractDeclaredMethod(TypeToken.class, "getRawType", Type.class);

    try {
      new Binding(null, getRawType);
      fail();
    } catch(ObjectInstantiationException e) {
      assertEquals(Binding.class, e.getObjectType());
      assertNull(e.getArguments().get(0));
      assertEquals(getRawType, e.getArguments().get(1));
      
      InaccessibleMethodException ex = (InaccessibleMethodException) e.getCauses().get(0);
      assertEquals(getRawType, ex.getMethod());
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
    
    Binding binding = new Binding(inner, TestUtils.extractMethod(inner.getClass(), "stringify", Object.class));
    assertEquals("42", binding.invoke(42));
    assertEquals("sbrubbles", binding.invoke("sbrubbles"));
    assertEquals("true", binding.invoke(true));
    assertEquals("java.lang.Object -> java.lang.Object", binding.invoke(new ConverterType(Object.class, Object.class)));
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
      TestUtils.assertType(BindingInvocationException.class, e);
      assertEquals(substring, ((BindingInvocationException) e).getBinding());
      TestUtils.assertType(IllegalArgumentException.class, e.getCause());
    }
  }
  
  @Test
  public void invokeWithTargetException() {
    try {
      substring.invoke(-1, 9);
      fail();
    } catch(Exception e) {
      TestUtils.assertType(BindingInvocationException.class, e);
      assertEquals(substring, ((BindingInvocationException) e).getBinding());
      TestUtils.assertType(InvocationTargetException.class, e.getCause());
      TestUtils.assertType(StringIndexOutOfBoundsException.class, e.getCause().getCause());
    }
  }
  
  @Test
  public void getDeclaringType() throws SecurityException, NoSuchMethodException {
    assertEquals(String.class, substring.getInstanceClass());
    assertEquals(String.class, valueOf.getInstanceClass());
    
    final Method listEquals = TestUtils.extractMethod(List.class, "equals", Object.class);
    assertEquals(ArrayList.class, new Binding(new ArrayList<Object>(), listEquals).getInstanceClass());
    assertEquals(LinkedList.class, new Binding(new LinkedList<Object>(), listEquals).getInstanceClass());
  }
}
