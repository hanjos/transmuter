package transmuter.util;

/**
 * Contains utility methods for common operations on objects.
 * This class is not meant to be inherited from or instantiated.
 * 
 * @author Humberto S. N. dos Anjos
 */
public final class ObjectUtils {
  // ensuring non-instantiability and non-inheritability
  private ObjectUtils() { /* empty block */ }
  
  /**
   * Checks if the given object is not null.
   * 
   * @param object an object.
   * @param <T> the type of the given object.
   * @return the same object received, unmodified.
   * @throws IllegalArgumentException if the given object is null.
   */
  public static <T> T nonNull(T object) throws IllegalArgumentException {
    return nonNull(object, "value not allowed");
  }

  /**
   * Checks if the given object is not null.
   * 
   * @param object an object.
   * @param message a message to be used in the exception.
   * @param <T> the type of the given object.
   * @return the same object received, unmodified.
   * @throws IllegalArgumentException if the given object is null.
   */
  public static <T> T nonNull(T object, String message) throws IllegalArgumentException {
    if (object == null)
      throw new IllegalArgumentException(message + ": null");

    return object;
  }

  /**
   * Returns the hash code of the given object.
   * 
   * @param object an object.
   * @return the hash code of the given object, or 0 if it's {@code null}.
   */
  public static int hashCodeOf(Object object) {
    return (object == null) ? 0 : object.hashCode();
  }

  /**
   * Checks if both arguments are {@link Object#equals(Object) equal}.
   * 
   * @param a an object.
   * @param b an object.
   * @return {@code true} if both arguments are null, the same object, or equivalent; 
   * {@code false} otherwise.
   */
  public static boolean areEqual(Object a, Object b) {
    return (a == b) || (a == null ? b.equals(a) : a.equals(b));
  }
  
  /**
   * Returns the class of the given object.
   * 
   * @param object an object.
   * @return the class of the given object, or {@code null} if the given object is null.
   */
  public static Class<?> classOf(Object object) {
    return (object != null) ? object.getClass() : null;
  }
  
  /**
   * Checks if the array with the arguments is empty.
   * 
   * @param objects an array of objects.
   * @return {@code true} if {@code objects} is either {@code null} or has length 0; {@code false} otherwise.
   */
  public static <T> boolean isEmpty(T... objects) {
    return objects == null || objects.length == 0;
  }
}
