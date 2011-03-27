package com.googlecode.transmuter.core;

import static com.googlecode.transmuter.TestUtils.extractMethod;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.transmuter.converter.Converter;
import com.googlecode.transmuter.converter.ConverterType;
import com.googlecode.transmuter.core.exception.NoCompatibleConvertersFoundException;
import com.googlecode.transmuter.core.exception.TooManyConvertersFoundException;
import com.googlecode.transmuter.fixture.StringConverter;
import com.googlecode.transmuter.type.TypeToken;
import com.googlecode.transmuter.util.CollectionUtils;
import com.googlecode.transmuter.util.exception.ObjectInstantiationException;

public class BasicConverterSelectorTest {
  private BasicConverterSelector selector;
  private StringConverter source;
  
  private ConverterType unknownType;
  private ConverterType compatibleType;
  
  private Converter stringify;
  private Converter equals;
  
  
  @Before
  public void setUp() throws ObjectInstantiationException, SecurityException, NoSuchMethodException {
    selector = new BasicConverterSelector();
    source = new StringConverter();
    
    unknownType = new ConverterType(TypeToken.STRING, TypeToken.ValueType.INTEGER.wrapper);
    compatibleType = new ConverterType(TypeToken.STRING, TypeToken.STRING);
    
    stringify = new Converter(source, extractMethod(StringConverter.class, "stringify", Object.class));
    equals = new Converter(source, extractMethod(StringConverter.class, "equals", Object.class));
  }
  
  @Test
  public void getConverterForIterableWithNullsIn() {
    basicGetConverterForIterable(Arrays.asList(stringify, null, equals, null, null));
  }
  
  @Test
  public void getConverterForIterable() {
    basicGetConverterForIterable(Arrays.asList(stringify, equals));
  }

  @Test
  public void getConverterForIterableWithCompatible() {
    final List<Converter> stringifyAndEquals = Arrays.asList(stringify, equals);
    
    assertEquals(
        stringify, 
        selector.getConverterFor(compatibleType, stringifyAndEquals));
  }
  
  private void basicGetConverterForIterable(final Iterable<Converter> stringifyAndEquals) {
    assertEquals(
        stringify, 
        selector.getConverterFor(stringify.getType(), stringifyAndEquals));
    
    assertEquals(
        equals, 
        selector.getConverterFor(equals.getType(), stringifyAndEquals));
  }
  
  @Test
  public void getConverterForIterableNoCompatibleConverters() {
    final List<Converter> stringifyAndEquals = Arrays.asList(stringify, equals);
    
    try {
      selector.getConverterFor(unknownType, stringifyAndEquals);
      fail();
    } catch (NoCompatibleConvertersFoundException e) {
      assertArrayEquals(
          stringifyAndEquals.toArray(),
          CollectionUtils.toList(e.getConverters()).toArray());
      
      assertEquals(unknownType, e.getConverterType());
    }
  }
  
  @Test
  public void getConverterForIterableTooManyCompatibleConverters() {
    final List<Converter> stringifyRepeatedAndEquals = Arrays.asList(stringify, equals, stringify);
    
    try {
      selector.getConverterFor(compatibleType, stringifyRepeatedAndEquals);
      fail();
    } catch (TooManyConvertersFoundException e) {
      assertArrayEquals(
          new Object[] { stringify, stringify },
          CollectionUtils.toList(e.getConverters()).toArray());
      
      assertEquals(compatibleType, e.getConverterType());
    }
  }
  
  @Test
  public void getConverterForIterableNoTypeGiven() {
    final List<Converter> stringifyAndEquals = Arrays.asList(stringify, equals);
    
    try {
      selector.getConverterFor(null, stringifyAndEquals);
      fail();
    } catch (NoCompatibleConvertersFoundException e) {
      assertArrayEquals(
          stringifyAndEquals.toArray(),
          CollectionUtils.toList(e.getConverters()).toArray());
      
      assertNull(e.getConverterType());
    }
  }
  
  @Test
  public void getConverterForIterableNullConverterListGiven() {
    try {
      selector.getConverterFor(compatibleType, (Iterable<? extends Converter>) null);
      fail();
    } catch (NoCompatibleConvertersFoundException e) {
      assertNull(e.getConverters());
      assertEquals(compatibleType, e.getConverterType());
    }
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void getConverterForIterableEmptyConverterListGiven() {
    try {
      selector.getConverterFor(compatibleType, Collections.EMPTY_LIST);
      fail();
    } catch (NoCompatibleConvertersFoundException e) {
      assertTrue(CollectionUtils.toList(e.getConverters()).isEmpty());
      assertEquals(compatibleType, e.getConverterType());
    }
  }
}
