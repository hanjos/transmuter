package com.googlecode.transmuter.util;

import static com.googlecode.transmuter.TestUtils.assertMatchingCollections;
import static com.googlecode.transmuter.util.CollectionUtils.toList;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

public class CollectionUtilsTest {
  // TODO better tests
  @SuppressWarnings("serial")
  @Test
  public void testToList() {
    assertNull(toList(null));
    
    assertMatchingCollections(new ArrayList<String>(), toList(new HashSet<String>()));
    
    assertMatchingCollections(Arrays.asList("something"), toList(new HashSet<String>() {{ add("something"); }}));
  }
}
