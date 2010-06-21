package transmuter.util;

import static transmuter.util.ObjectUtils.*;

/**
 * Some utility methods for Strings. This class cannot be inherited from or instantiated.
 * 
 * @author Humberto S. N. dos Anjos
 */
public final class StringUtils {
  // ensuring non-instantiability and non-inheritability
  private StringUtils() { /* empty block */ }

  /**
   * Returns a string holding the objects' string representation joined by the given delimiter.
   * 
   * Examples:
   * <pre>
   * concatenate(", ", new Object[] { "a", "b", "c" })     // --> "a, b, c"
   * concatenate(", ", new Object[] { false, "b", "c" })   // --> "false, b, c"
   * concatenate("",   new Object[] { "a", 1, "c" })       // --> "a1c"
   * concatenate(null, new Object[] { "a", 1, "c" })       // --> "a1c"
   * concatenate("//", new Object[] { "usr", "bin" })     // --> "usr//bin"
   * concatenate("/",  new Object[] {}) // no arguments given --> ""
   * concatenate("/",  null)            // no arguments given --> ""
   * </pre>
   * 
   * @param delim the delimiter. Will be the empty string if it receives {@code null}.
   * @param objects the given objects for concatenation.
   * @return a string holding the objects' string representation joined by the given delimiter. Will be the empty 
   * string if no objects are given.  
   */
  public static String concatenate(String delim, Object[] objects) {
    if(isEmpty(objects))
      return "";
    
    String firstObjectAsString = String.valueOf(objects[0]);
    
    if(objects.length == 1)
      return firstObjectAsString;
    
    delim = delim != null ? delim : "";
    StringBuilder sb = new StringBuilder(firstObjectAsString);
    for(int i = 1; i < objects.length; i++)
      sb.append(delim).append(String.valueOf(objects[i]));
    
    return sb.toString();
  }
}
