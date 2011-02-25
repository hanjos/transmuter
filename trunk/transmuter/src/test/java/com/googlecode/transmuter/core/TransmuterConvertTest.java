package com.googlecode.transmuter.core;

import static com.googlecode.transmuter.TestUtils.extractMethod;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.transmuter.TestUtils;
import com.googlecode.transmuter.converter.Converter;
import com.googlecode.transmuter.converter.ConverterType;
import com.googlecode.transmuter.converter.Converts;
import com.googlecode.transmuter.exception.NoCompatibleConvertersFoundException;
import com.googlecode.transmuter.exception.TooManyConvertersFoundException;
import com.googlecode.transmuter.fixture.MultipleConverter;
import com.googlecode.transmuter.fixture.MultipleValidConverter;
import com.googlecode.transmuter.fixture.StringConverter;
import com.googlecode.transmuter.type.TypeToken;

public class TransmuterConvertTest {
  private static final TypeToken<ArrayList<String>> ARRAYLIST_OF_STRING = new TypeToken<ArrayList<String>>() { /**/ };
  private static final TypeToken<List<String>> LIST_OF_STRING = new TypeToken<List<String>>() { /**/ };
  
  private Transmuter t;

  @Before
  public void setUp() {
    t = new Transmuter();
  }
  
  @Test
  public void convert() {
    t.register(new Converts.EagerProvider(new StringConverter()));
    
    assertEquals("sbrubbles", t.convert("sbrubbles", Object.class, String.class));
    assertEquals("sbrubbles", t.convert("sbrubbles", String.class, String.class));
    assertEquals("sbrubbles", t.convert("sbrubbles", String.class));
    assertEquals("1", t.convert(1, Object.class, String.class));
    assertEquals("1", t.convert(1, Integer.class, String.class));
    assertEquals("1", t.convert(1, String.class));
    
    t.register(new Converts.EagerProvider(new MultipleConverter()));
    
    assertArrayEquals(
        new Object[] { "sbrubbles" }, 
        t.convert("sbrubbles", TypeToken.STRING, LIST_OF_STRING).toArray());
    assertArrayEquals(
        new Object[] { "sbrubbles" }, 
        t.convert("sbrubbles", LIST_OF_STRING).toArray());
    assertEquals("double: 2.0", t.convert(2.0, Double.class, String.class));
    assertEquals("double: 2.0", t.convert(2.0, String.class));
    assertEquals("double: 2.0", t.convert(2.0, TypeToken.STRING));
    assertEquals("2.0", t.convert(2.0, Object.class, String.class));
  }
  
  @Test
  public void convertWithNullArguments() {
    t.register(new Converts.EagerProvider(new StringConverter()));
    
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
    t.register(new Converts.EagerProvider(parameterized));
    
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
    t.register(new Converts.EagerProvider(raw));
    
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
    t.register(new Converts.EagerProvider(converter));
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
}
