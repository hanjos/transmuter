package com.googlecode.transmuter;

import static com.googlecode.transmuter.TestUtils.assertInvalidReturnType;
import static com.googlecode.transmuter.TestUtils.assertMatchingCollections;
import static com.googlecode.transmuter.TestUtils.assertWrongParameterCount;
import static com.googlecode.transmuter.TestUtils.extractMethod;
import static com.googlecode.transmuter.util.CollectionUtils.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Test;

import com.googlecode.transmuter.converter.Converter;
import com.googlecode.transmuter.converter.exception.InvalidParameterTypeException;
import com.googlecode.transmuter.converter.exception.InvalidReturnTypeException;
import com.googlecode.transmuter.exception.ConverterProviderException;
import com.googlecode.transmuter.fixture.FlawedConverter;
import com.googlecode.transmuter.fixture.GenericConverter;
import com.googlecode.transmuter.fixture.GenericMethodConverter;
import com.googlecode.transmuter.fixture.MultipleConverter;
import com.googlecode.transmuter.fixture.PartialGenericConverter;
import com.googlecode.transmuter.fixture.VarargConverter;

public class EagerProviderTest {
  @Test
  public void simpleProvider() throws SecurityException, NoSuchMethodException {
    MultipleConverter object = new MultipleConverter();
    Converts.EagerProvider provider = new Converts.EagerProvider(object);
    
    Converter converter = new Converter(object, extractMethod(MultipleConverter.class, "converter", double.class));
    Converter convert = new Converter(object, extractMethod(MultipleConverter.class, "convert", String.class));
    
    assertMatchingCollections(Arrays.asList(converter, convert), toList(provider));
  }
  
  @Test
  public void nullProvider() {
    Converts.EagerProvider provider = new Converts.EagerProvider(null);
    
    assertMatchingCollections(Collections.emptyList(), toList(provider));
  }
  
  @Test
  public void noConvertsProvider() {
    Converts.EagerProvider provider = new Converts.EagerProvider(new Object());
    
    assertMatchingCollections(Collections.emptyList(), toList(provider));
  }
  
