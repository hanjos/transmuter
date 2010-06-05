package transmuter.util;

public class ObjectUtils {
  // ensuring non-instantiability and non-inheritability
  private ObjectUtils() { /* empty block */ }
  
  public static <T> T nonNull(T object) {
    return nonNull(object, "null value not allowed");
  }

  public static <T> T nonNull(T object, String message) {
    if (object == null)
      throw new IllegalArgumentException(message);

    return object;
  }

  public static int hashCodeOf(Object object) {
    return (object == null) ? 0 : object.hashCode();
  }

  public static boolean areEqual(Object a, Object b) {
    return (a == b) || (a == null ? b.equals(a) : a.equals(b));
  }
  
  public static Class<?> classOf(Object a) {
    return (a != null) ? a.getClass() : null;
  }
}
