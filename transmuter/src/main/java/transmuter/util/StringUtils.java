package transmuter.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class StringUtils {
  private StringUtils() { /* empty block */ }

  public static interface Stringifier<T> {
    public String stringify(T o);
  }
  
  public static final Stringifier<Object> DEFAULT;
  
  static {
    DEFAULT = new Stringifier<Object>() {
      @Override
      public String stringify(Object o) {
        return String.valueOf(o);
      }
    };
  }
  
  public static <T> String concatenate(String delim, T[] objects) {
    return concatenate(DEFAULT, delim, objects);
  }
  
  @SuppressWarnings("unchecked")
  public static <T> String concatenate(Stringifier<? super T> stringifier, String delim, T[] objects) {
    return concatenate(
        stringifier, 
        delim, 
        (List<T>) ((objects != null) ? Arrays.asList(objects) : Collections.EMPTY_LIST));
  }
  
  public static <T> String concatenate(String delim, Collection<T> objects) {
    return concatenate(DEFAULT, delim, objects);
  }
  
  public static <T> String concatenate(Stringifier<? super T> stringifier, String delim, Collection<T> objects) {
    if (objects == null || objects.isEmpty())
      return "";
    
    if(stringifier == null)
      stringifier = DEFAULT;
  
    Iterator<T> iterator = objects.iterator();
    StringBuilder sb = new StringBuilder(stringifier.stringify(iterator.next()));
    
    while(iterator.hasNext())
      sb.append(delim).append(stringifier.stringify(iterator.next()));
    
    return sb.toString();
  }
}