  @Test
  public void privateProvider() {
    Converts.EagerProvider provider = new Converts.EagerProvider(new Object() {
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
    Converts.EagerProvider provider = new Converts.EagerProvider(new Object() {
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
    Converts.EagerProvider provider = new Converts.EagerProvider(new Object() {
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
    Converts.EagerProvider provider = new Converts.EagerProvider(object);
    
    Converter stringifyArray = new Converter(object, VarargConverter.class.getMethod("stringifyArray", Object[].class));
    
    assertMatchingCollections(Arrays.asList(stringifyArray), toList(provider));
  }
  
  @Test
  public void genericMethodProvider() {
    try {
      new Converts.EagerProvider(new GenericMethodConverter());
      fail();
    } catch(ConverterProviderException e) {
      assertEquals(2, e.getCauses().size());
      
      Iterator<? extends Exception> iterator = e.getCauses().iterator();
      Exception first = iterator.next();
      Exception second = iterator.next();
      assertEquals(InvalidParameterTypeException.class, first.getClass());
      assertTrue(((InvalidParameterTypeException) first).getType() instanceof TypeVariable<?>);
      
      assertEquals(InvalidReturnTypeException.class, second.getClass());
      assertTrue(((InvalidReturnTypeException) second).getType() instanceof TypeVariable<?>);
    }
  }
  
  @Test
  public void partialGenericClassProvider() throws SecurityException, NoSuchMethodException {
    try {
      new Converts.EagerProvider(new PartialGenericConverter<List<String>>()); // no way of getting this info with Java
      fail();
    } catch(ConverterProviderException e) {
      assertEquals(1, e.getCauses().size());
      
      Exception first = e.getCauses().iterator().next();
      assertEquals(InvalidParameterTypeException.class, first.getClass());
      
      InvalidParameterTypeException ex = (InvalidParameterTypeException) first;
      assertEquals(extractMethod(PartialGenericConverter.class, "convert", Object.class), ex.getMethod());
      assertTrue(ex.getType() instanceof TypeVariable<?>);
      assertEquals("From", ((TypeVariable<?>) ex.getType()).getName());
    }
    
    try {
      new Converts.EagerProvider(new PartialGenericConverter<Object>()); // raw
      fail();
    } catch(ConverterProviderException e) {
      assertEquals(1, e.getCauses().size());
      
      Exception first = e.getCauses().iterator().next();
      assertEquals(InvalidParameterTypeException.class, first.getClass());
      final Method convertMethod = extractMethod(PartialGenericConverter.class, "convert", Object.class);
      
      InvalidParameterTypeException ex = (InvalidParameterTypeException) first;
      assertEquals(convertMethod, ex.getMethod());
      assertTrue(ex.getType() instanceof TypeVariable<?>);
      assertEquals("From", ((TypeVariable<?>) ex.getType()).getName());
    }
  }
  
  @Test
  public void methodFromGenericClass() throws SecurityException, NoSuchMethodException {
    try {
      new Converts.EagerProvider(new GenericConverter<List<String>, String>()); // no way of getting this info with Java
      fail();
    } catch(ConverterProviderException e) {
      assertEquals(2, e.getCauses().size());
      
      Iterator<? extends Exception> iterator = e.getCauses().iterator();
      Exception first = iterator.next();
      Exception second = iterator.next();
      
      assertEquals(InvalidParameterTypeException.class, first.getClass());
      assertEquals(InvalidReturnTypeException.class, second.getClass());
      
      InvalidParameterTypeException ex = (InvalidParameterTypeException) first;
      assertEquals(extractMethod(GenericConverter.class, "convert", Object.class), ex.getMethod());
      assertTrue(ex.getType() instanceof TypeVariable<?>);
      assertEquals("From", ((TypeVariable<?>) ex.getType()).getName());
      
      InvalidReturnTypeException ex1 = (InvalidReturnTypeException) second;
      assertEquals(extractMethod(GenericConverter.class, "convert", Object.class), ex1.getMethod());
      assertTrue(ex1.getType() instanceof TypeVariable<?>);
      assertEquals("To", ((TypeVariable<?>) ex1.getType()).getName());
    }
    
    try {
      new Converts.EagerProvider(new GenericConverter<Object, Object>()); // raw
      fail();
    } catch(ConverterProviderException e) {
      assertEquals(2, e.getCauses().size());
      
      Iterator<? extends Exception> iterator = e.getCauses().iterator();
      Exception first = iterator.next();
      Exception second = iterator.next();
      
      assertEquals(InvalidParameterTypeException.class, first.getClass());
      assertEquals(InvalidReturnTypeException.class, second.getClass());
      
      InvalidParameterTypeException ex = (InvalidParameterTypeException) first;
      assertEquals(extractMethod(GenericConverter.class, "convert", Object.class), ex.getMethod());
      assertTrue(ex.getType() instanceof TypeVariable<?>);
      assertEquals("From", ((TypeVariable<?>) ex.getType()).getName());
      
      InvalidReturnTypeException ex1 = (InvalidReturnTypeException) second;
      assertEquals(extractMethod(GenericConverter.class, "convert", Object.class), ex1.getMethod());
      assertTrue(ex1.getType() instanceof TypeVariable<?>);
      assertEquals("To", ((TypeVariable<?>) ex1.getType()).getName());
    }
  }
  
  @Test
  public void flawedProvider() throws SecurityException, NoSuchMethodException {
    Object flawed = new FlawedConverter();
    
    try {
      new Converts.EagerProvider(flawed);
      fail();
    } catch(ConverterProviderException e) {
      final Collection<? extends Exception> causes = e.getCauses();
      final Class<?> flawedClass = flawed.getClass();
      
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
      
      assertEquals(5, causes.size());
    }
  }
  
  @Test
  public void immutableProvider() throws SecurityException, NoSuchMethodException {
    VarargConverter object = new VarargConverter();
    Converts.EagerProvider provider = new Converts.EagerProvider(object);
    
    Converter stringifyArray = new Converter(object, VarargConverter.class.getMethod("stringifyArray", Object[].class));
    
    Iterator<Converter> iterator = provider.iterator();
    int count = 0;
    while(iterator.hasNext()) {
      Converter c = iterator.next();
      count++;
      
      assertEquals(stringifyArray, c);
      
      try {
        iterator.remove();  // potential modification of the underlying collection
        fail();
      } catch (UnsupportedOperationException ignored) {
        // expected
      }
    }
    
    assertEquals(1, count);
    assertMatchingCollections(Arrays.asList(stringifyArray), toList(provider));
  }
  

  
  @Test
  public void iterator() throws SecurityException, NoSuchMethodException {
    MultipleConverter object = new MultipleConverter();
    Converts.EagerProvider provider = new Converts.EagerProvider(object);
    
    List<Converter> converters = Arrays.asList(
        new Converter(object, extractMethod(MultipleConverter.class, "converter", double.class)),
        new Converter(object, extractMethod(MultipleConverter.class, "convert", String.class)));
    
    Iterator<Converter> iterator = provider.iterator();
    
    // first element
    assertTrue(iterator.hasNext());
    Converter first = iterator.next();
    assertTrue(converters.contains(first));
    
    // second element
    assertTrue(iterator.hasNext());
    Converter second = iterator.next();
    assertTrue(converters.contains(second));
    assertNotSame(first, second);
    
    // no more elements
    assertFalse(iterator.hasNext());
    
    try {
      iterator.next();
      fail();
    } catch (NoSuchElementException ignored) {
      // expected
    }
  }
  
  @Test
  public void emptyIterator() {
    Converts.EagerProvider provider = new Converts.EagerProvider(null);
    
    Iterator<Converter> iterator = provider.iterator();
    
    // no elements
    assertFalse(iterator.hasNext());
    
    try {
      iterator.next();
      fail();
    } catch (NoSuchElementException ignored) {
      // expected
    }
  }
}
