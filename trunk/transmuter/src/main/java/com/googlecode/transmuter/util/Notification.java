package com.googlecode.transmuter.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Collects information about errors, so that multiple errors can be accounted for in a single pass 
 * instead of failing on the first error.
 * 
 * @author Humberto S. N. dos Anjos
 */
// TODO improve prose
public class Notification implements Iterable<Exception> {
  private static final List<Exception> EMPTY_LIST = Collections.emptyList();
  
  private List<Exception> errors;

  // constructors
  /**
   * Builds a new Notification instance with no reported errors.
   */
  public Notification() {
    this(null);
  }
  
  /**
   * Builds a new Notification instance which copies all reported errors from the given notification.
   * 
   * @param n a previously built notification. If {@code null}, no errors are inherited, and this instance 
   * starts fresh.
   */
  public Notification(Notification n) {
    if(n != null)
      errors = new ArrayList<Exception>(n.errors);
    else
      errors = new ArrayList<Exception>();
  }

  // operations
  /**
   * Stores the given errors in this notification. {@code null} values are ignored.
   * 
   * @param _errors the reported errors. 
   * @return this instance.
   */
  public Notification report(Exception... _errors) {
    return report((_errors != null) ? Arrays.asList(_errors) : EMPTY_LIST);
  }
  
  /**
   * Stores the given errors in this notification. {@code null} values are ignored.
   * 
   * @param _errors an iterable object holding the reported errors. 
   * @return this instance.
   */
  public Notification report(Iterable<? extends Exception> _errors) {
    if(_errors == null)
      return this;
    
    for(Exception error : _errors)
      if(error != null)
        errors.add(error);
    
    return this;
  }
  
  /**
   * Returns an iterator for transversal of all the errors present in this instance.
   * 
   * @return an iterator for transversal of all the errors present in this instance.
   */
  @Override
  public Iterator<Exception> iterator() {
    return errors.iterator();
  }
  
  // properties
  /**
   * Returns {@code true} if no errors were reported.
   * 
   * @return {@code true} if no errors were reported.
   */
  public boolean isOk() {
    return errors.isEmpty();
  }
  
  /**
   * Returns {@code true} if any errors were reported.
   * 
   * @return {@code true} if any errors were reported.
   */
  public boolean hasErrors() {
    return ! isOk();
  }
  
  /**
   * Returns a list of all errors reported to this instance.
   * 
   * @return a list of all errors reported to this instance.
   */
  public List<Exception> getErrors() {
    return Collections.unmodifiableList(errors);
  }
}
