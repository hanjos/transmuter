package com.googlecode.transmuter.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

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
    
    assertEquals(n, n.report(iae));
    
    assertFalse(n.isOk());
    assertTrue(n.hasErrors());
    assertArrayEquals(new Object[] { iae }, n.getErrors().toArray());
    
    assertEquals(n, n.report(npe));
    
    assertFalse(n.isOk());
    assertTrue(n.hasErrors());
    assertArrayEquals(new Object[] { iae, npe }, n.getErrors().toArray());
  }
  
  @Test
  public void reportWithIterable() {
    assertTrue(n.isOk());
    assertFalse(n.hasErrors());
    assertTrue(n.getErrors().isEmpty());
    
    assertEquals(n, n.report(Arrays.asList(iae, npe, e)));
    
    assertFalse(n.isOk());
    assertTrue(n.hasErrors());
    assertArrayEquals(new Object[] { iae, npe, e }, n.getErrors().toArray());
  }
  
  @Test
  public void reportWithVarargs() {
    assertTrue(n.isOk());
    assertFalse(n.hasErrors());
    assertTrue(n.getErrors().isEmpty());
    
    assertEquals(n, n.report(iae, npe, e));
    
    assertFalse(n.isOk());
    assertTrue(n.hasErrors());
    assertArrayEquals(new Object[] { iae, npe, e }, n.getErrors().toArray());
  }
  
  @Test
  public void reportNulls() {
    assertTrue(n.isOk());
    assertFalse(n.hasErrors());
    assertTrue(n.getErrors().isEmpty());
    
    assertEquals(n, n.report());
    
    assertTrue(n.isOk());
    assertFalse(n.hasErrors());
    assertTrue(n.getErrors().isEmpty());
    
    assertEquals(n, n.report((Exception) null));
    
    assertTrue(n.isOk());
    assertFalse(n.hasErrors());
    assertTrue(n.getErrors().isEmpty());
    
    assertEquals(n, n.report((Exception[]) null));
    
    assertTrue(n.isOk());
    assertFalse(n.hasErrors());
    assertTrue(n.getErrors().isEmpty());
    
    assertEquals(n, n.report(null, null, null));
    
    assertTrue(n.isOk());
    assertFalse(n.hasErrors());
    assertTrue(n.getErrors().isEmpty());
    
    assertEquals(n, n.report((Iterable<Exception>) null));
    
    assertTrue(n.isOk());
    assertFalse(n.hasErrors());
    assertTrue(n.getErrors().isEmpty());
    
    assertEquals(n, n.report(null, null, iae, null, null, null));
    
    assertFalse(n.isOk());
    assertTrue(n.hasErrors());
    assertArrayEquals(new Object[] { iae }, n.getErrors().toArray());
  }
  
  @Test
  public void methodChaining() {
    assertEquals(n, n.report(iae).report(null, null).report(npe).report(e));
    assertArrayEquals(new Object[] { iae, npe, e }, n.getErrors().toArray());
  }
  
  @Test
  public void iterable() {
    for(@SuppressWarnings("unused") Exception error : n)
      fail("There should be no errors in n!");
    
    assertEquals(n, n.report(iae, npe).report(e));
    
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
    
    n.report(iae);
    n.report(npe);
    
    Notification heir = new Notification(n); // to you, son, I leave all my debts...
    assertFalse(heir.isOk());
    assertTrue(heir.hasErrors());
    assertArrayEquals(new Object[] { iae, npe }, heir.getErrors().toArray());
    assertArrayEquals(n.getErrors().toArray(), heir.getErrors().toArray());
    
    heir.report(e);
    assertFalse(heir.isOk());
    assertTrue(heir.hasErrors());
    assertArrayEquals(new Object[] { iae, npe, e }, heir.getErrors().toArray());
    assertArrayEquals(new Object[] { iae, npe }, n.getErrors().toArray());
  }
}
