package com.googlecode.transmuter;

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
import com.googlecode.transmuter.converter.Binding;
import com.googlecode.transmuter.converter.ConverterType;
import com.googlecode.transmuter.converter.exception.ConverterTypeIncompatibleWithBindingException;
import com.googlecode.transmuter.converter.exception.ConverterTypeInstantiationException;
import com.googlecode.transmuter.exception.ConverterCollisionException;
import com.googlecode.transmuter.mock.MultipleConverter;
import com.googlecode.transmuter.mock.MultipleValidConverter;
import com.googlecode.transmuter.mock.StringConverter;
import com.googlecode.transmuter.type.TypeToken;


public class ConverterMapTest {
  private static final TypeToken<List<String>> LIST_OF_STRING = new TypeToken<List<String>>() { /**/ };
  private static final TypeToken<ArrayList<String>> ARRAYLIST_OF_STRING = new TypeToken<ArrayList<String>>() { /**/ };
  
  private Transmuter t;
  private Map<ConverterType, Binding> map;

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
          new Binding(
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
    
    Map<ConverterType, Binding> temp = new HashMap<ConverterType, Binding>();
    temp.put(
        new ConverterType(TypeToken.OBJECT, TypeToken.STRING),
        new Binding(
            new StringConverter(),
            StringConverter.class.getMethod("stringify", Object.class)));
    temp.put(
        new ConverterType(double.class, String.class), 
        new Binding(
            multiple, 
            multiple.getClass().getMethod("converter", double.class)));
    temp.put(
        new ConverterType(TypeToken.STRING, LIST_OF_STRING), 
        new Binding(
            multiple, 
            multiple.getClass().getMethod("convert", String.class)));
    
    try {
      map.putAll(temp);
      fail();
    } catch(ConverterCollisionException e) { //  only the first exception
      // TODO no way of knowing which error comes first
      // think of a better way to handle this
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
      map.put(null, new Binding(o, o.getClass().getMethod("converter", double.class)));
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
    
    map.putAll(new HashMap<ConverterType, Binding>());
    
    assertTrue(map.isEmpty());
  }
  
  @Test
  public void redundantPut() throws SecurityException, NoSuchMethodException {
    assertFalse(t.isRegistered(Object.class, String.class));
    assertTrue(map.isEmpty());
    
    final StringConverter a = new StringConverter();
    final Method stringify = StringConverter.class.getMethod("stringify", Object.class);
    assertNull(map.put(new ConverterType(Object.class, String.class), new Binding(a, stringify)));
    
    assertTrue(t.isRegistered(Object.class, String.class));
    assertEquals(1, map.size());
    
    assertEquals(new Binding(a, stringify), map.put(new ConverterType(Object.class, String.class), new Binding(a, stringify)));
    
    assertTrue(t.isRegistered(Object.class, String.class));
    assertEquals(1, map.size());
  }
  
  @Test(expected = ConverterTypeIncompatibleWithBindingException.class)
  public void incompatibleConverterTypeAndBinding() throws SecurityException, NoSuchMethodException {
    map.put(
        new ConverterType(String.class, double.class), 
        new Binding(new StringConverter(), StringConverter.class.getMethod("stringify", Object.class)));
  }
  
  @Test(expected = ConverterTypeInstantiationException.class)
  public void bindingWithNoConverterType() throws SecurityException, NoSuchMethodException {
    map.put(
        new ConverterType(String.class, double.class), 
        new Binding("alksjdklajs", String.class.getMethod("codePointCount", int.class, int.class)));
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
    Binding stringify = new Binding(converter, StringConverter.class.getMethod("stringify", Object.class));
    Binding toString = new Binding(converter, StringConverter.class.getMethod("toString"));
    
    assertFalse(pbm.checkForCollision(converterType, stringify));
    assertFalse(pbm.checkForCollision(converterType, toString));
    
    pbm.put(converterType, stringify);
    
    assertTrue(pbm.checkForCollision(converterType, stringify));
    
    try {
      pbm.checkForCollision(converterType, toString);
      fail();
    } catch(ConverterCollisionException e) {
      assertEquals(converterType, e.getConverterType());
      assertTrue(e.getBindings().containsAll(Arrays.asList(stringify, toString)));
      assertTrue(Arrays.asList(stringify, toString).containsAll(e.getBindings()));
    }
    
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
    
    pbm.clear();
    
    assertFalse(pbm.checkForCollision(converterType, stringify));
    assertFalse(pbm.checkForCollision(converterType, toString));
    
    Map<ConverterType, Binding> noChecking = new HashMap<ConverterType, Binding>();
    noChecking.put(converterType, toString);
    
    assertFalse(ConverterMap.checkMapForCollision(converterType, toString, pbm));
    assertTrue(ConverterMap.checkMapForCollision(converterType, toString, noChecking));
    
    assertFalse(pbm.checkForCollision(converterType, stringify));
    
    try {
      ConverterMap.checkMapForCollision(converterType, stringify, noChecking);
      fail();
    } catch(ConverterCollisionException e) {
      assertEquals(converterType, e.getConverterType());
      assertTrue(Arrays.asList(stringify, toString).containsAll(e.getBindings()));
      assertTrue(e.getBindings().containsAll(Arrays.asList(stringify, toString)));
    }
  }
  

  
  @Test
  public void getMostCompatibleConverterFor() throws SecurityException, NoSuchMethodException {
    final MultipleValidConverter converter = new MultipleValidConverter();
    t.register(converter);
    
    assertEquals(
        map.get(new ConverterType(Serializable.class, String.class)),
        new Binding(
            converter, 
            extractMethod(converter.getClass(), "toString", Serializable.class)));
    assertEquals(
        map.get(new ConverterType(LIST_OF_STRING, TypeToken.STRING)),
        new Binding(
            converter, 
            extractMethod(converter.getClass(), "toString", List.class)));
    
    assertNull(map.get(new ConverterType(ARRAYLIST_OF_STRING, TypeToken.STRING)));
    assertNull(map.get(null));
    assertNull(map.get(new ConverterType(Object.class, Integer.class)));
  }
  
  private Method extractMethod(Class<?> cls, String name, Class<?>... parameterTypes) 
  throws SecurityException, NoSuchMethodException {
    return cls.getMethod(name, parameterTypes);
  }
}
