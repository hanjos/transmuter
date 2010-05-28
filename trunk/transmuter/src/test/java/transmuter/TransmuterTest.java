package transmuter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static transmuter.util.ObjectUtils.areEqual;

import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import transmuter.exception.ConverterCollisionException;
import transmuter.exception.ConverterRegistrationException;
import transmuter.exception.InvalidReturnTypeException;
import transmuter.exception.SameClassConverterCollisionException;
import transmuter.exception.WrongParameterCountException;
import transmuter.type.TypeToken;
import transmuter.type.exception.UnexpectedTypeException;
import transmuter.util.Binding;
import transmuter.util.Pair;

public class TransmuterTest {
  private Transmuter t;
  private Map<Pair, Binding> map;

  private static class A {
    @SuppressWarnings("unused") // just to shut up Eclipse's warnings
    @Converter
    public String stringify(Object object) {
      return String.valueOf(object);
    }
  }
  
  @Before
  public void setUp() {
    t = new Transmuter();
    map = t.getConverterMap();
  }
  
  @Test
  public void registerAndIsRegisteredAndUnregister() {
    assertTrue(t.getConverterMap().isEmpty());
    assertFalse(t.isRegistered(int.class, boolean.class));
    
    t.register(new Object() {
      @SuppressWarnings("unused") // just to shut up Eclipse's warnings
      @Converter
      public boolean toBoolean(int i) {
        return (i == 0) ? false : true;
      }
    });
    
    assertEquals(1, t.getConverterMap().size());
    assertTrue(t.isRegistered(int.class, boolean.class));
    
    t.unregister(int.class, boolean.class);
    
    assertTrue(t.getConverterMap().isEmpty());
    assertFalse(t.isRegistered(int.class, boolean.class));
  }
  
  @Test
  public void registerFlawedConverter() throws SecurityException, NoSuchMethodException {
    @SuppressWarnings("unused") // just to shut up Eclipse's warnings
    Object flawed = new Object() {
      @Converter
      public boolean intraClassCollision1(int i) {
        return (i == 0) ? false : true;
      }
      
      @Converter
      public boolean intraClassCollision2(int i) {
        return i % 2 == 0;
      }
      
      @Converter
      public void voidAsReturnType(Object whatever) {
        // empty block
      }
      
      @Converter
      public Object tooManyParameters(Object a, Object b) {
        return null;
      }
      
      @Converter
      public Object tooFewParameters() {
        return null;
      }
      
      @Converter
      public String extraClassCollision(double d) {
        return String.valueOf(d);
      }
      
      @Converter
      public void voidAndTooManyParameters(int a, int b, int c, int d) {
        // empty block
      }
    };
    
    assertTrue(t.getConverterMap().isEmpty());
    assertFalse(t.isRegistered(double.class, String.class));
    
    final Object working = new Object() {
      @SuppressWarnings("unused") // just to shut up Eclipse's warnings
      @Converter
      public String test(double d) {
        return "double: " + d;
      }
    };
    t.register(working);
    
    assertEquals(1, t.getConverterMap().size());
    assertTrue(t.isRegistered(double.class, String.class));
    
    try {
      t.register(flawed);
      fail();
    } catch(ConverterRegistrationException e) {
      final List<? extends Exception> causes = e.getCauses();
      final Class<?> flawedClass = flawed.getClass();
      final TypeToken<Void> voidClass = TypeToken.get(void.class);
      
      assertEquals(7, causes.size());
      
      assertWrongParameterCount(causes,
          extractMethod(flawedClass, "tooManyParameters", Object.class, Object.class),
          1, 1);
      assertWrongParameterCount(causes, 
          extractMethod(flawedClass, "tooFewParameters"),
          1, 1);
      assertWrongParameterCount(causes, 
          extractMethod(flawedClass, "voidAndTooManyParameters", int.class, int.class, int.class, int.class),
          1, 1);
      assertInvalidReturnType(causes, 
          extractMethod(flawedClass, "voidAsReturnType", Object.class), 
          voidClass, 1);
      assertInvalidReturnType(causes, 
          extractMethod(flawedClass, "voidAndTooManyParameters", int.class, int.class, int.class, int.class), 
          voidClass, 1);
      assertSameClassConverterCollision(causes, 
          TypeToken.get(flawedClass), 
          new Pair(int.class, boolean.class), 
          Arrays.asList(
              extractMethod(flawedClass, "intraClassCollision1", int.class),
              extractMethod(flawedClass, "intraClassCollision2", int.class)),
          1);
      assertConverterCollision(causes, 
          new Pair(double.class, String.class), 
          Arrays.asList(
              extractMethod(flawedClass, "extraClassCollision", double.class),
              extractMethod(working.getClass(), "test", double.class)),
          1);
    }
    
    assertEquals(1, t.getConverterMap().size());
    assertTrue(t.isRegistered(double.class, String.class));
  }
  
