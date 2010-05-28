package transmuter.exception;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;

import transmuter.type.TypeToken;
import transmuter.util.Pair;

public class SameClassConverterCollisionException extends ConverterCollisionException {
  private static final long serialVersionUID = 1L;
  
  private static String buildMessage(List<Method> methods, TypeToken<?> declaringType, Pair pair) {
    return "more than one converter for " + pair + " found in " + declaringType + ": " + listMethodsToString(methods);
  }
  
  private static String listMethodsToString(List<Method> methods) {
    if(methods == null)
      return null;
    
    if(methods.isEmpty())
      return "[]";
    
    StringBuilder sb = new StringBuilder("[ ").append(methodToString(methods.get(0)));
    
    final int length = methods.size();
    for(int i = 1; i < length; i++)
      sb.append(", ").append(methodToString(methods.get(i)));
    
    return sb.append(" ]").toString();
  }
  
  // TODO do a proper toString for Method
  private static String methodToString(Method method) {
    if(method == null)
      return null;
    
    StringBuilder sb = new StringBuilder();
    
    if(Modifier.isStatic(method.getModifiers()))
      sb.append("static ");
    
    sb.append(method.getGenericReturnType() + " ");
    sb.append(method.getName() + "(");
    Type[] params = method.getGenericParameterTypes(); // avoid clone
    for(int j = 0; j < params.length; j++) {
      sb.append(params[j]);
      if(j < (params.length - 1))
        sb.append(", ");
    }
    sb.append(")");
    
    Type[] exceptions = method.getGenericExceptionTypes(); // avoid clone
    if(exceptions.length > 0) {
      sb.append(" throws ");
      for(int k = 0; k < exceptions.length; k++) {
        sb.append(exceptions[k]);
        if(k < (exceptions.length - 1))
          sb.append(", ");
      }
    }
    
    return sb.toString();
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
