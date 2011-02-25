package com.googlecode.transmuter.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.transmuter.converter.Converts;
import com.googlecode.transmuter.fixture.VarargConverter;
import com.googlecode.transmuter.type.TypeToken;

public class TransmuterUnregisterTest {
  private Transmuter t;
  
  @Before
  public void setUp() {
    t = new Transmuter();
  }
  
  @Test
  public void unregisterNullAndNonexistent() {
    t.register(new Converts.EagerProvider(new VarargConverter()));
    
    assertTrue(t.isRegistered(Object[].class, String.class));
    assertEquals(1, t.getConverterMap().size());
    
    t.unregister(null); // nothing happens
    
    assertTrue(t.isRegistered(Object[].class, String.class));
    assertEquals(1, t.getConverterMap().size());
    
    t.unregister(boolean.class, int.class); // doesn't exist, nothing happens
    
    assertTrue(t.isRegistered(Object[].class, String.class));
    assertEquals(1, t.getConverterMap().size());
    
    t.unregister(null, String.class); // nothing happens
    
    assertTrue(t.isRegistered(Object[].class, String.class));
    assertEquals(1, t.getConverterMap().size());
    
    t.unregister(TypeToken.STRING, TypeToken.get(void.class)); // nothing happens
    
    assertTrue(t.isRegistered(Object[].class, String.class));
    assertEquals(1, t.getConverterMap().size());
  }
}
