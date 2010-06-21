package transmuter.util;

import static org.junit.Assert.assertEquals;
import static transmuter.util.StringUtils.concatenate;

import org.junit.Test;

public class StringUtilsTest {
  @Test
  public void testConcatenateWithoutAndWithNullStringifier() {
    assertEquals("[a, b, c]", concatenate(", ", "[a", "b", "c]"));
    assertEquals("[a, b, c]", concatenate(", ", new Object[] { "[a", "b", "c]" }));
    assertEquals("[abc]", concatenate(null, new Object[] { "[a", "b", "c]" }));
    assertEquals("[abc]", concatenate("", new Object[] { "[a", "b", "c]" }));
    assertEquals("double, class java.lang.String, null", concatenate(", ", double.class, String.class, null));
    
    assertEquals("", concatenate(", "));
    assertEquals("", concatenate(null));
    assertEquals("", concatenate(", ", new Object[] {}));
    assertEquals("", concatenate(", ", (Object[]) null));
  }
}
