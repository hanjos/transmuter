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
public class Notification implements Iterable<Exception> {
  private static final List<Exception> EMPTY_LIST = Collections.emptyList();
  
  private List<Exception> errors;
  
  // since Collections.unmodifiableList is backed by the actual instance, we only need to calculate it once
  private List<Exception> unmodifiableErrors;

  // constructors
  /**
   * Builds a new Notification instance with no reported errors.
   */
  public Notification() {
    errors = new ArrayList<Exception>();
    unmodifiableErrors = Collections.unmodifiableList(errors);
  }

  // operations
  /**
   * Stores the given errors in this notification. {@code null} values are ignored.
   * 
   * @param _errors the reported errors. 
   * @return this instance.
   */
  public Notification add(Exception... _errors) {
    return add((_errors != null) ? Arrays.asList(_errors) : EMPTY_LIST);
  }
  
  /**
   * Stores the given errors in this notification. {@code null} values are ignored.
   * 
   * @param _errors an iterable object holding the reported errors. 
   * @return this instance.
   */
  public Notification add(Iterable<? extends Exception> _errors) {
    if(_errors == null)
      return this;
    
    for(Exception error : _errors)
      if(error != null)
        errors.add(error);
    
    return this;
  }
  
  /**
   * Returns an iterator for transversal of all the errors present in this instance. The iterator is unmodifiable, so
   * one cannot alter this instance's internal records with it.
   * 
   * @return an iterator for transversal of all the errors present in this instance.
   */
  @Override
  public Iterator<Exception> iterator() {
    return getErrors().iterator();
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
   * Returns an unmodifiable list of all errors reported to this instance. The returned list is backed by the internal
   * one, so additional errors reported in this notification will show up in the returned list.
   * 
   * @return an unmodifiable list of all errors reported to this instance.
   */
  public List<Exception> getErrors() {
    return unmodifiableErrors;
  }
}
