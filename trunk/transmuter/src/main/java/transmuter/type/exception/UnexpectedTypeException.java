package transmuter.type.exception;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UnexpectedTypeException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private static String buildMessage(Type token, Class<?>... expected) {
    StringBuilder message = new StringBuilder();
    
    if(expected == null || expected.length == 0) {
      message.append("Expected nothing, ");
    } else {
      message.append("Expected one of: ");
    
      for (Class<?> clazz : expected)
        message.append(clazz.getName()).append(", ");
    }
    
    if(token != null)
      message.append("but got: ").append(token.getClass().getName())
          .append(", for type token: ").append(token).append('.');
    else
      message.append("but got a null type token.");

    return message.toString();
  }
  
  private Type token;
  private List<Class<?>> expected;

  public UnexpectedTypeException(Type token, Class<?>... expected) {
    super(buildMessage(token, expected));
    
    this.token = token;
    this.expected = Collections.unmodifiableList(Arrays.asList(expected));
  }

  public Type getToken() {
    return token;
  }

  public List<Class<?>> getExpected() {
    return expected;
  }
}