  private Method extractMethod(Class<?> cls, String name, Class<?>... parameterTypes) 
  throws SecurityException, NoSuchMethodException {
    return cls.getMethod(name, parameterTypes);
  }
  
  private void assertWrongParameterCount(final List<? extends Exception> causes, 
      Method method, int expected, int expectedCount) {
    int count = 0;
    for(Exception cause : causes) {
      if(cause.getClass() != WrongParameterCountException.class)
        continue;
      
      WrongParameterCountException cause2 = (WrongParameterCountException) cause;
      if(areEqual(cause2.getMethod(), method)
      && cause2.getActual() == method.getParameterTypes().length
      && cause2.getExpected() == expected)
        count++;
    }
    
    assertEquals(expectedCount, count);
  }
  
  private void assertInvalidReturnType(final List<? extends Exception> causes, 
      Method method, TypeToken<?> returnType, int expectedCount) {
    int count = 0;
    for(Exception cause : causes) {
      if(cause.getClass() != InvalidReturnTypeException.class)
        continue;
      
      InvalidReturnTypeException cause2 = (InvalidReturnTypeException) cause;
      if(areEqual(cause2.getMethod(), method)
      && areEqual(cause2.getReturnType(), returnType))
        count++;
    }
    
    assertEquals(expectedCount, count);
  }
  
  private void assertSameClassConverterCollision(final List<? extends Exception> causes, 
      TypeToken<?> declaringType, Pair pair, List<Method> methods, int expectedCount) {
    int count = 0;
    for(Exception cause : causes) {
      if(cause.getClass() != SameClassConverterCollisionException.class)
        continue;
      
      SameClassConverterCollisionException cause2 = (SameClassConverterCollisionException) cause;
      if(areEqual(cause2.getPair(), pair)
      && (cause2.getMethods().containsAll(methods) && methods.containsAll(cause2.getMethods()))
      && areEqual(cause2.getDeclaringType(), declaringType))
        count++;
    }
    
    assertEquals(expectedCount, count);
  }
  
  private void assertConverterCollision(final List<? extends Exception> causes, 
      Pair pair, List<Method> methods, int expectedCount) {
    int count = 0;
    for(Exception cause : causes) {
      if(cause.getClass() != ConverterCollisionException.class)
        continue;
      
      ConverterCollisionException cause2 = (ConverterCollisionException) cause;
      if(areEqual(cause2.getPair(), pair)
      && (cause2.getMethods().containsAll(methods) && methods.containsAll(cause2.getMethods())))
        count++;
    }
    
    assertEquals(expectedCount, count);
  }

  @Test
  public void registerNullAndEmptyObject() {
    assertTrue(t.getConverterMap().isEmpty());
    
    t.register(null); // nothing happens
    
    assertTrue(t.getConverterMap().isEmpty());
    
    t.register(new Object()); // nothing happens
    
    assertTrue(t.getConverterMap().isEmpty());
  }
  
