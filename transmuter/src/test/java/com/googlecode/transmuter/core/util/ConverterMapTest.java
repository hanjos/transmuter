package com.googlecode.transmuter.core.util;

import static com.googlecode.transmuter.TestUtils.extractMethod;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
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

import com.googlecode.transmuter.converter.Converter;
import com.googlecode.transmuter.converter.ConverterType;
import com.googlecode.transmuter.converter.Converts;
import com.googlecode.transmuter.core.util.ConverterMap;
import com.googlecode.transmuter.exception.ConverterCollisionException;
import com.googlecode.transmuter.fixture.MultipleValidConverter;
import com.googlecode.transmuter.fixture.StringConverter;
import com.googlecode.transmuter.type.TypeToken;

public class ConverterMapTest {
  private static final TypeToken<List<String>> LIST_OF_STRING = new TypeToken<List<String>>() { /**/ };
  private static final TypeToken<ArrayList<String>> ARRAYLIST_OF_STRING = new TypeToken<ArrayList<String>>() { /**/ };
  
  private ConverterMap map;

  @Before
  public void setUp() {
    map = new ConverterMap();
  }
  
  @Test
  public void checkForCollision() throws SecurityException, NoSuchMethodException {
    StringConverter converter = new StringConverter();
    ConverterType converterType = new ConverterType(Object.class, String.class);
    Converter stringify = new Converter(converter, StringConverter.class.getMethod("stringify", Object.class));
    Converter equals = new Converter(converter, StringConverter.class.getMethod("equals", Object.class));
    
    // ensuring there's no previous mapping...
    assertFalse(map.checkForCollision(converterType, stringify));
    assertFalse(map.checkForCollision(converterType, equals));
    
    // putting stringify
    map.put(converterType, stringify);
    
    // showing that stringify is now in
    assertTrue(map.checkForCollision(converterType, stringify));
    
    // checking equals will now explode
    try {
      map.checkForCollision(converterType, equals);
      fail();
    } catch(ConverterCollisionException e) {
      assertEquals(converterType, e.getConverterType());
      assertTrue(e.getConverters().containsAll(Arrays.asList(stringify, equals)));
      assertTrue(Arrays.asList(stringify, equals).containsAll(e.getConverters()));
    }
    
    // nobody likes null
    try {
      map.checkForCollision(null, stringify);
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
    
    try {
      map.checkForCollision(converterType, null);
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
    
    try {
      ConverterMap.checkMapForCollision(converterType, stringify, null);
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
    
    // cleaning up the house
    map.clear();
    
    assertFalse(map.checkForCollision(converterType, stringify));
    assertFalse(map.checkForCollision(converterType, equals));
    
    // putting equals in a competitor
    Map<ConverterType, Converter> noChecking = new HashMap<ConverterType, Converter>();
    noChecking.put(converterType, equals);
    
    assertFalse(ConverterMap.checkMapForCollision(converterType, equals, map));
    assertTrue(ConverterMap.checkMapForCollision(converterType, equals, noChecking));
    
    assertFalse(map.checkForCollision(converterType, stringify));
    
    try {
      ConverterMap.checkMapForCollision(converterType, stringify, noChecking);
      fail();
    } catch(ConverterCollisionException e) {
      assertEquals(converterType, e.getConverterType());
      assertTrue(Arrays.asList(stringify, equals).containsAll(e.getConverters()));
      assertTrue(e.getConverters().containsAll(Arrays.asList(stringify, equals)));
    }
  }
  
  @Test
  public void getMostCompatibleConverterFor() throws SecurityException, NoSuchMethodException {
    final MultipleValidConverter converter = new MultipleValidConverter();
    
    for(Converter c : new Converts.EagerProvider(converter))
      map.put(c.getType(), c);
    
    assertEquals(
        map.get(new ConverterType(Serializable.class, String.class)),
        new Converter(
            converter, 
            extractMethod(converter.getClass(), "toString", Serializable.class)));
    assertEquals(
        map.get(new ConverterType(LIST_OF_STRING, TypeToken.STRING)),
        new Converter(
            converter, 
            extractMethod(converter.getClass(), "toString", List.class)));
    
    assertNull(map.get(new ConverterType(ARRAYLIST_OF_STRING, TypeToken.STRING)));
    assertNull(map.get(null));
    assertNull(map.get(new ConverterType(Object.class, Integer.class)));
  }
}
