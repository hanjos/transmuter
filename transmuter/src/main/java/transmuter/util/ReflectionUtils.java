package transmuter.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;

import transmuter.util.StringUtils.Stringifier;

public final class ReflectionUtils {
  static final String[] EMPTY_STRING_ARRAY = new String[0];

  private ReflectionUtils() { /* empty block */ }

  public static String simpleMethodToString(Method method) {
    if(method == null)
      return "<null>";
    
    StringBuilder sb = new StringBuilder();
    
    sb.append(Modifier.toString(method.getModifiers()));
    
    Type[] typeparms = method.getTypeParameters();
    if (typeparms.length > 0)
      sb.append("<").append(StringUtils.concatenate(", ", getTypeNames(typeparms))).append("> ");
    
    sb.append(getTypeName(method.getGenericReturnType())).append(" ");
    sb.append(method.getName()).append("(");
    sb.append(StringUtils.concatenate(", ", getTypeNames(method.getGenericParameterTypes()))).append(")");
    
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
      try {
        Class<?> cl = cls;
        int dimensions = 0;
        while (cl.isArray()) {
          dimensions++;
          cl = cl.getComponentType();
        }
  
        StringBuilder sb = new StringBuilder().append(cl.getName());
        for (int i = 0; i < dimensions; i++) {
          sb.append("[]");
        }
  
        return sb.toString();
      } catch (Throwable e) {
        /* FALLTHRU */
      }
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

  public static String listMethodsToString(List<Method> methods) {
    return "[" + StringUtils.concatenate(
        new Stringifier<Method>() {
          @Override
          public String stringify(Method o) {
            return simpleMethodToString(o);
          }
        },
        ", ",
        methods) + "]";
  }

  public static Method extractMethod(Class<?> cls, String name,
      Class<?>... parameterTypes) throws NoSuchMethodException,
      SecurityException {
    return cls.getDeclaredMethod(name, parameterTypes);
  }
  
  
}
