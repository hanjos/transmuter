package com.googlecode.transmuter.util;

import static com.googlecode.transmuter.util.StringUtils.concatenate;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

public class StringUtilsTest {
  @SuppressWarnings("unchecked")
  @Test
  public void testConcatenateArrayWithoutAndWithNullStringifier() {
    assertEquals("[a, b, c]", concatenate(", ", new Object[] { "[a", "b", "c]" }));
    assertEquals("false, b, c]", concatenate(", ", new Object[] { false, "b", "c]" }));
    assertEquals("[abc]", concatenate(null, new Object[] { "[a", "b", "c]" }));
    assertEquals("[abc]", concatenate("", new Object[] { "[a", "b", "c]" }));
    assertEquals(
        "double, class java.lang.String, null", 
        concatenate(", ", new Object[] { double.class, String.class, null }));
    assertEquals("a1c", concatenate("", Arrays.asList("a", 1, "c"))); 
    assertEquals("a1c", concatenate(null, Arrays.asList("a", 1, "c")));
    
    assertEquals("", concatenate(", ", (Object[]) null));
    assertEquals("", concatenate(null, (Object[]) null));
    assertEquals("", concatenate(", ", new Object[] {}));
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void testConcatenateIterableWithoutAndWithNullStringifier() {
    assertEquals("[a, b, c]", concatenate(", ", Arrays.asList("[a", "b", "c]")));
    assertEquals("false, b, c]", concatenate(", ", Arrays.asList(false, "b", "c]")));
    assertEquals("[abc]", concatenate(null, Arrays.asList("[a", "b", "c]")));
    assertEquals("[abc]", concatenate("", Arrays.asList("[a", "b", "c]")));
    assertEquals("double, class java.lang.String, null", concatenate(", ", Arrays.asList(double.class, String.class, null)));
    
    assertEquals("", concatenate(", ", (Iterable<?>) null));
    assertEquals("", concatenate(null, (Iterable<?>) null));
    assertEquals("", concatenate(", ", new HashSet<String>()));
  }
}
