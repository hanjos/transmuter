package com.googlecode.transmuter.util.exception;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.googlecode.transmuter.util.StringUtils;

/**
 * The superclass of exceptions which wrap a list of exceptions. Meant to be used when an operation may fail for 
 * multiple reasons, and one wishes to report them all instead of only the first detected. 
 * 
 *  @author Humberto S. N. dos Anjos
 */
public class MultipleCausesException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  protected static final List<Exception> EMPTY_EXCEPTION_LIST = Arrays.asList(new Exception[0]);
  
  private static String buildMessage(List<? extends Exception> causes) {
    if(causes.isEmpty())
      return "";
    
    return "Multiple exceptions found:\n    " + StringUtils.concatenate(";\n    ", causes);
  }

  private List<? extends Exception> causes;
  
  /**
   * Builds a new instance.
   * 
   * @param causes the exceptions to be bundled.
   */
  public MultipleCausesException(Exception... causes) {
    this((causes != null) ? Arrays.asList(causes) : EMPTY_EXCEPTION_LIST);
  }
  
  /**
   * Builds a new instance.
   * 
   * @param causes the exceptions to be bundled.
   */
  public MultipleCausesException(List<? extends Exception> causes) {
    super(buildMessage((causes != null) ? causes : EMPTY_EXCEPTION_LIST));
    
    this.causes = Collections.unmodifiableList((causes != null) ? causes : EMPTY_EXCEPTION_LIST);
  }
  
  /**
   * Builds a new instance.
   * 
   * @param message the message to be shown. 
   * @param causes the exceptions to be bundled.
   */
  public MultipleCausesException(String message, List<? extends Exception> causes) {
    super(message);
    
    this.causes = Collections.unmodifiableList((causes != null) ? causes : EMPTY_EXCEPTION_LIST);
  }

  /**
   * Returns the bundled exceptions.
   * 
   * @return the bundled exceptions.
   */
  public List<? extends Exception> getCauses() {
    return causes;
  }
  
  /**
   * Returns the first bundled exception, or {@code null} if none were given.
   * @return the first bundled exception, or {@code null} if none were given.
   */
  @Override
  public Throwable getCause() {
    return causes.isEmpty() ? null : causes.get(0); 
  }
}
