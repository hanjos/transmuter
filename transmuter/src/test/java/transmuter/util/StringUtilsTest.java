package transmuter.util;

import static transmuter.util.StringUtils.concatenate;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class StringUtilsTest {
  @SuppressWarnings("unchecked")
  @Test
  public void testConcatenateWithoutStringifier() {
    assertEquals("[a, b, c]", concatenate(", ", Arrays.asList("[a", "b", "c]")));
    assertEquals("double, class java.lang.String, null", concatenate(", ", Arrays.asList(double.class, String.class, null)));
    
    assertEquals("", concatenate(", ", new String[] {}));
    assertEquals("", concatenate(", ", (Object[]) null));
  }
}
