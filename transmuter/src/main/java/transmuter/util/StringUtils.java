package transmuter.util;

import static transmuter.util.ObjectUtils.*;

public final class StringUtils {
  // ensuring non-instantiability and non-inheritability
  private StringUtils() { /* empty block */ }

  public static String concatenate(String delim, Object... objects) {
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
