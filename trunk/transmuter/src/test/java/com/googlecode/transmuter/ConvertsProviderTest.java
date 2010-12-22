package com.googlecode.transmuter;

import static com.googlecode.transmuter.TestUtils.assertMatchingCollections;
import static com.googlecode.transmuter.TestUtils.extractMethod;
import static com.googlecode.transmuter.util.CollectionUtils.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.googlecode.transmuter.converter.Converter;
import com.googlecode.transmuter.converter.exception.InvalidParameterTypeException;
import com.googlecode.transmuter.converter.exception.InvalidReturnTypeException;
import com.googlecode.transmuter.exception.ConverterProviderException;
import com.googlecode.transmuter.fixture.GenericConverter;
import com.googlecode.transmuter.fixture.GenericMethodConverter;
import com.googlecode.transmuter.fixture.MultipleConverter;
import com.googlecode.transmuter.fixture.PartialGenericConverter;
import com.googlecode.transmuter.fixture.VarargConverter;

public class ConvertsProviderTest {
  @Test
  public void simpleProvider() throws SecurityException, NoSuchMethodException {
    MultipleConverter object = new MultipleConverter();
    Converts.Provider provider = new Converts.Provider(object);
    
    Converter converter = new Converter(object, extractMethod(MultipleConverter.class, "converter", double.class));
    Converter convert = new Converter(object, extractMethod(MultipleConverter.class, "convert", String.class));
    
    assertMatchingCollections(Arrays.asList(converter, convert), toList(provider));
  }
  
  @Test
  public void nullProvider() {
    Converts.Provider provider = new Converts.Provider(null);
    
    assertMatchingCollections(Collections.emptyList(), toList(provider));
  }
  
  @Test
  public void noConvertsProvider() {
    Converts.Provider provider = new Converts.Provider(new Object());
    
    assertMatchingCollections(Collections.emptyList(), toList(provider));
  }
  
  @Test
  public void privateProvider() {
    Converts.Provider provider = new Converts.Provider(new Object() {
      @SuppressWarnings("unused") // just to shut up Eclipse's warnings
      @Converts
      private String stringify(Object o) {
        return String.valueOf(o);
      }
    });
    
    assertMatchingCollections(Collections.emptyList(), toList(provider));
  }
  
  @Test
  public void packagePrivateProvider() {
    Converts.Provider provider = new Converts.Provider(new Object() {
      @SuppressWarnings("unused") // just to shut up Eclipse's warnings
      @Converts
      String stringify(Object o) {
        return String.valueOf(o);
      }
    });
    
    assertMatchingCollections(Collections.emptyList(), toList(provider));
  }
  
  @Test
  public void protectedProvider() {
    Converts.Provider provider = new Converts.Provider(new Object() {
      @SuppressWarnings("unused") // just to shut up Eclipse's warnings
      @Converts
      protected String stringify(Object o) {
        return String.valueOf(o);
      }
    });
    
    assertMatchingCollections(Collections.emptyList(), toList(provider));
  }
  
  @Test
  public void varargProvider() throws SecurityException, NoSuchMethodException {
    VarargConverter object = new VarargConverter();
    Converts.Provider provider = new Converts.Provider(object);
    
    Converter stringifyArray = new Converter(object, VarargConverter.class.getMethod("stringifyArray", Object[].class));
    
    assertMatchingCollections(Arrays.asList(stringifyArray), toList(provider));
  }
  
  @Test
  public void genericMethodProvider() {
    try {
      new Converts.Provider(new GenericMethodConverter());
      fail();
    } catch(ConverterProviderException e) {
      assertEquals(2, e.getCauses().size());
      
      assertEquals(InvalidParameterTypeException.class, e.getCauses().get(0).getClass());
      assertTrue(((InvalidParameterTypeException) e.getCauses().get(0)).getType() instanceof TypeVariable<?>);
      
      assertEquals(InvalidReturnTypeException.class, e.getCauses().get(1).getClass());
      assertTrue(((InvalidReturnTypeException) e.getCauses().get(1)).getType() instanceof TypeVariable<?>);
    }
  }
  
  @Test
  public void partialGenericClassProvider() throws SecurityException, NoSuchMethodException {
    try {
      new Converts.Provider(new PartialGenericConverter<List<String>>()); // no way of getting this info with Java
      fail();
    } catch(ConverterProviderException e) {
      assertEquals(1, e.getCauses().size());
      assertEquals(InvalidParameterTypeException.class, e.getCauses().get(0).getClass());
      
      InvalidParameterTypeException ex = (InvalidParameterTypeException) e.getCauses().get(0);
      assertEquals(extractMethod(PartialGenericConverter.class, "convert", Object.class), ex.getMethod());
      assertTrue(ex.getType() instanceof TypeVariable<?>);
      assertEquals("From", ((TypeVariable<?>) ex.getType()).getName());
    }
    
    try {
      new Converts.Provider(new PartialGenericConverter<Object>()); // raw
      fail();
    } catch(ConverterProviderException e) {
      assertEquals(1, e.getCauses().size());
      assertEquals(InvalidParameterTypeException.class, e.getCauses().get(0).getClass());
      final Method convertMethod = extractMethod(PartialGenericConverter.class, "convert", Object.class);
      
      InvalidParameterTypeException ex = (InvalidParameterTypeException) e.getCauses().get(0);
      assertEquals(convertMethod, ex.getMethod());
      assertTrue(ex.getType() instanceof TypeVariable<?>);
      assertEquals("From", ((TypeVariable<?>) ex.getType()).getName());
    }
  }
  
  @Test
  public void registerMethodFromGenericClass() throws SecurityException, NoSuchMethodException {
    try {
      new Converts.Provider(new GenericConverter<List<String>, String>()); // no way of getting this info with Java
      fail();
    } catch(ConverterProviderException e) {
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
    
    try {
      new Converts.Provider(new GenericConverter<Object, Object>()); // raw
      fail();
    } catch(ConverterProviderException e) {
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
  }
}
