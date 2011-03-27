package com.googlecode.transmuter.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.googlecode.transmuter.converter.Converter;
import com.googlecode.transmuter.converter.ConverterType;
import com.googlecode.transmuter.core.exception.NoCompatibleConvertersFoundException;
import com.googlecode.transmuter.core.exception.TooManyConvertersFoundException;
import com.googlecode.transmuter.util.CollectionUtils;

/**
 * A simple {@linkplain ConverterSelector converter selector}.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class BasicConverterSelector implements ConverterSelector {
  /**
   * Does a simple search in the given converters for a type match. 
   * <p>
   * If an exact match is found, its converter is returned; if not, a compatible converter will be searched for. 
   * Exceptions are thrown if no compatible converter is found, or more than one is found, since this implementation
   * does not know which to choose.
   * 
   * @param type the type to match.
   * @param converters where to look for a matching converter.
   * @return a converter with the same type or a compatible one.
   * @throws NoCompatibleConvertersFoundException if no converters compatible with the given type were found.
   * @throws TooManyConvertersFoundException if no exact match for the given type is found, and there is more than one 
   *         compatible converter.
   */
  @Override
  public Converter getConverterFor(ConverterType type, Iterable<? extends Converter> converters) 
  throws NoCompatibleConvertersFoundException, TooManyConvertersFoundException {
    if(type == null || converters == null || ! converters.iterator().hasNext())
      throw new NoCompatibleConvertersFoundException(type, CollectionUtils.toList(converters));
    
    List<Converter> compatibles = new ArrayList<Converter>();
    for(Converter c : converters) {
      if(c == null)
        continue;
      
      if(type.equals(c.getType())) // found a perfect match!
        return c;
      
      if(c.getType().isAssignableFrom(type)) { // this may do
        compatibles.add(c);
        continue;
      }
    }
    
    if(compatibles.size() == 1) // found only one compatible, use it
      return compatibles.get(0);
    
    if(compatibles.isEmpty()) // no compatibles found, blow up
      throw new NoCompatibleConvertersFoundException(type, CollectionUtils.toList(converters));
    
    // lots of compatibles found, how to pick only one?
    throw new TooManyConvertersFoundException(type, compatibles);
  }

  /**
   * Does a simple search in the given converter map for a type match. 
   * <p>
   * If an exact match is found, its converter is returned; if not, a compatible converter will be searched for. 
   * Exceptions are thrown if no compatible converter is found, or more than one is found, since this implementation
   * does not know which to choose.
   * 
   * @param type the type to match.
   * @param map where to look for a matching converter.
   * @return a converter with the same type or a compatible one.
   * @throws NoCompatibleConvertersFoundException if no converters compatible with the given type were found.
   * @throws TooManyConvertersFoundException if no exact match for the given type is found, and there is more than one 
   *         compatible converter.
   */
  @SuppressWarnings("unchecked")
  @Override
  public Converter getConverterFor(ConverterType type, Map<? extends ConverterType, ? extends Converter> map)
      throws NoCompatibleConvertersFoundException, TooManyConvertersFoundException {
    if(type == null || map == null || map.isEmpty())
      throw new NoCompatibleConvertersFoundException(type, Collections.EMPTY_LIST);
    
    if(map.containsKey(type)) // found a perfect match!
      return map.get(type);
    
    List<Converter> compatibles = new ArrayList<Converter>();
    for(Entry<? extends ConverterType, ? extends Converter> entry : map.entrySet()) {
      if(entry.getKey() == null)
        continue;
      
      if(entry.getKey().isAssignableFrom(type)) { // this may do
        compatibles.add(entry.getValue());
        continue;
      }
    }
    
    if(compatibles.size() == 1) // found only one compatible, use it
      return compatibles.get(0);
    
    if(compatibles.isEmpty()) // no compatibles found, blow up
      throw new NoCompatibleConvertersFoundException(type, map.values());
    
    // lots of compatibles found, how to pick only one?
    throw new TooManyConvertersFoundException(type, compatibles);
  }
}