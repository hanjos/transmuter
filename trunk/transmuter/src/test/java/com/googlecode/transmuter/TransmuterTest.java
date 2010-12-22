package com.googlecode.transmuter;

import static com.googlecode.transmuter.TestUtils.extractMethod;
import static com.googlecode.transmuter.type.TypeToken.ValueType.DOUBLE;
import static com.googlecode.transmuter.util.ObjectUtils.areEqual;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.transmuter.converter.Converter;
import com.googlecode.transmuter.converter.ConverterType;
import com.googlecode.transmuter.converter.exception.InvalidParameterTypeException;
import com.googlecode.transmuter.converter.exception.InvalidReturnTypeException;
import com.googlecode.transmuter.converter.exception.WrongParameterCountException;
import com.googlecode.transmuter.exception.ConverterCollisionException;
import com.googlecode.transmuter.exception.ConverterRegistrationException;
import com.googlecode.transmuter.exception.NoCompatibleConvertersFoundException;
import com.googlecode.transmuter.exception.TooManyConvertersFoundException;
import com.googlecode.transmuter.fixture.FlawedConverter;
import com.googlecode.transmuter.fixture.GenericConverter;
import com.googlecode.transmuter.fixture.GenericMethodConverter;
import com.googlecode.transmuter.fixture.MultipleConverter;
import com.googlecode.transmuter.fixture.MultipleValidConverter;
import com.googlecode.transmuter.fixture.PartialGenericConverter;
import com.googlecode.transmuter.fixture.StringArrayToListStringConverter;
import com.googlecode.transmuter.fixture.StringConverter;
import com.googlecode.transmuter.fixture.VarargConverter;
import com.googlecode.transmuter.type.TypeToken;


public class TransmuterTest {
  private static final TypeToken<ArrayList<String>> ARRAYLIST_OF_STRING = new TypeToken<ArrayList<String>>() { /**/ };
  private static final TypeToken<List<String>> LIST_OF_STRING = new TypeToken<List<String>>() { /**/ };
  private Transmuter t;
  private Map<ConverterType, Converter> map;

  @Before
  public void setUp() {
    t = new Transmuter();
    map = t.getConverterMap();
  }
  
  @Test
  public void registerAndIsRegisteredAndUnregister() {
    assertTrue(t.getConverterMap().isEmpty());
    assertFalse(t.isRegistered(double.class, String.class));
    assertFalse(t.isRegistered(TypeToken.STRING, LIST_OF_STRING));
    
    t.register(new MultipleConverter());
    
    assertEquals(2, t.getConverterMap().size());
    assertTrue(t.isRegistered(double.class, String.class));
    assertTrue(t.isRegistered(TypeToken.STRING, LIST_OF_STRING));
    
    t.unregister(DOUBLE.primitive, TypeToken.STRING);
    
    assertEquals(1, t.getConverterMap().size());
    assertFalse(t.isRegistered(double.class, String.class));
    assertTrue(t.isRegistered(TypeToken.STRING, LIST_OF_STRING));
  }
  
  @Test
  public void registerFlawedConverter() throws SecurityException, NoSuchMethodException {
    Object flawed = new FlawedConverter();
    
    assertTrue(t.getConverterMap().isEmpty());
    assertFalse(t.isRegistered(double.class, String.class));
    assertFalse(t.isRegistered(TypeToken.STRING, LIST_OF_STRING));
    
    final Object working = new MultipleConverter();
    t.register(working);
    
    assertEquals(2, t.getConverterMap().size());
    assertTrue(t.isRegistered(double.class, String.class));
    assertTrue(t.isRegistered(TypeToken.STRING, LIST_OF_STRING));
    
    try {
      t.register(flawed);
      fail();
    } catch(ConverterRegistrationException e) {
      final List<? extends Exception> causes = e.getCauses();
      final Class<?> flawedClass = flawed.getClass();
      
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
          void.class, 1);
      assertInvalidReturnType(causes, 
          extractMethod(flawedClass, "voidAndTooManyParameters", int.class, int.class, int.class, int.class), 
          void.class, 1);
      assertConverterCollision(causes, 
          new ConverterType(int.class, boolean.class), 
          Arrays.asList(
              new Converter(flawed, extractMethod(flawedClass, "intraClassCollision1", int.class)),
              new Converter(flawed, extractMethod(flawedClass, "intraClassCollision2", int.class))),
          1);
      assertConverterCollision(causes, 
          new ConverterType(double.class, String.class), 
          Arrays.asList(
              new Converter(flawed, extractMethod(flawedClass, "extraClassCollision", double.class)),
              new Converter(working, extractMethod(working.getClass(), "converter", double.class))),
          1);
    }
    
