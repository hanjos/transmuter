package com.googlecode.transmuter;

import com.googlecode.transmuter.converter.Converter;
import com.googlecode.transmuter.converter.ConverterType;
import com.googlecode.transmuter.converter.exception.InvalidReturnTypeException;
import com.googlecode.transmuter.converter.exception.WrongParameterCountException;
import com.googlecode.transmuter.core.exception.ConverterCollisionException;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Set;

import static com.googlecode.transmuter.util.ObjectUtils.areEqual;
import static org.junit.Assert.*;

public class TestUtils {
  public static Method extractMethod(Class<?> cls, String name, Class<?>... parameterTypes) 
      throws NoSuchMethodException, SecurityException {
    return cls.getMethod(name, parameterTypes);
  }

  public static Method extractDeclaredMethod(Class<?> cls, String name, Class<?>... parameterTypes) 
      throws NoSuchMethodException, SecurityException {
    return cls.getDeclaredMethod(name, parameterTypes);
  }

  public static void assertType(Class<?> cls, Object object) {
    assertNotNull(object);
    assertEquals(cls, object.getClass());
  }

  public static void assertMatchingCollections(final Collection<?> a, final Collection<?> b) {
    if(a == b)
      return;
    
    assertNotNull(a);
    assertNotNull(b);
    
    assertTrue(a.containsAll(b));
    assertTrue(b.containsAll(a));
  }

  public static void assertWrongParameterCount(final Collection<? extends Exception> causes, 
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

  public static void assertInvalidReturnType(final Collection<? extends Exception> causes, 
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

  public static void assertConverterCollision(final Collection<? extends Exception> causes, 
      ConverterType converterType, Set<Converter> converters, int expectedCount) {
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
}
