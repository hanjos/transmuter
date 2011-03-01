package com.googlecode.transmuter.core;

import static com.googlecode.transmuter.TestUtils.assertInvalidReturnType;
import static com.googlecode.transmuter.TestUtils.assertWrongParameterCount;
import static com.googlecode.transmuter.TestUtils.extractMethod;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.transmuter.converter.Converter;
import com.googlecode.transmuter.converter.ConverterType;
import com.googlecode.transmuter.converter.Converts;
import com.googlecode.transmuter.core.exception.ConverterRegistrationException;
import com.googlecode.transmuter.fixture.FlawedConverter;
import com.googlecode.transmuter.fixture.MultipleConverter;
import com.googlecode.transmuter.fixture.StringConverter;
import com.googlecode.transmuter.fixture.VarargConverter;
import com.googlecode.transmuter.type.TypeToken;
import com.googlecode.transmuter.util.Notification;
import com.googlecode.transmuter.util.exception.NotificationNotFoundException;

public class TransmuterRegisterTest {
  private static final TypeToken<List<String>> LIST_OF_STRING = new TypeToken<List<String>>() { /**/ };
  
  private Transmuter t;
  
  @Before
  public void setUp() {
    t = new Transmuter();
  }
  
  @Test
  public void registerFlawedConverter() throws SecurityException, NoSuchMethodException {
    assertTrue(t.getConverterMap().isEmpty());
    assertFalse(t.isRegistered(double.class, String.class));
    assertFalse(t.isRegistered(TypeToken.STRING, LIST_OF_STRING));
    
    final Object working = new MultipleConverter();
    t.register(new Converts.EagerProvider(working));
    
    assertEquals(2, t.getConverterMap().size());
    assertTrue(t.isRegistered(double.class, String.class));
    assertTrue(t.isRegistered(TypeToken.STRING, LIST_OF_STRING));
    
    final Object flawed = new FlawedConverter();
    try {
      t.register(new Converts.LazyProvider(flawed));
      fail();
    } catch (ConverterRegistrationException e) {
      final Class<?> flawedClass = flawed.getClass();
      final Collection<? extends Exception> causes = e.getCauses();
      
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
          void.class, 1);
      assertInvalidReturnType(causes, 
          extractMethod(flawedClass, "voidAndTooManyParameters", int.class, int.class, int.class, int.class), 
          void.class, 1);
      
    }
    
    assertEquals(2, t.getConverterMap().size());
    assertTrue(t.isRegistered(double.class, String.class));
    assertTrue(t.isRegistered(TypeToken.STRING, LIST_OF_STRING));
  }
  
  @Test
  public void registerNullAndEmptyObject() {
    assertTrue(t.getConverterMap().isEmpty());
    
    t.register(null); // nothing happens
    
    assertTrue(t.getConverterMap().isEmpty());
    
    t.register(new Converts.EagerProvider(new Object())); // nothing happens
    
    assertTrue(t.getConverterMap().isEmpty());
  }
  
  @Test
  public void registerWithRedundantPut() {
    Map<ConverterType, Converter> map = t.getConverterMap();
    
    assertFalse(t.isRegistered(Object.class, String.class));
    assertFalse(t.isRegistered(TypeToken.OBJECT, TypeToken.STRING));
    assertTrue(map.isEmpty());
    
    t.register(new Converts.EagerProvider(new StringConverter()));
    
    assertTrue(t.isRegistered(Object.class, String.class));
    assertTrue(t.isRegistered(TypeToken.OBJECT, TypeToken.STRING));
    assertEquals(1, map.size());
    
    t.register(new Converts.EagerProvider(new StringConverter()));
    
    assertTrue(t.isRegistered(Object.class, String.class));
    assertTrue(t.isRegistered(TypeToken.OBJECT, TypeToken.STRING));
    assertEquals(1, map.size());
  }
  
  // XXX is this what should happen, or should an exception be thrown?
  @Test
  public void registerPrivate() {
    assertFalse(t.isRegistered(Object.class, String.class));
    
    t.register(new Converts.EagerProvider(new Object() {
      @SuppressWarnings("unused") // just to shut up Eclipse's warnings
      @Converts
      private String stringify(Object o) {
        return String.valueOf(o);
      }
    }));
    
    assertFalse(t.isRegistered(Object.class, String.class));
  }
  
  @Test
  public void registerPackagePrivate() {
    assertFalse(t.isRegistered(Object.class, String.class));
    
    t.register(new Converts.EagerProvider(new Object() {
      @SuppressWarnings("unused") // just to shut up Eclipse's warnings
      @Converts
      String stringify(Object o) {
        return String.valueOf(o);
      }
    }));
    
    assertFalse(t.isRegistered(Object.class, String.class));
  }
  
  @Test
  public void registerProtected() {
    assertFalse(t.isRegistered(Object.class, String.class));
    
    t.register(new Converts.EagerProvider(new Object() {
      @SuppressWarnings("unused") // just to shut up Eclipse's warnings
      @Converts
      protected String stringify(Object o) {
        return String.valueOf(o);
      }
    }));
    
    assertFalse(t.isRegistered(Object.class, String.class));
  }
  
  @Test
  public void registerInnerClass() {
    assertFalse(t.isRegistered(Object.class, String.class));
    
    t.register(new Converts.EagerProvider(new Object() {
      @SuppressWarnings("unused") // just to shut up Eclipse's warnings
      @Converts
      public String stringify(Object o) {
        return String.valueOf(o);
      }
    }));
    
    assertTrue(t.isRegistered(Object.class, String.class));
    
    assertEquals("42", t.convert(42, Integer.class, String.class));
    assertEquals("sbrubbles", t.convert("sbrubbles", String.class, String.class));
    assertEquals("true", t.convert(true, Object.class, String.class));
    assertEquals("java.lang.Object -> java.lang.Object", t.convert(new ConverterType(Object.class, Object.class), Object.class, String.class));
  }
  
  @Test
  public void registerVararg() {
    assertFalse(t.isRegistered(Object[].class, String.class));
    
    t.register(new Converts.EagerProvider(new VarargConverter()));
    
    assertTrue(t.isRegistered(Object[].class, String.class));
  }
  
  @Test
  public void faultyHasNext() {
    Iterable<Converter> faulty = new Iterable<Converter>() {
      @Override
      public Iterator<Converter> iterator() {
        return new Iterator<Converter>() {
          @Override
          public boolean hasNext() {
            throw new UnsupportedOperationException();
          }

          @Override
          public Converter next() { return null; }

          @Override
          public void remove() { /* empty block */ }
        };
      }
    };
    
    try {
      t.register(faulty);
    } catch (ConverterRegistrationException e) {
      Collection<? extends Exception> causes = e.getCauses();
      
      assertEquals(1, causes.size());
      
      Iterator<? extends Exception> iterator = causes.iterator();
      Exception first = iterator.next();
      
      assertEquals(UnsupportedOperationException.class, first.getClass());
    }
  }
  
  @Test
  public void tryRegisterReturningNull() {
    Transmuter t = new Transmuter() {
      @Override
      protected Notification tryRegister(Iterable<? extends Converter> converters) {
        return null;
      }
    };
    
    try {
      t.register(null);
    } catch (ConverterRegistrationException e) {
      Collection<? extends Exception> causes = e.getCauses();
      
      assertEquals(1, causes.size());
      
      Iterator<? extends Exception> iterator = causes.iterator();
      Exception first = iterator.next();
      
      assertEquals(NotificationNotFoundException.class, first.getClass());
    }
  }  
}
