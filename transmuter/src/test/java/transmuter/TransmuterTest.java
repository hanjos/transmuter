package transmuter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static transmuter.util.ObjectUtils.*;

import java.util.ArrayList;
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
import transmuter.util.Binding;
import transmuter.util.Pair;

public class TransmuterTest {
  private Transmuter t;
  
  @Before
  public void setUp() {
    t = new Transmuter();
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
  public void registerFlawedConverter() {
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
    
    t.register(new Object() {
      @SuppressWarnings("unused") // just to shut up Eclipse's warnings
      @Converter
      public String test(double d) {
        return "double: " + d;
      }
    });
    
    assertEquals(1, t.getConverterMap().size());
    assertTrue(t.isRegistered(double.class, String.class));
    
    try {
      t.register(flawed);
      fail();
    } catch(ConverterRegistrationException e) {
      final List<? extends Exception> causes = e.getCauses();
      
      assertEquals(7, causes.size());
      assertWrongParameterCount(causes, new WrongParameterCountException(1, 0), 1);
      assertWrongParameterCount(causes, new WrongParameterCountException(1, 4), 1);
      assertWrongParameterCount(causes, new WrongParameterCountException(1, 2), 1);
      assertInvalidReturnType(causes, new InvalidReturnTypeException(void.class), 2);
      assertSameClassConverterCollision(
          causes, 
          new SameClassConverterCollisionException(flawed.getClass(), new Pair(int.class, boolean.class)),
          1);
      assertConverterCollision(
          causes, 
          new ConverterCollisionException(new Pair(double.class, String.class)), 
          1);
    }
    
    assertEquals(1, t.getConverterMap().size());
    assertTrue(t.isRegistered(double.class, String.class));
  }
  
  private void assertWrongParameterCount(final List<? extends Exception> causes, 
      WrongParameterCountException e, int expectedCount) {
    int count = 0;
    for(Exception cause : causes) {
      if(cause.getClass() != e.getClass())
        continue;
      
      WrongParameterCountException cause2 = (WrongParameterCountException) cause;
      if(cause2.getActual() == e.getActual()
      && cause2.getExpected() == e.getExpected())
        count++;
    }
    
    assertEquals(expectedCount, count);
  }
  
  private void assertInvalidReturnType(final List<? extends Exception> causes, 
      InvalidReturnTypeException e, int expectedCount) {
    int count = 0;
    for(Exception cause : causes) {
      if(cause.getClass() != e.getClass())
        continue;
      
      InvalidReturnTypeException cause2 = (InvalidReturnTypeException) cause;
      if(areEqual(cause2.getReturnType(), e.getReturnType()))
        count++;
    }
    
    assertEquals(expectedCount, count);
  }
  
  private void assertSameClassConverterCollision(final List<? extends Exception> causes, 
      SameClassConverterCollisionException e, int expectedCount) {
    int count = 0;
    for(Exception cause : causes) {
      if(cause.getClass() != e.getClass())
        continue;
      
      SameClassConverterCollisionException cause2 = (SameClassConverterCollisionException) cause;
      if(areEqual(cause2.getPair(), e.getPair())
      && areEqual(cause2.getDeclaringType(), e.getDeclaringType()))
        count++;
    }
    
    assertEquals(expectedCount, count);
  }
  
  private void assertConverterCollision(final List<? extends Exception> causes, 
      ConverterCollisionException e, int expectedCount) {
    int count = 0;
    for(Exception cause : causes) {
      if(cause.getClass() != e.getClass())
        continue;
      
      ConverterCollisionException cause2 = (ConverterCollisionException) cause;
      if(areEqual(cause2.getPair(), e.getPair()))
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
  
  @Test
  public void unregisterNullAndNonexistent() {
    t.register(new Object() {
      @SuppressWarnings("unused") // just to shut up Eclipse's warnings
      @Converter
      public String test(double d) {
        return "double: " + d;
      }
    });
    
    assertTrue(t.isRegistered(double.class, String.class));
    assertEquals(1, t.getConverterMap().size());
    
    t.unregister(null); // nothing happens
    
    assertTrue(t.isRegistered(double.class, String.class));
    assertEquals(1, t.getConverterMap().size());
    
    t.unregister(boolean.class, int.class); // nothing happens
    
    assertTrue(t.isRegistered(double.class, String.class));
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
    Map<Pair, Binding> map = t.getConverterMap();
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
    Map<Pair, Binding> map = t.getConverterMap();
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
}
