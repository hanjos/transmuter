package com.googlecode.transmuter.util;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class NotificationTest {
  private Notification n;
  
  private IllegalArgumentException iae;
  private NullPointerException npe;
  private Exception e;
  
  @Before
  public void setUp() {
    n = new Notification();
    
    iae = new IllegalArgumentException();
    npe = new NullPointerException();
    e = new Exception();
  }
  
  @Test
  public void reportIsOkAndHasErrors() {
    assertTrue(n.isOk());
    assertFalse(n.hasErrors());
    assertTrue(n.getErrors().isEmpty());
    
    assertEquals(n, n.add(iae));
    
    assertFalse(n.isOk());
    assertTrue(n.hasErrors());
    assertArrayEquals(new Object[] { iae }, n.getErrors().toArray());
    
    assertEquals(n, n.add(npe));
    
    assertFalse(n.isOk());
    assertTrue(n.hasErrors());
    assertArrayEquals(new Object[] { iae, npe }, n.getErrors().toArray());
  }
  
  @Test
  public void reportWithIterable() {
    assertTrue(n.isOk());
    assertFalse(n.hasErrors());
    assertTrue(n.getErrors().isEmpty());
    
    assertEquals(n, n.add(Arrays.asList(iae, npe, e)));
    
    assertFalse(n.isOk());
    assertTrue(n.hasErrors());
    assertArrayEquals(new Object[] { iae, npe, e }, n.getErrors().toArray());
  }
  
  @Test
  public void reportWithVarargs() {
    assertTrue(n.isOk());
    assertFalse(n.hasErrors());
    assertTrue(n.getErrors().isEmpty());
    
    assertEquals(n, n.add(iae, npe, e));
    
    assertFalse(n.isOk());
    assertTrue(n.hasErrors());
    assertArrayEquals(new Object[] { iae, npe, e }, n.getErrors().toArray());
  }
  
  @Test
  public void reportNulls() {
    assertTrue(n.isOk());
    assertFalse(n.hasErrors());
    assertTrue(n.getErrors().isEmpty());
    
    assertEquals(n, n.add());
    
    assertTrue(n.isOk());
    assertFalse(n.hasErrors());
    assertTrue(n.getErrors().isEmpty());
    
    assertEquals(n, n.add((Exception) null));
    
    assertTrue(n.isOk());
    assertFalse(n.hasErrors());
    assertTrue(n.getErrors().isEmpty());
    
    assertEquals(n, n.add((Exception[]) null));
    
    assertTrue(n.isOk());
    assertFalse(n.hasErrors());
    assertTrue(n.getErrors().isEmpty());
    
    assertEquals(n, n.add(null, null, null));
    
    assertTrue(n.isOk());
    assertFalse(n.hasErrors());
    assertTrue(n.getErrors().isEmpty());
    
    assertEquals(n, n.add((Iterable<Exception>) null));
    
    assertTrue(n.isOk());
    assertFalse(n.hasErrors());
    assertTrue(n.getErrors().isEmpty());
    
    assertEquals(n, n.add(null, null, iae, null, null, null));
    
    assertFalse(n.isOk());
    assertTrue(n.hasErrors());
    assertArrayEquals(new Object[] { iae }, n.getErrors().toArray());
  }
  
  @Test
  public void methodChaining() {
    assertEquals(n, n.add(iae).add(null, null).add(npe).add(e));
    assertArrayEquals(new Object[] { iae, npe, e }, n.getErrors().toArray());
  }
  
  @Test
  public void iterable() {
    for(@SuppressWarnings("unused") Exception error : n)
      fail("There should be no errors in n!");
    
    assertEquals(n, n.add(iae, npe).add(e));
    
    List<Exception> accumulated = new ArrayList<Exception>();
    for(Exception error : n)
      accumulated.add(error);
    
    assertArrayEquals(accumulated.toArray(), n.getErrors().toArray());
  }
  
  @Test
  public void constructors() {
    Notification emptyN = new Notification();
    assertTrue(emptyN.isOk());
    assertFalse(emptyN.hasErrors());
    assertTrue(emptyN.getErrors().isEmpty());
    
    n.add(iae);
    n.add(npe);
    
    Notification heir = new Notification().add(n); // to you, son, I leave all my debts...
    assertFalse(heir.isOk());
    assertTrue(heir.hasErrors());
    assertArrayEquals(new Object[] { iae, npe }, heir.getErrors().toArray());
    assertArrayEquals(n.getErrors().toArray(), heir.getErrors().toArray());
    
    heir.add(e);
    assertFalse(heir.isOk());
    assertTrue(heir.hasErrors());
    assertArrayEquals(new Object[] { iae, npe, e }, heir.getErrors().toArray());
    assertArrayEquals(new Object[] { iae, npe }, n.getErrors().toArray());
  }
  
  @Test
  public void getErrors() {
    n.add(iae, npe);
    
    List<Exception> errors = n.getErrors();
    
    assertTrue(errors.contains(iae));
    assertTrue(errors.contains(npe));
    assertFalse(errors.contains(e));
    
    try {
      errors.add(e);
      fail();
    } catch(UnsupportedOperationException e) {
      // expected; we can safely ignore it
    }
    
    n.add(e);
    assertTrue(errors.contains(e));
    
    Iterator<Exception> iterator = n.iterator();
    
    try {
      iterator.remove();
    } catch(UnsupportedOperationException e) {
      // expected; we can safely ignore it
    }
  }
}
