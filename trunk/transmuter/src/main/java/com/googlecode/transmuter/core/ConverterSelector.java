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
   * Searches for a converter in the given converters which either exactly matches or is compatible with the given 
   * type.  
   * 
   * @param type the type to match.
   * @param converters where to look for a matching converter.
   * @return a converter with the same type or a compatible one.
   * @throws NoCompatibleConvertersFoundException if no converters compatible with the given type were found.
   * @throws TooManyConvertersFoundException if no exact match for the given type is found, and there is more than one 
   *         compatible converter and no way to choose which is more appropriate.
   */
  Converter getConverterFor(ConverterType type, Iterable<? extends Converter> converters) 
      throws NoCompatibleConvertersFoundException, TooManyConvertersFoundException;
}
