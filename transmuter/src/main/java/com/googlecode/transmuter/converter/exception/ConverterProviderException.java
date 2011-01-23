package com.googlecode.transmuter.converter.exception;

import java.util.Collection;

import com.googlecode.transmuter.converter.Converts;
import com.googlecode.transmuter.util.exception.MultipleCausesException;

/**
 * Intended to be thrown on errors from converter providers, such as {@link Converts.EagerProvider}, which will be bundled 
 * up in this exception.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class ConverterProviderException extends MultipleCausesException {
  private static final long serialVersionUID = 1L;

  public ConverterProviderException(Exception... causes) {
    super(causes);
  }

  public ConverterProviderException(Collection<? extends Exception> causes) {
    super(causes);
  }

  public ConverterProviderException(String message, Collection<? extends Exception> causes) {
    super(message, causes);
  }
}
