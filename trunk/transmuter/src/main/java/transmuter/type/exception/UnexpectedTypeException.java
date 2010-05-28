package transmuter.type.exception;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UnexpectedTypeException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private static String buildMessage(Type type, Class<?>... expected) {
    StringBuilder message = new StringBuilder();
    
    if(expected == null || expected.length == 0) {
      message.append("Expected nothing, ");
    } else {
      message.append("Expected one of: ");
    
      for (Class<?> clazz : expected)
        message.append(clazz.getName()).append(", ");
    }
    
    if(type != null)
      message.append("but got: ").append(type.getClass().getName())
          .append(", for type: ").append(type).append('.');
    else
      message.append("but got a null type.");

    return message.toString();
  }
  
  private Type type;
  private List<Class<?>> expected;

  public UnexpectedTypeException(Type type, Class<?>... expected) {
    super(buildMessage(type, expected));
    
    this.type = type;
    this.expected = Collections.unmodifiableList(Arrays.asList(expected));
  }

  public Type getType() {
    return type;
  }

  public List<Class<?>> getExpected() {
    return expected;
  }
}