    assertEquals(2, t.getConverterMap().size());
    assertTrue(t.isRegistered(double.class, String.class));
    assertTrue(t.isRegistered(TypeToken.STRING, LIST_OF_STRING));
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
      Method method, Type returnType, int expectedCount) {
    int count = 0;
    for(Exception cause : causes) {
      if(cause.getClass() != InvalidReturnTypeException.class)
        continue;
      
      InvalidReturnTypeException cause2 = (InvalidReturnTypeException) cause;
      if(areEqual(cause2.getMethod(), method)
      && areEqual(cause2.getType(), returnType))
        count++;
    }
    
    assertEquals(expectedCount, count);
  }
  
  private void assertConverterCollision(final List<? extends Exception> causes, 
      ConverterType converterType, List<Converter> converters, int expectedCount) {
    int count = 0;
    for(Exception cause : causes) {
      if(cause.getClass() != ConverterCollisionException.class)
        continue;
      
      ConverterCollisionException cause2 = (ConverterCollisionException) cause;
      if(areEqual(cause2.getConverterType(), converterType)
      && (cause2.getConverters().containsAll(converters) && converters.containsAll(cause2.getConverters())))
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
  public void registerWithRedundantPut() {
    assertFalse(t.isRegistered(Object.class, String.class));
    assertFalse(t.isRegistered(TypeToken.OBJECT, TypeToken.STRING));
    assertTrue(map.isEmpty());
    
    t.register(new StringConverter());
    
    assertTrue(t.isRegistered(Object.class, String.class));
    assertTrue(t.isRegistered(TypeToken.OBJECT, TypeToken.STRING));
    assertEquals(1, map.size());
    
    t.register(new StringConverter());
    
    assertTrue(t.isRegistered(Object.class, String.class));
    assertTrue(t.isRegistered(TypeToken.OBJECT, TypeToken.STRING));
    assertEquals(1, map.size());
  }
  
  // XXX is this what should happen, or should an exception be thrown?
  @Test
  public void registerPrivate() {
    assertFalse(t.isRegistered(Object.class, String.class));
    
    t.register(new Object() {
      @SuppressWarnings("unused") // just to shut up Eclipse's warnings
      @Converts
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
      @Converts
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
      @Converts
      protected String stringify(Object o) {
        return String.valueOf(o);
      }
    });
    
    assertFalse(t.isRegistered(Object.class, String.class));
  }
  
  @Test
  public void registerInnerClass() {
    assertFalse(t.isRegistered(Object.class, String.class));
    
    t.register(new Object() {
      @SuppressWarnings("unused") // just to shut up Eclipse's warnings
      @Converts
      public String stringify(Object o) {
        return String.valueOf(o);
      }
    });
    
    assertTrue(t.isRegistered(Object.class, String.class));
    
    assertEquals("42", t.convert(42, Integer.class, String.class));
    assertEquals("sbrubbles", t.convert("sbrubbles", String.class, String.class));
    assertEquals("true", t.convert(true, Object.class, String.class));
    assertEquals("java.lang.Object -> java.lang.Object", t.convert(new ConverterType(Object.class, Object.class), Object.class, String.class));
  }
  
  @Test
  public void registerVararg() {
    assertFalse(t.isRegistered(Object[].class, String.class));
    
    t.register(new VarargConverter());
    
    assertTrue(t.isRegistered(Object[].class, String.class));
  }
  
  @Test
  public void registerGenericMethod() {
    assertTrue(t.getConverterMap().isEmpty());
    
    try {
      t.register(new GenericMethodConverter());
      fail();
    } catch(ConverterRegistrationException e) {
      assertEquals(2, e.getCauses().size());
      
      assertEquals(InvalidParameterTypeException.class, e.getCauses().get(0).getClass());
      assertTrue(((InvalidParameterTypeException) e.getCauses().get(0)).getType() instanceof TypeVariable<?>);
      
      assertEquals(InvalidReturnTypeException.class, e.getCauses().get(1).getClass());
      assertTrue(((InvalidReturnTypeException) e.getCauses().get(1)).getType() instanceof TypeVariable<?>);
    }
    
    assertTrue(t.getConverterMap().isEmpty());
  }
  
  @Test
  public void registerMethodFromPartialGenericClass() throws SecurityException, NoSuchMethodException {
    assertFalse(t.isRegistered(LIST_OF_STRING, TypeToken.STRING));
    
    try {
      t.register(new PartialGenericConverter<List<String>>()); // no way of getting this information with Java
      fail();
    } catch(ConverterRegistrationException e) {
      assertEquals(1, e.getCauses().size());
      assertEquals(InvalidParameterTypeException.class, e.getCauses().get(0).getClass());
      
      InvalidParameterTypeException ex = (InvalidParameterTypeException) e.getCauses().get(0);
      assertEquals(extractMethod(PartialGenericConverter.class, "convert", Object.class), ex.getMethod());
      assertTrue(ex.getType() instanceof TypeVariable<?>);
      assertEquals("From", ((TypeVariable<?>) ex.getType()).getName());
    }
    
    assertFalse(t.isRegistered(LIST_OF_STRING, TypeToken.STRING));
    
    try {
      t.register(new PartialGenericConverter<Object>()); // raw
      fail();
    } catch(ConverterRegistrationException e) {
      assertEquals(1, e.getCauses().size());
      assertEquals(InvalidParameterTypeException.class, e.getCauses().get(0).getClass());
      final Method convertMethod = extractMethod(PartialGenericConverter.class, "convert", Object.class);
      
      InvalidParameterTypeException ex = (InvalidParameterTypeException) e.getCauses().get(0);
      assertEquals(convertMethod, ex.getMethod());
      assertTrue(ex.getType() instanceof TypeVariable<?>);
      assertEquals("From", ((TypeVariable<?>) ex.getType()).getName());
    }
    
    assertFalse(t.isRegistered(LIST_OF_STRING, TypeToken.STRING));
  }
  
  @Test
  public void registerMethodFromGenericClass() throws SecurityException, NoSuchMethodException {
    assertFalse(t.isRegistered(LIST_OF_STRING, TypeToken.STRING));
    
    try {
      t.register(new GenericConverter<List<String>, String>()); // no way of getting this information with Java
      fail();
    } catch(ConverterRegistrationException e) {
      assertEquals(2, e.getCauses().size());
      assertEquals(InvalidParameterTypeException.class, e.getCauses().get(0).getClass());
      assertEquals(InvalidReturnTypeException.class, e.getCauses().get(1).getClass());
      
      InvalidParameterTypeException ex = (InvalidParameterTypeException) e.getCauses().get(0);
      assertEquals(extractMethod(GenericConverter.class, "convert", Object.class), ex.getMethod());
      assertTrue(ex.getType() instanceof TypeVariable<?>);
      assertEquals("From", ((TypeVariable<?>) ex.getType()).getName());
      
      InvalidReturnTypeException ex1 = (InvalidReturnTypeException) e.getCauses().get(1);
      assertEquals(extractMethod(GenericConverter.class, "convert", Object.class), ex1.getMethod());
      assertTrue(ex1.getType() instanceof TypeVariable<?>);
      assertEquals("To", ((TypeVariable<?>) ex1.getType()).getName());
    }
    
    assertFalse(t.isRegistered(LIST_OF_STRING, TypeToken.STRING));
    
    try {
      t.register(new GenericConverter<Object, Object>()); // raw
      fail();
    } catch(ConverterRegistrationException e) {
      assertEquals(2, e.getCauses().size());
      assertEquals(InvalidParameterTypeException.class, e.getCauses().get(0).getClass());
      assertEquals(InvalidReturnTypeException.class, e.getCauses().get(1).getClass());
      
      InvalidParameterTypeException ex = (InvalidParameterTypeException) e.getCauses().get(0);
      assertEquals(extractMethod(GenericConverter.class, "convert", Object.class), ex.getMethod());
      assertTrue(ex.getType() instanceof TypeVariable<?>);
      assertEquals("From", ((TypeVariable<?>) ex.getType()).getName());
      
      InvalidReturnTypeException ex1 = (InvalidReturnTypeException) e.getCauses().get(1);
      assertEquals(extractMethod(GenericConverter.class, "convert", Object.class), ex1.getMethod());
      assertTrue(ex1.getType() instanceof TypeVariable<?>);
      assertEquals("To", ((TypeVariable<?>) ex1.getType()).getName());
    }
    
    assertFalse(t.isRegistered(LIST_OF_STRING, TypeToken.STRING));
  }
  
  @Test
  public void unregisterNullAndNonexistent() {
    t.register(new VarargConverter());
    
    assertTrue(t.isRegistered(Object[].class, String.class));
    assertEquals(1, t.getConverterMap().size());
    
    t.unregister(null); // nothing happens
    
    assertTrue(t.isRegistered(Object[].class, String.class));
    assertEquals(1, t.getConverterMap().size());
    
    t.unregister(boolean.class, int.class); // doesn't exist, nothing happens
    
    assertTrue(t.isRegistered(Object[].class, String.class));
    assertEquals(1, t.getConverterMap().size());
    
    t.unregister(null, String.class); // nothing happens
    
    assertTrue(t.isRegistered(Object[].class, String.class));
    assertEquals(1, t.getConverterMap().size());
    
    t.unregister(TypeToken.STRING, TypeToken.get(void.class)); // nothing happens
    
    assertTrue(t.isRegistered(Object[].class, String.class));
    assertEquals(1, t.getConverterMap().size());
  }
  
  @Test
  public void isRegistered() {
    assertTrue(t.getConverterMap().isEmpty());
    
    t.register(new MultipleConverter());
    
    assertTrue(t.isRegistered(new ConverterType(double.class, String.class)));
    assertTrue(t.isRegistered(TypeToken.STRING, LIST_OF_STRING));
    assertFalse(t.isRegistered(String.class, List.class));
  }
  
  @Test
  public void convert() {
    t.register(new StringConverter());
    
    assertEquals("sbrubbles", t.convert("sbrubbles", Object.class, String.class));
    assertEquals("sbrubbles", t.convert("sbrubbles", String.class, String.class));
    assertEquals("sbrubbles", t.convert("sbrubbles", String.class));
    assertEquals("1", t.convert(1, Object.class, String.class));
    assertEquals("1", t.convert(1, Integer.class, String.class));
    assertEquals("1", t.convert(1, String.class));
    
    t.register(new MultipleConverter());
    
    assertArrayEquals(new Object[] { "sbrubbles" }, t.convert("sbrubbles", TypeToken.STRING, LIST_OF_STRING).toArray());
    assertArrayEquals(new Object[] { "sbrubbles" }, t.convert("sbrubbles", LIST_OF_STRING).toArray());
    assertEquals("double: 2.0", t.convert(2.0, Double.class, String.class));
    assertEquals("double: 2.0", t.convert(2.0, String.class));
    assertEquals("double: 2.0", t.convert(2.0, TypeToken.STRING));
    assertEquals("2.0", t.convert(2.0, Object.class, String.class));
  }
  
  @Test
  public void convertWithNullArguments() {
    t.register(new StringConverter());
    
    assertEquals("null", t.convert(null, Object.class, String.class));
    
    try {
      t.convert(null, String.class);
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
    
    try {
      t.convert(null, TypeToken.STRING);
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
    
    try {
      t.convert(new Object(), (Class<Object>) null);
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
    
    try {
      t.convert(new Object(), (TypeToken<Object>) null);
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
  }
  
  @Test
  public void convertWithParameterizedType() throws SecurityException, NoSuchMethodException {
    final Object parameterized = new Object() {
      @SuppressWarnings("unused") // just to make Eclipse happy
      @Converts
      public int size(Map<String, String> map) {
        return map.size();
      }
      
      @SuppressWarnings("unused") // just to make Eclipse happy
      @Converts
      public int size2(Map<ConverterType, Converter> map) {
        return map.size();
      }
    };
    t.register(parameterized);
    
    final TypeToken<Map<String, String>> MAP_STRING_TO_STRING = new TypeToken<Map<String, String>>() { /**/ };
    final TypeToken<Map<ConverterType, Converter>> MAP_CONVERTERTYPE_TO_CONVERTER = 
      new TypeToken<Map<ConverterType, Converter>>() { /**/ };
    final TypeToken<Integer> INT = TypeToken.get(int.class);
    
    assertTrue(0 == t.convert(new HashMap<String, String>(), MAP_STRING_TO_STRING, INT));
    assertTrue(2 == t.convert(t.getConverterMap(), MAP_CONVERTERTYPE_TO_CONVERTER, INT));
    
    try {
      t.convert(new HashMap<String, String>(), INT);
      fail();
    } catch(NoCompatibleConvertersFoundException e) {
      assertEquals(new ConverterType(HashMap.class, int.class), e.getConverterType());
    }
    
    try {
      t.convert(new HashMap<ConverterType, Converter>(), INT);
      fail();
    } catch(NoCompatibleConvertersFoundException e) {
      assertEquals(new ConverterType(HashMap.class, int.class), e.getConverterType());
    }
    
    final Object raw = new Object() {
      @SuppressWarnings({ "unused" })
      @Converts
      public int size(@SuppressWarnings("rawtypes") Map map) {
        return map.size();
      }
    };
    t.register(raw);
    
    assertTrue(0 == t.convert(new HashMap<String, String>(), INT));
    
    try {
      t.convert(t.getConverterMap(), INT);
      fail();
    } catch(TooManyConvertersFoundException e) {
      assertEquals(new ConverterType(t.getConverterMap().getClass(), int.class), e.getConverterType());
      TestUtils.assertMatchingCollections(
          e.getConverters(),
          Arrays.asList(
              new Converter(parameterized, parameterized.getClass().getDeclaredMethod("size2", Map.class)),
              new Converter(raw, raw.getClass().getDeclaredMethod("size", Map.class))));
    }
  }

  @Test
  public void convertUnknown() {
    try {
      t.convert("sbrubbles", Object.class, String.class);
      fail();
    } catch(NoCompatibleConvertersFoundException e) {
      assertEquals(new ConverterType(Object.class, String.class), e.getConverterType());
    }
  }
  
  @Test
  public void multipleValidConverters() throws SecurityException, NoSuchMethodException {
    final MultipleValidConverter converter = new MultipleValidConverter();
    t.register(converter);
    try {
      t.convert(new ArrayList<String>(), ARRAYLIST_OF_STRING, TypeToken.STRING);
      fail();
    } catch(TooManyConvertersFoundException e) {
      assertEquals(new ConverterType(ARRAYLIST_OF_STRING, TypeToken.STRING), e.getConverterType());
      assertEquals(2, e.getConverters().size());
    
      assertTrue(e.getConverters().contains(
          new Converter(
              converter, 
              extractMethod(converter.getClass(), "toString", List.class))));
      assertTrue(e.getConverters().contains(
          new Converter(
              converter, 
              extractMethod(converter.getClass(), "toString", Serializable.class))));
    }
  }
  
  @Test
  public void getCompatibleConvertersFor() throws SecurityException, NoSuchMethodException {
    final MultipleValidConverter converter = new MultipleValidConverter();
    t.register(converter);
    
    TestUtils.assertMatchingCollections(
        t.getCompatibleConvertersFor(new ConverterType(ARRAYLIST_OF_STRING, TypeToken.STRING)),
        Arrays.asList(
            new Converter(
                converter, 
                extractMethod(converter.getClass(), "toString", List.class)),
            new Converter(
                converter, 
                extractMethod(converter.getClass(), "toString", Serializable.class))));
    
    TestUtils.assertMatchingCollections(
        t.getCompatibleConvertersFor(new ConverterType(Serializable.class, String.class)),
        Arrays.asList(
            t.getConverterFor(new ConverterType(Serializable.class, String.class))));
    
    assertTrue(t.getCompatibleConvertersFor(null).isEmpty());
    assertTrue(t.getCompatibleConvertersFor(new ConverterType(Object.class, Integer.class)).isEmpty());
  }
  
  @Test
  public void getConverterFor() throws SecurityException, NoSuchMethodException {
    final MultipleValidConverter converter = new MultipleValidConverter();
    t.register(converter);
    
    assertEquals(
        t.getConverterFor(new ConverterType(Serializable.class, String.class)),
        new Converter(
          converter, 
          extractMethod(converter.getClass(), "toString", Serializable.class)));
    assertEquals(
        t.getConverterFor(new ConverterType(LIST_OF_STRING, TypeToken.STRING)),
        new Converter(
          converter, 
          extractMethod(converter.getClass(), "toString", List.class)));
    
    try {
      t.getConverterFor(null);
      fail();
    } catch(NoCompatibleConvertersFoundException e) {
      assertNull(e.getConverterType());
    }
    
    try {
      assertNull(t.getConverterFor(new ConverterType(Object.class, Integer.class)));
      fail();
    } catch(NoCompatibleConvertersFoundException e) {
      assertEquals(new ConverterType(Object.class, Integer.class), e.getConverterType());
    }
    
    try {
      t.getConverterFor(new ConverterType(ARRAYLIST_OF_STRING, TypeToken.STRING));
      fail();
    } catch(TooManyConvertersFoundException e) {
      assertEquals(new ConverterType(ARRAYLIST_OF_STRING, TypeToken.STRING), e.getConverterType());
      assertEquals(2, e.getConverters().size());
    
      TestUtils.assertMatchingCollections(
          e.getConverters(),
          Arrays.asList(
            new Converter(
                converter, 
                extractMethod(converter.getClass(), "toString", List.class)),
            new Converter(
                converter, 
                extractMethod(converter.getClass(), "toString", Serializable.class))));
    }
  }
  
  @Test
  public void inheritedGenericMethod() {
    TypeToken<String[]> ARRAY_OF_STRING = TypeToken.get(String[].class);
    
    assertFalse(t.isRegistered(ARRAY_OF_STRING, LIST_OF_STRING));
    
    t.register(new StringArrayToListStringConverter());
    
    assertTrue(t.isRegistered(ARRAY_OF_STRING, LIST_OF_STRING));
  }
}
