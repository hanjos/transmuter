package com.googlecode.transmuter.converter;

import static com.googlecode.transmuter.TestUtils.extractMethod;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.transmuter.converter.exception.BindingInstantiationException;

public class ConverterTest {
  private String string;
  private Method matchesMethod;
  private Method valueOfMethod;
  private Converter matches;
  private Converter valueOf;
  
  @Before
  public void setUp() throws SecurityException, NoSuchMethodException {
    string = "0123456789";
    matchesMethod = extractMethod(String.class, "matches", String.class);
    valueOfMethod = extractMethod(String.class, "valueOf", Object.class);
    
    matches = new Converter(string, matchesMethod);
    valueOf = new Converter(valueOfMethod);
  }
  
  @Test
  public void getType() {
    assertEquals(new ConverterType(String.class, boolean.class), matches.getType());
    assertEquals(new ConverterType(Object.class, String.class), valueOf.getType());
  }
  
  @Test
  public void constructorWithInvalidMethod() throws SecurityException, NoSuchMethodException {
    Method substringMethod = extractMethod(String.class, "substring", int.class, int.class);
    
    // one can make a Binding with string and substringMethod...
    Binding binding = new Binding(string, substringMethod);
    assertEquals(string, binding.getInstance());
    assertEquals(substringMethod, binding.getMethod());
    
    // ...but not a Converter!
    try {
      new Converter(string, substringMethod);
      fail();
    } catch (BindingInstantiationException e) {
      // TODO proper inspection
    }
  }
}
