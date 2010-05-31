package transmuter.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

public class ReflectionUtils {
  private static final String[] EMPTY_STRING_ARRAY = new String[0];

  // ensuring non-instantiability and non-inheritability
  private ReflectionUtils() { /* empty block */ }

  public static String simpleMethodToString(Method method) {
    if(method == null)
      return "<null>";
    
    StringBuilder sb = new StringBuilder();
    
    sb.append(Modifier.toString(method.getModifiers())).append(" ");
    
    Type[] typeparms = method.getTypeParameters();
    if (typeparms.length > 0)
      sb.append("<").append(StringUtils.concatenate(", ", getTypeNames(typeparms))).append("> ");
    
    sb.append(getTypeName(method.getGenericReturnType())).append(" ");
    sb.append(method.getName()).append("(");
    
    String[] typeNames = getTypeNames(method.getGenericParameterTypes());
    if(method.isVarArgs()) {
      String last = typeNames[typeNames.length - 1];
      typeNames[typeNames.length - 1] = last.substring(0, last.length() - "[]".length()) + "...";
    }
    
    sb.append(StringUtils.concatenate(", ", typeNames)).append(")");
    
    Type[] exceptions = method.getGenericExceptionTypes(); // avoid clone
    if(exceptions.length > 0)
      sb.append(" throws ").append(StringUtils.concatenate(", ", getTypeNames(exceptions)));
    
    return sb.toString();
  }

  /*
   * Adapted from Field.getTypeName. Utility routine to paper over array type
   * names
   */
  @SuppressWarnings("unchecked")
  public static String getTypeName(Type type) {
    if (!(type instanceof Class))
      return String.valueOf(type);
  
    Class<?> cls = (Class<?>) type;
    if (cls.isArray()) {
      // XXX in Field.getTypeName, this block was wrapped with a try block
      // which allowed a Throwable to simply fall through. Why? 
      Class<?> cl = cls;
      StringBuilder rank = new StringBuilder();
      while (cl.isArray()) {
        rank.append("[]");
        cl = cl.getComponentType();
      }

      return new StringBuilder(cl.getName()).append(rank.toString()).toString();
    }
  
    return cls.getName();
  }

  public static String[] getTypeNames(Type... types) {
    if(types == null || types.length == 0)
      return EMPTY_STRING_ARRAY;
    
    String[] typesAsStrings = new String[types.length];
    for(int i = 0; i < typesAsStrings.length; i++)
      typesAsStrings[i] = getTypeName(types[i]);
    
    return typesAsStrings;
  }  
}
