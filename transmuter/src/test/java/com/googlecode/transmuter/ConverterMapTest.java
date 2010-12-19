package com.googlecode.transmuter;

import static com.googlecode.transmuter.TestUtils.extractMethod;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.transmuter.Transmuter.ConverterMap;
import com.googlecode.transmuter.converter.Converter;
import com.googlecode.transmuter.converter.ConverterType;
import com.googlecode.transmuter.converter.exception.ConverterTypeIncompatibleWithConverterException;
import com.googlecode.transmuter.exception.ConverterCollisionException;
import com.googlecode.transmuter.fixture.MultipleConverter;
import com.googlecode.transmuter.fixture.MultipleValidConverter;
import com.googlecode.transmuter.fixture.StringConverter;
import com.googlecode.transmuter.type.TypeToken;
import com.googlecode.transmuter.util.exception.ObjectInstantiationException;

public class ConverterMapTest {
  private static final TypeToken<List<String>> LIST_OF_STRING = new TypeToken<List<String>>() { /**/ };
  private static final TypeToken<ArrayList<String>> ARRAYLIST_OF_STRING = new TypeToken<ArrayList<String>>() { /**/ };
  
  private Transmuter t;
  private Map<ConverterType, Converter> map;

  @Before
  public void setUp() {
    t = new Transmuter();
    map = t.getConverterMap();
  }
  
  @Test
  public void converterMap() throws SecurityException, NoSuchMethodException {
    assertTrue(map.isEmpty());
    
    t.register(new MultipleConverter());
    
    assertEquals(2, map.size());
    assertTrue(map.containsKey(new ConverterType(double.class, String.class)));
    assertTrue(map.containsKey(new ConverterType(TypeToken.STRING, LIST_OF_STRING)));
    assertFalse(map.containsKey(new ConverterType(String.class, List.class)));
    
    Object multiple = new MultipleConverter();
    
    try {
      map.put(
          new ConverterType(double.class, String.class), 
          new Converter(
              multiple, 
              multiple.getClass().getMethod("converter", double.class)));
      fail();
    } catch(ConverterCollisionException e) {
      assertEquals(new ConverterType(double.class, String.class), e.getConverterType());
    }
    
    
  }
  
  @Test
  public void putAll() throws SecurityException, NoSuchMethodException {
    t.register(new MultipleConverter());
    
    assertTrue(map.containsKey(new ConverterType(double.class, String.class)));
    assertTrue(map.containsKey(new ConverterType(TypeToken.STRING, LIST_OF_STRING)));
    assertFalse(map.containsKey(new ConverterType(TypeToken.OBJECT, TypeToken.STRING)));
    
    Object multiple = new MultipleConverter();
    
    Map<ConverterType, Converter> temp = new HashMap<ConverterType, Converter>();
    temp.put(
        new ConverterType(TypeToken.OBJECT, TypeToken.STRING),
        new Converter(
            new StringConverter(),
            StringConverter.class.getMethod("stringify", Object.class)));
    temp.put(
        new ConverterType(double.class, String.class), 
        new Converter(
            multiple, 
            multiple.getClass().getMethod("converter", double.class)));
    temp.put(
        new ConverterType(TypeToken.STRING, LIST_OF_STRING), 
        new Converter(
            multiple, 
            multiple.getClass().getMethod("convert", String.class)));
    
    try {
      map.putAll(temp);
      fail();
    } catch(ConverterCollisionException e) { //  only the first exception
      // no way of knowing which error comes first
      // TODO think of a better way to handle this
    }
    
    assertEquals(2, map.size());
    assertTrue(map.containsKey(new ConverterType(double.class, String.class)));
    assertTrue(map.containsKey(new ConverterType(TypeToken.STRING, LIST_OF_STRING)));
    assertFalse(map.containsKey(new ConverterType(TypeToken.OBJECT, TypeToken.STRING)));
  }
  
