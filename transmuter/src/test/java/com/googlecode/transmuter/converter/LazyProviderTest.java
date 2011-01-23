package com.googlecode.transmuter.converter;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Test;

import com.googlecode.transmuter.converter.Converter;
import com.googlecode.transmuter.converter.Converts;
import com.googlecode.transmuter.converter.exception.ConverterProviderException;
import com.googlecode.transmuter.converter.exception.InvalidParameterTypeException;
import com.googlecode.transmuter.converter.exception.InvalidReturnTypeException;
import com.googlecode.transmuter.fixture.FlawedConverter;
import com.googlecode.transmuter.fixture.GenericConverter;
import com.googlecode.transmuter.fixture.GenericMethodConverter;
import com.googlecode.transmuter.fixture.MultipleConverter;
import com.googlecode.transmuter.fixture.PartialGenericConverter;
import com.googlecode.transmuter.fixture.VarargConverter;

public class LazyProviderTest {
  @Test
  public void simpleProvider() throws SecurityException, NoSuchMethodException {
    MultipleConverter object = new MultipleConverter();
    Converts.LazyProvider provider = new Converts.LazyProvider(object);
    
    Converter converter = new Converter(object, extractMethod(MultipleConverter.class, "converter", double.class));
    Converter convert = new Converter(object, extractMethod(MultipleConverter.class, "convert", String.class));
    
    assertMatchingCollections(Arrays.asList(converter, convert), toList(provider));
  }
  
  @Test
  public void nullProvider() {
    Converts.LazyProvider provider = new Converts.LazyProvider(null);
    
    assertMatchingCollections(Collections.emptyList(), toList(provider));
  }
  
  @Test
  public void noConvertsProvider() {
    Converts.LazyProvider provider = new Converts.LazyProvider(new Object());
    
    assertMatchingCollections(Collections.emptyList(), toList(provider));
  }
  