  // TODO is this what should happen, or should an exception be thrown?
  @Test
  public void registerPrivate() {
    assertFalse(t.isRegistered(Object.class, String.class));
    
    t.register(new Object() {
      @SuppressWarnings("unused") // just to shut up Eclipse's warnings
      @Converter
      private String stringify(Object o) {
        return String.valueOf(o);
      }
    });
    
    assertFalse(t.isRegistered(Object.class, String.class));
  }
  
  @Test
  public void registerPackagePrivate() {
    assertFalse(t.isRegistered(Object.class, String.class));
    
    t.register(new Object() {
      @SuppressWarnings("unused") // just to shut up Eclipse's warnings
      @Converter
      String stringify(Object o) {
        return String.valueOf(o);
      }
    });
    
    assertFalse(t.isRegistered(Object.class, String.class));
  }
  
  @Test
  public void registerProtected() {
    assertFalse(t.isRegistered(Object.class, String.class));
    
    t.register(new Object() {
      @SuppressWarnings("unused") // just to shut up Eclipse's warnings
      @Converter
      protected String stringify(Object o) {
        return String.valueOf(o);
      }
    });
    
    assertFalse(t.isRegistered(Object.class, String.class));
  }
  
  @Test
  public void registerVararg() {
    assertFalse(t.isRegistered(Object[].class, String.class));
    
    t.register(new Object() {
      @SuppressWarnings("unused") // just to shut up Eclipse's warnings
      @Converter
      public String stringifyArray(Object... o) {
        return String.valueOf(o);
      }
    });
    
    assertTrue(t.isRegistered(Object[].class, String.class));
  }
  
  @Test
  public void registerGenericType() {
    assertTrue(t.getConverterMap().isEmpty());
    
    try {
      t.register(new Object() {
        @SuppressWarnings("unused") // just to shut up Eclipse's warnings
        @Converter
        public <T> T nonNull(T o) {
          if(o == null)
            throw new IllegalArgumentException();
          
          return o;
        }
      });
    } catch(ConverterRegistrationException e) {
      assertEquals(2, e.getCauses().size());
      
      assertEquals(UnexpectedTypeException.class, e.getCauses().get(0).getClass());
      assertTrue(((UnexpectedTypeException) e.getCauses().get(0)).getType() instanceof TypeVariable);
      
      assertEquals(UnexpectedTypeException.class, e.getCauses().get(1).getClass());
      assertTrue(((UnexpectedTypeException) e.getCauses().get(1)).getType() instanceof TypeVariable);
      // TODO go into details
    }
    
    assertTrue(t.getConverterMap().isEmpty());
  }
  
  @Test
  public void unregisterNullAndNonexistent() {
    t.register(new Object() {
      @SuppressWarnings("unused") // just to shut up Eclipse's warnings
      @Converter
      public String test(Object... d) {
        return "double: " + d;
      }
    });
    
    assertTrue(t.isRegistered(Object[].class, String.class));
    assertEquals(1, t.getConverterMap().size());
    
    t.unregister(null); // nothing happens
    
    assertTrue(t.isRegistered(Object[].class, String.class));
    assertEquals(1, t.getConverterMap().size());
    
    t.unregister(boolean.class, int.class); // doesn't exist, nothing happens
    
    assertTrue(t.isRegistered(Object[].class, String.class));
    assertEquals(1, t.getConverterMap().size());
  }
  
  @Test
  public void isRegistered() {
    assertTrue(t.getConverterMap().isEmpty());
    
    t.register(new Object() {
      @SuppressWarnings("unused") // just to shut up Eclipse's warnings
      @Converter
      public String converter(double d) {
        return "double: " + d;
      }
      
      @SuppressWarnings({ "unused", "serial" }) // just to shut up Eclipse's warnings
      @Converter
      public List<String> convert(final String s) {
        return new ArrayList<String>() {{ add(s); }};
      }
    });
    
    assertTrue(t.isRegistered(new Pair(double.class, String.class)));
    assertTrue(t.isRegistered(TypeToken.STRING, new TypeToken<List<String>>() {}));
    assertFalse(t.isRegistered(String.class, List.class));
  }
  
