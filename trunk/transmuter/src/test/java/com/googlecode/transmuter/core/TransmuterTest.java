package com.googlecode.transmuter.core;

import static com.googlecode.transmuter.TestUtils.extractMethod;
import static com.googlecode.transmuter.type.TypeToken.ValueType.DOUBLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.transmuter.TestUtils;
import com.googlecode.transmuter.converter.Converter;
import com.googlecode.transmuter.converter.ConverterType;
import com.googlecode.transmuter.converter.Converts;
import com.googlecode.transmuter.core.exception.NoCompatibleConvertersFoundException;
import com.googlecode.transmuter.core.exception.TooManyConvertersFoundException;
import com.googlecode.transmuter.fixture.MultipleConverter;
import com.googlecode.transmuter.fixture.MultipleValidConverter;
import com.googlecode.transmuter.type.TypeToken;

public class TransmuterTest {
  private static final TypeToken<ArrayList<String>> ARRAYLIST_OF_STRING = new TypeToken<ArrayList<String>>() { /**/ };
  private static final TypeToken<List<String>> LIST_OF_STRING = new TypeToken<List<String>>() { /**/ };
  
  private Transmuter t;

  @Before
  public void setUp() {
    t = new Transmuter();
  }
  
  @Test
  public void registerAndIsRegisteredAndUnregister() {
    assertTrue(t.getConverterMap().isEmpty());
    assertFalse(t.isRegistered(double.class, String.class));
    assertFalse(t.isRegistered(TypeToken.STRING, LIST_OF_STRING));
    
    t.register(new Converts.EagerProvider(new MultipleConverter()));
    
    assertEquals(2, t.getConverterMap().size());
    assertTrue(t.isRegistered(double.class, String.class));
    assertTrue(t.isRegistered(TypeToken.STRING, LIST_OF_STRING));
    
    t.unregister(DOUBLE.primitive, TypeToken.STRING);
    
    assertEquals(1, t.getConverterMap().size());
    assertFalse(t.isRegistered(double.class, String.class));
    assertTrue(t.isRegistered(TypeToken.STRING, LIST_OF_STRING));
  }
  
  @Test
  public void getConverterFor() throws SecurityException, NoSuchMethodException {
    final MultipleValidConverter converter = new MultipleValidConverter();
    t.register(new Converts.EagerProvider(converter));
    
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
}
