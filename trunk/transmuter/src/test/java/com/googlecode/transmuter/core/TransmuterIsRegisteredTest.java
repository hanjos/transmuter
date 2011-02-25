package com.googlecode.transmuter.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.transmuter.converter.ConverterType;
import com.googlecode.transmuter.converter.Converts;
import com.googlecode.transmuter.fixture.MultipleConverter;
import com.googlecode.transmuter.fixture.StringArrayToListStringConverter;
import com.googlecode.transmuter.type.TypeToken;

public class TransmuterIsRegisteredTest {
  private static final TypeToken<List<String>> LIST_OF_STRING = new TypeToken<List<String>>() { /**/ };
  
  private Transmuter t;
  
  @Before
  public void setUp() {
    t = new Transmuter();
  }
  
  @Test
  public void isRegistered() {
    assertTrue(t.getConverterMap().isEmpty());
    
    t.register(new Converts.EagerProvider(new MultipleConverter()));
    
    assertTrue(t.isRegistered(new ConverterType(double.class, String.class)));
    assertTrue(t.isRegistered(TypeToken.STRING, LIST_OF_STRING));
    assertFalse(t.isRegistered(String.class, List.class));
  }
  
  @Test
  public void inheritedGenericMethod() {
    TypeToken<String[]> ARRAY_OF_STRING = TypeToken.get(String[].class);
    
    assertFalse(t.isRegistered(ARRAY_OF_STRING, LIST_OF_STRING));
    
    t.register(new Converts.EagerProvider(new StringArrayToListStringConverter()));
    
    assertTrue(t.isRegistered(ARRAY_OF_STRING, LIST_OF_STRING));
  }
}
