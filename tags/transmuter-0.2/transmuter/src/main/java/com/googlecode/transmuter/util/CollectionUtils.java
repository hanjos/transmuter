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

  /**
   * Accumulates all objects from the given iterable in a list.
   * @param iterable holds an iterator with an unknown number of objects.
   * @param <T> the type of the objects returned from {@code iterable}
   * @return a list holding all objects from {@code iterable}. 
   */
  public static <T> List<T> toList(Iterable<T> iterable) {
    if(iterable == null)
      return null;
    
    List<T> list = new ArrayList<T>();
    for(T element : iterable)
      list.add(element);
    
    return list;
  }
}
