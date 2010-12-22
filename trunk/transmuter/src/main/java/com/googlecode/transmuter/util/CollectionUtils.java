package com.googlecode.transmuter.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds several collection-related utility methods.
 * 
 * @author Humberto S. N. dos Anjos
 */
public final class CollectionUtils {
  // ensuring non-inheritability and non-instantiability
  private CollectionUtils() { /* empty block */ }

  public static <T> List<T> toList(Iterable<T> iterable) {
    if(iterable == null)
      return null;
    
    List<T> list = new ArrayList<T>();
    for(T element : iterable)
      list.add(element);
    
    return list;
  }
}