  @Test
  public void converterMap() throws SecurityException, NoSuchMethodException {
    assertTrue(map.isEmpty());
    
    t.register(new Object() {
      @SuppressWarnings("unused") // just to shut up Eclipse's warnings
      @Converter
      public String converter(double d) {
        return "double: " + d;
      }
      
      @SuppressWarnings({ "unused", "serial" }) // just to shut up Eclipse's warnings
      @Converter
      public List<String> convert(final String s) {
        return new ArrayList<String>() {{ add(s); }};
      }
    });
    
    assertEquals(2, map.size());
    assertTrue(map.containsKey(new Pair(double.class, String.class)));
    assertTrue(map.containsKey(new Pair(TypeToken.STRING, new TypeToken<List<String>>() {})));
    assertFalse(map.containsKey(new Pair(String.class, List.class)));
    
    Object double2string = new Object() {
      @SuppressWarnings("unused") // just to shut up Eclipse's warnings
      @Converter
      public String test(double d) {
        return "double: " + d;
      }
    };
    
    Object string2ListOfString = new Object() {
      @SuppressWarnings({ "unused", "serial" }) // just to shut up Eclipse's warnings
      @Converter
      public List<String> convert(final String s) {
        return new ArrayList<String>() {{ add(s); }};
      }
    };
    
    try {
      map.put(
          new Pair(double.class, String.class), 
          new Binding(
              double2string, 
              double2string.getClass().getMethod("test", double.class)));
      fail();
    } catch(ConverterCollisionException e) {
      assertEquals(new Pair(double.class, String.class), e.getPair());
    }
    
    Map<Pair, Binding> temp = new HashMap<Pair, Binding>();
    temp.put(
        new Pair(double.class, String.class), 
        new Binding(
            double2string, 
            double2string.getClass().getMethod("test", double.class)));
    temp.put(
        new Pair(TypeToken.STRING, new TypeToken<List<String>>() {}), 
        new Binding(
            string2ListOfString, 
            string2ListOfString.getClass().getMethod("convert", String.class)));
    
    try {
      map.putAll(temp);
      fail();
    } catch(ConverterCollisionException e) { //  only the first exception
      // TODO no way of knowing which error comes first, what to do?
    }
  }
  
  @Test
  public void converterMapWithNulls() throws SecurityException, NoSuchMethodException {
    Object o = new Object() {
      @SuppressWarnings("unused") // just to shut up Eclipse's warnings
      @Converter
      public String test(double d) {
        return "double: " + d;
      }
    };
    
    try {
      map.put(null, new Binding(o, o.getClass().getMethod("test", double.class)));
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
    
    assertTrue(map.isEmpty());
    
    try {
      map.put(new Pair(double.class, String.class), null);
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
    
    assertTrue(map.isEmpty());
    
    map.putAll(null);
    
    assertTrue(map.isEmpty());
    
    map.putAll(new HashMap<Pair, Binding>());
    
    assertTrue(map.isEmpty());
  }
  
  @Test
  public void converterMapWithRedundantPut() throws SecurityException, NoSuchMethodException {
    assertFalse(t.isRegistered(Object.class, String.class));
    assertTrue(map.isEmpty());
    
    final A a = new A();
    final Method stringify = A.class.getMethod("stringify", Object.class);
    map.put(new Pair(Object.class, String.class), new Binding(a, stringify));
    
    assertTrue(t.isRegistered(Object.class, String.class));
    assertEquals(1, map.size());
    
    map.put(new Pair(Object.class, String.class), new Binding(a, stringify));
    
    assertTrue(t.isRegistered(Object.class, String.class));
    assertEquals(1, map.size());
  }
}
