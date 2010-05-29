package transmuter.exception;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import transmuter.type.TypeToken;
import transmuter.util.Pair;
import transmuter.util.ReflectionUtils;
import transmuter.util.StringUtils;
import transmuter.util.StringUtils.Stringifier;

public class SameClassConverterCollisionException extends ConverterCollisionException {
  private static final long serialVersionUID = 1L;
  
  private static String buildMessage(List<Method> methods, TypeToken<?> declaringType, Pair pair) {
    return "more than one converter for " + pair + " found in " + declaringType + ": [" 
        + StringUtils.concatenate(
          new Stringifier<Method>() {
            @Override
            public String stringify(Method o) {
              return ReflectionUtils.simpleMethodToString(o);
            }
          }, ", ", methods) 
        + "]";
  }
  
  private TypeToken<?> declaringType;
  
  public SameClassConverterCollisionException(List<Method> methods, Type declaringType, Pair pair) {
    this(methods, TypeToken.get(declaringType), pair);
  }
  
  public SameClassConverterCollisionException(List<Method> methods, Type declaringType, Pair pair, String message) {
    this(methods, TypeToken.get(declaringType), pair, message);
  }
  
  public SameClassConverterCollisionException(List<Method> methods, TypeToken<?> declaringType, Pair pair) {
    this(methods, declaringType, pair, buildMessage(methods, declaringType, pair));
  }
  
  public SameClassConverterCollisionException(List<Method> methods, TypeToken<?> declaringType, Pair pair,
      String message) {
    super(methods, pair, message);
    
    this.declaringType = declaringType;
  }
  
  public TypeToken<?> getDeclaringType() {
    return declaringType;
  }
}
