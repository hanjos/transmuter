package com.googlecode.transmuter.core;

import com.googlecode.transmuter.converter.Converter;
import com.googlecode.transmuter.converter.ConverterType;
import com.googlecode.transmuter.core.exception.NoCompatibleConvertersFoundException;
import com.googlecode.transmuter.core.exception.TooManyConvertersFoundException;

/**
 * A converter selector provides an algorithm for picking a converter of a given type (or compatible with said 
 * given type) out of a group of converters.
 *  
 * @author Humberto S. N. dos Anjos
 */
public interface ConverterSelector {
  /**
   * Searches for a converter which either exactly matches or is compatible with the given converter type using 
   * the given converters. 
   * <p>
   * Lacking an exact match, a compatible converter will be searched for. An exception will be thrown if no converter 
   * is found, or if more than one compatible (non-exact match) converter is found and this selector cannot decide 
   * which should be returned.
   * 
   * @param type a converter type.
   * @param converters where to look for a matching converter.
   * @return a converter compatible with the given converter type. May not an exact match.
   * @throws NoCompatibleConvertersFoundException no compatible converters were found.
   * @throws TooManyConvertersFoundException more than one compatible converter was found, and this selector was unable
   * to decide which should be picked.
   */
  Converter getConverterFor(ConverterType type, Iterable<? extends Converter> converters) 
      throws NoCompatibleConvertersFoundException, TooManyConvertersFoundException;
}
