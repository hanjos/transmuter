package transmuter.util;

public class ObjectUtils {
  public static <T> T nonNull(T object) {
    return nonNull(object, "null value not allowed!");
  }
  
  public static <T> T nonNull(T object, String message) {
    if(object == null)
      throw new IllegalArgumentException(message);
    
    return object;
  }
  
  public static Class<?> classOf(Object object) {
    if(object == null)
      return null;
    
    return object.getClass();
  }
  
  public static int hashCodeOf(Object object) {
    return (object == null) ? 0 : object.hashCode();
  }
  
  public static boolean areEqual(Object a, Object b) {
    return (a == b)
        || (a == null ? b.equals(a) : a.equals(b));
  }
}