  @Test
  public void privateProvider() {
    Converts.LazyProvider provider = new Converts.LazyProvider(new Object() {
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
    Converts.LazyProvider provider = new Converts.LazyProvider(new Object() {
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
    Converts.LazyProvider provider = new Converts.LazyProvider(new Object() {
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
    Converts.LazyProvider provider = new Converts.LazyProvider(object);
    
    Converter stringifyArray = new Converter(object, VarargConverter.class.getMethod("stringifyArray", Object[].class));
    
    assertMatchingCollections(Arrays.asList(stringifyArray), toList(provider));
  }
  
  @Test
  public void genericMethodProvider() {
    // errors are only detected in the iteration, so the constructor works fine
    Iterable<Converter> iterable = new Converts.LazyProvider(new GenericMethodConverter());
    
    List<Exception> causes = new ArrayList<Exception>();
    
    Iterator<Converter> iterator = iterable.iterator();
    while(iterator.hasNext()) {
      try {
        iterator.next();
        fail();
      } catch(ConverterProviderException e) {
        causes.addAll(e.getCauses());
      }
    }
    
    assertEquals(2, causes.size());
    
    assertEquals(InvalidParameterTypeException.class, causes.get(0).getClass());
    assertTrue(((InvalidParameterTypeException) causes.get(0)).getType() instanceof TypeVariable<?>);
    
    assertEquals(InvalidReturnTypeException.class, causes.get(1).getClass());
    assertTrue(((InvalidReturnTypeException) causes.get(1)).getType() instanceof TypeVariable<?>);
  }
  
  @Test
  public void partialGenericClassProvider() throws SecurityException, NoSuchMethodException {
    // errors are only detected in the iteration, so the constructor works fine
    Iterable<Converter> iterable = new Converts.LazyProvider(new PartialGenericConverter<List<String>>()); 
    // can't get List<String> from Java at runtime
    
    List<Exception> causes = new ArrayList<Exception>();
    
    Iterator<Converter> iterator = iterable.iterator();
    while(iterator.hasNext()) {
      try {
        iterator.next();
        fail();
      } catch(ConverterProviderException e) {
        causes.addAll(e.getCauses());
      }
    }
    
    assertEquals(1, causes.size());
    assertEquals(InvalidParameterTypeException.class, causes.get(0).getClass());
    
    InvalidParameterTypeException ex = (InvalidParameterTypeException) causes.get(0);
    assertEquals(extractMethod(PartialGenericConverter.class, "convert", Object.class), ex.getMethod());
    assertTrue(ex.getType() instanceof TypeVariable<?>);
    assertEquals("From", ((TypeVariable<?>) ex.getType()).getName());
  }
  
  @Test
  public void partialGenericClassProvider2() throws NoSuchMethodException {
    // errors are only detected in the iteration, so the constructor works fine
    Iterable<Converter> iterable = new Converts.LazyProvider(new PartialGenericConverter<Object>()); // raw 
    
    List<Exception> causes = new ArrayList<Exception>();
    
    Iterator<Converter> iterator = iterable.iterator();
    while(iterator.hasNext()) {
      try {
        iterator.next();
        fail();
      } catch(ConverterProviderException e) {
        causes.addAll(e.getCauses());
      }
    }

    assertEquals(1, causes.size());
    assertEquals(InvalidParameterTypeException.class, causes.get(0).getClass());
    final Method convertMethod = extractMethod(PartialGenericConverter.class, "convert", Object.class);
    
    InvalidParameterTypeException ex = (InvalidParameterTypeException) causes.get(0);
    assertEquals(convertMethod, ex.getMethod());
    assertTrue(ex.getType() instanceof TypeVariable<?>);
    assertEquals("From", ((TypeVariable<?>) ex.getType()).getName());
  }
  
  @Test
  public void methodFromGenericClass() throws SecurityException, NoSuchMethodException {
    // errors are only detected in the iteration, so the constructor works fine
    Iterable<Converter> iterable = new Converts.LazyProvider(new GenericConverter<List<String>, String>()); 
    // can't get <List<String>, String> from Java at runtime
    
    List<Exception> causes = new ArrayList<Exception>();
    
    Iterator<Converter> iterator = iterable.iterator();
    while(iterator.hasNext()) {
      try {
        iterator.next();
        fail();
      } catch(ConverterProviderException e) {
        causes.addAll(e.getCauses());
      }
    }

    assertEquals(2, causes.size());
    assertEquals(InvalidParameterTypeException.class, causes.get(0).getClass());
    assertEquals(InvalidReturnTypeException.class, causes.get(1).getClass());
    
    InvalidParameterTypeException ex = (InvalidParameterTypeException) causes.get(0);
    assertEquals(extractMethod(GenericConverter.class, "convert", Object.class), ex.getMethod());
    assertTrue(ex.getType() instanceof TypeVariable<?>);
    assertEquals("From", ((TypeVariable<?>) ex.getType()).getName());
    
    InvalidReturnTypeException ex1 = (InvalidReturnTypeException) causes.get(1);
    assertEquals(extractMethod(GenericConverter.class, "convert", Object.class), ex1.getMethod());
    assertTrue(ex1.getType() instanceof TypeVariable<?>);
    assertEquals("To", ((TypeVariable<?>) ex1.getType()).getName());
  }

  @Test
  public void methodFromGenericClass2() throws NoSuchMethodException {
    // errors are only detected in the iteration, so the constructor works fine
    Iterable<Converter> iterable = new Converts.LazyProvider(new GenericConverter<Object, Object>()); 
    // can't get <List<String>, String> from Java at runtime
    
    List<Exception> causes = new ArrayList<Exception>();
    
    Iterator<Converter> iterator = iterable.iterator();
    while(iterator.hasNext()) {
      try {
        iterator.next();
        fail();
      } catch(ConverterProviderException e) {
        causes.addAll(e.getCauses());
      }
    }

    assertEquals(2, causes.size());
    assertEquals(InvalidParameterTypeException.class, causes.get(0).getClass());
    assertEquals(InvalidReturnTypeException.class, causes.get(1).getClass());
      
    InvalidParameterTypeException ex = (InvalidParameterTypeException) causes.get(0);
    assertEquals(extractMethod(GenericConverter.class, "convert", Object.class), ex.getMethod());
    assertTrue(ex.getType() instanceof TypeVariable<?>);
    assertEquals("From", ((TypeVariable<?>) ex.getType()).getName());
    
    InvalidReturnTypeException ex1 = (InvalidReturnTypeException) causes.get(1);
    assertEquals(extractMethod(GenericConverter.class, "convert", Object.class), ex1.getMethod());
    assertTrue(ex1.getType() instanceof TypeVariable<?>);
    assertEquals("To", ((TypeVariable<?>) ex1.getType()).getName());
  }
  
  @Test
  public void flawedProvider() throws SecurityException, NoSuchMethodException {
    Object flawed = new FlawedConverter();
    
    // errors are only detected in the iteration, so the constructor works fine
    Iterable<Converter> iterable = new Converts.LazyProvider(flawed); 
    
    List<Exception> causes = new ArrayList<Exception>();
    Iterator<Converter> iterator = iterable.iterator();
    while(iterator.hasNext()) {
      try {
        Converter c = iterator.next();
        String methodName = c.getMethod().getName();
        if(! "intraClassCollision1".equals(methodName) 
        && ! "intraClassCollision2".equals(methodName)
        && ! "extraClassCollision".equals(methodName))
          fail();
      } catch(ConverterProviderException e) {
        causes.addAll(e.getCauses());
      }
    }
    
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
  
  @Test
  public void immutableProvider() throws SecurityException, NoSuchMethodException {
    VarargConverter object = new VarargConverter();
    Converts.LazyProvider provider = new Converts.LazyProvider(object);
    
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
    Converts.LazyProvider provider = new Converts.LazyProvider(object);
    
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
    Converts.LazyProvider provider = new Converts.LazyProvider(null);
    
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
