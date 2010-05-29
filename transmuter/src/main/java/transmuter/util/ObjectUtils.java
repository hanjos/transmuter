package transmuter.util;

public final class ObjectUtils {
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
}
