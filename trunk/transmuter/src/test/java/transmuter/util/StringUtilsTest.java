package transmuter.util;

import static org.junit.Assert.assertEquals;
import static transmuter.util.StringUtils.concatenate;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import transmuter.type.TypeToken;
import transmuter.util.StringUtils.Stringifier;

public class StringUtilsTest {
  @SuppressWarnings("unchecked")
  @Test
  public void testConcatenateWithoutAndWithNullStringifier() {
    assertEquals("[a, b, c]", concatenate(", ", new Object[] { "[a", "b", "c]" }));
    assertEquals("[a, b, c]", concatenate(null, ", ", new Object[] { "[a", "b", "c]" }));
    assertEquals("double, class java.lang.String, null", concatenate(", ", Arrays.asList(double.class, String.class, null)));
    assertEquals("double, class java.lang.String, null", concatenate(null, ", ", Arrays.asList(double.class, String.class, null)));
    
    assertEquals("", concatenate(", ", new String[] {}));
    assertEquals("", concatenate(", ", (Object[]) null));
  }
  
  @Test
  public void testConcatenateWithStringifier() {
    assertEquals(
        "double java.lang.String[] null java.util.List<java.lang.String>",
        concatenate(new Stringifier<Type>() {
          @Override
          public String stringify(Type o) {
            if(o == null)
              return "null";
            
            return ReflectionUtils.getTypeName(o);
          }},
          " ", 
          Arrays.asList(double.class, String[].class, null, new TypeToken<List<String>>() {}.getType())));
  }
}