  @Test
  public void nulls() throws SecurityException, NoSuchMethodException {
    Object o = new MultipleConverter();
    
    try {
      map.put(null, new Converter(o, o.getClass().getMethod("converter", double.class)));
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
    
    assertTrue(map.isEmpty());
    
    try {
      map.put(new ConverterType(double.class, String.class), null);
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
    
    assertTrue(map.isEmpty());
    
    map.putAll(null);
    
    assertTrue(map.isEmpty());
    
    map.putAll(new HashMap<ConverterType, Converter>());
    
    assertTrue(map.isEmpty());
  }
  
  @Test
  public void redundantPut() throws SecurityException, NoSuchMethodException {
    assertFalse(t.isRegistered(Object.class, String.class));
    assertTrue(map.isEmpty());
    
    final StringConverter a = new StringConverter();
    final Method stringify = StringConverter.class.getMethod("stringify", Object.class);
    assertNull(map.put(new ConverterType(Object.class, String.class), new Converter(a, stringify)));
    
    assertTrue(t.isRegistered(Object.class, String.class));
    assertEquals(1, map.size());
    
    assertEquals(new Converter(a, stringify), map.put(new ConverterType(Object.class, String.class), new Converter(a, stringify)));
    
    assertTrue(t.isRegistered(Object.class, String.class));
    assertEquals(1, map.size());
  }
  
  @Test(expected = ConverterTypeIncompatibleWithConverterException.class)
  public void incompatibleConverterTypeAndConverter() throws SecurityException, NoSuchMethodException {
    map.put(
        new ConverterType(String.class, double.class), 
        new Converter(new StringConverter(), StringConverter.class.getMethod("stringify", Object.class)));
  }
  
  @Test
  public void converterWithNoConverterType() throws SecurityException, NoSuchMethodException {
    String instance = "alksjdklajs";
    Method method = String.class.getMethod("codePointCount", int.class, int.class);
    
    try {
      map.put(
          new ConverterType(String.class, double.class), 
          new Converter(instance, method));
      fail();
    } catch (ObjectInstantiationException e) {
      assertEquals(Converter.class, e.getObjectType());
      assertEquals(instance, e.getArguments().get(0));
      assertEquals(method, e.getArguments().get(1));
      
      // TODO proper causes analysis
    }
  }
  
  @Test
  public void containsNullKey() {
    assertFalse(map.containsKey(null));
  }
  
  @Test
  public void containsKeyWithPrimitives() {
    t.register(new MultipleConverter());
    
    assertTrue(map.containsKey(new ConverterType(double.class, String.class)));
    assertTrue(map.containsKey(new ConverterType(Double.class, String.class)));
  }
  
  @Test
  public void checkForCollision() throws SecurityException, NoSuchMethodException {
    ConverterMap pbm = (ConverterMap) map;
    
    StringConverter converter = new StringConverter();
    ConverterType converterType = new ConverterType(Object.class, String.class);
    Converter stringify = new Converter(converter, StringConverter.class.getMethod("stringify", Object.class));
    Converter equals = new Converter(converter, StringConverter.class.getMethod("equals", Object.class));
    
    // ensuring there's no previous mapping...
    assertFalse(pbm.checkForCollision(converterType, stringify));
    assertFalse(pbm.checkForCollision(converterType, equals));
    
    // putting stringify
    pbm.put(converterType, stringify);
    
    // showing that stringify is now in
    assertTrue(pbm.checkForCollision(converterType, stringify));
    
    // checking equals will now explode
    try {
      pbm.checkForCollision(converterType, equals);
      fail();
    } catch(ConverterCollisionException e) {
      assertEquals(converterType, e.getConverterType());
      assertTrue(e.getConverters().containsAll(Arrays.asList(stringify, equals)));
      assertTrue(Arrays.asList(stringify, equals).containsAll(e.getConverters()));
    }
    
    // nobody likes null
    try {
      pbm.checkForCollision(null, stringify);
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
    
    try {
      pbm.checkForCollision(converterType, null);
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
    pbm.clear();
    
    assertFalse(pbm.checkForCollision(converterType, stringify));
    assertFalse(pbm.checkForCollision(converterType, equals));
    
    // putting equals in a competitor
    Map<ConverterType, Converter> noChecking = new HashMap<ConverterType, Converter>();
    noChecking.put(converterType, equals);
    
    assertFalse(ConverterMap.checkMapForCollision(converterType, equals, pbm));
    assertTrue(ConverterMap.checkMapForCollision(converterType, equals, noChecking));
    
    assertFalse(pbm.checkForCollision(converterType, stringify));
    
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
    t.register(converter);
    
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
