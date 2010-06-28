package com.googlecode.transmuter.util;

import static com.googlecode.gentyref.GenericTypeReflector.capture;
import static com.googlecode.gentyref.GenericTypeReflector.getExactSuperType;
import static com.googlecode.transmuter.util.ObjectUtils.isEmpty;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

/**
 * Contains utility methods for common reflection operations.
 * This class is not meant to be inherited from or instantiated.
 * 
 * @author Humberto S. N. dos Anjos
 */
public final class ReflectionUtils {
  private static final String[] EMPTY_STRING_ARRAY = new String[0];

  // ensuring non-instantiability and non-inheritability
  private ReflectionUtils() { /* empty block */ }

  /**
   * Returns a simplified string representation of the given method.
   * 
   * @param method a method object.
   * @return a simplified string representation of the given method, or the string {@code "<null>"} if the given 
   * method is {@code null}.
   */
  public static String simpleMethodToString(Method method) {
    if(method == null)
      return "<null>";
    
    StringBuilder sb = new StringBuilder();
    
    sb.append(Modifier.toString(method.getModifiers())).append(" ");
    
    Type[] typeparms = method.getTypeParameters();
    if (typeparms.length > 0)
      sb.append("<").append(StringUtils.concatenate(", ", getTypeNames(typeparms))).append("> ");
    
    /*
     * FIXME: Gentyref doesn't work well with methods with their own generic types; 
     * so I'm using the good ol' method.getGeneric*() methods for now
     */
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
  /**
   * Returns a simple string representation for the given type.
   * 
   * @param type a type object.
   * @return a simple string representation of {@code type}, which is: 
   * <ul>
   * <li>the string {@code "null"}, if {@code type} is {@code null};</li>
   * <li>the type's name, if {@code type} is a {@code Class} object;</li>
   * <li>the type's name followed by the necessary {@code []}s, if {@code type} is a {@code Class} object representing 
   * an array;</li>
   * <li>the result of {@code type}'s {@code toString} method otherwise.</li> 
   * </ul>
   */
  @SuppressWarnings("unchecked")
  public static String getTypeName(Type type) {
    if (type == null)
      return "null";
    
    if (! (type instanceof Class))
      return type.toString();
  
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

  /**
   * Translates the array of types received into an array of Strings holding the types' {@link #getTypeName(Type) names}.
   * 
   * @param types several type objects.
   * @return an array of Strings holding the types' {@link #getTypeName(Type) names}. This array has length 0 if the 
   * given array of types is {@link ObjectUtils#isEmpty(Object...) empty}.
   * @see #getTypeName(Type)
   * @see ObjectUtils#isEmpty(Object...)
   */
  public static String[] getTypeNames(Type... types) {
    if(isEmpty(types))
      return EMPTY_STRING_ARRAY;
    
    String[] typesAsStrings = new String[types.length];
    for(int i = 0; i < typesAsStrings.length; i++)
      typesAsStrings[i] = getTypeName(types[i]);
    
    return typesAsStrings;
  }

  /**
   * Checks if the given type is a subtype of the given method's declaring class.
   * 
   * @param method a method object. 
   * @param type a type object.
   * @return {@code true} if {@code ownerType} is a subtype of {@code method}'s declaring class.
   */
  public static boolean isCompatible(Method method, Type type) {
    return getExactSuperType(capture(type), method.getDeclaringClass()) != null;
  }  
}
