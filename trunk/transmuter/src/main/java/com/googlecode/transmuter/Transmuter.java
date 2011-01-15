package com.googlecode.transmuter;

import static com.googlecode.transmuter.util.ObjectUtils.areEqual;
import static com.googlecode.transmuter.util.ObjectUtils.classOf;
import static com.googlecode.transmuter.util.ObjectUtils.nonNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.googlecode.transmuter.converter.Converter;
import com.googlecode.transmuter.converter.ConverterType;
import com.googlecode.transmuter.converter.exception.ConverterTypeIncompatibleWithConverterException;
import com.googlecode.transmuter.converter.exception.InvocationException;
import com.googlecode.transmuter.exception.ConverterCollisionException;
import com.googlecode.transmuter.exception.ConverterRegistrationException;
import com.googlecode.transmuter.exception.NoCompatibleConvertersFoundException;
import com.googlecode.transmuter.exception.NotificationNotFoundException;
import com.googlecode.transmuter.exception.TooManyConvertersFoundException;
import com.googlecode.transmuter.type.TypeToken;
import com.googlecode.transmuter.util.Notification;
import com.googlecode.transmuter.util.exception.MultipleCausesException;
import com.googlecode.transmuter.util.exception.ObjectInstantiationException;

/**
 * The main object in the library. A transmuter provides a centralized type conversion operation, using previously 
 * registered methods as converters. Objects containing converter methods (public methods marked with the 
 * {@link Converts} annotation) may register them in the transmuter. These methods may later be used as converters 
 * when a conversion operation is made with matching types.
 * <p>
 * There cannot be more than one registered converter with the exact same {@linkplain ConverterType type}; the 
 * existing one must be explicitly unregistered before the new one is included.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class Transmuter {
  /**
   * A map used for converter registration, validating the prospective mapping before the actual insertion.
   * 
   *  @author Humberto S. N. dos Anjos
   */
  protected static class ConverterMap extends HashMap<ConverterType, Converter> {
    private static final long serialVersionUID = 1L;

    public ConverterMap() { /* empty block */ }

    /**
     * Validates the converter type and the converter (using {@link #validatePut(ConverterType, Converter) validatePut}) 
     * before insertion, throwing an exception if a problem is found.
     * <p>
     * In particular, converter cannot be overwritten; they must be specifically removed from this map before a new 
     * {@code put} operation with the given converter type can be done.
     * 
     * @return {@code null} if there was no previous converter for {@code converterType}, or {@code converter} if 
     * it was already paired with {@code converterType}.
     * @throws RuntimeException all exceptions thrown by {@link #validatePut(ConverterType, Converter)}. 
     * @see #validatePut(ConverterType, Converter)
     */
    @Override
    public Converter put(ConverterType converterType, Converter converter) {
      return validatePut(converterType, converter) 
           ? converter
           : super.put(converterType, converter);
    }

    /**
     * Checks if the converter type and the converter can be stored in this map.
     * <p>
     * The restrictions implemented here are:
     * <ul>
     * <li>neither {@code converterType} nor {@code converter} can be {@code null}.</li>
     * <li>a converter type must be obtainable from {@code converter}.</li>
     * <li>{@code converterType} must be assignable from {@code converter}'s converter type.</li>
     * <li>this map must not have {@code converterType} associated to a converter other than {@code converter}.</li>
     * </ul>
     * 
     * @param converterType a converter type.
     * @param converter a converter.
     * @return {@code true} if {@code converterType} is already associated with {@code converter}, or {@code false} 
     * if {@code converterType} is not associated to a converter here.
     * @throws IllegalArgumentException if either {@code converterType} or {@code converter} are {@code null}.
     * @throws ConverterTypeIncompatibleWithConverterException if {@code converterType} and {@code converter} are not 
     * compatible.
     * @throws ObjectInstantiationException if a converter type cannot be obtained from {@code converter}.
     * @throws ConverterCollisionException if this map already has a different converter associated to 
     * {@code converterType}.
     * @see #checkForCompatibility(ConverterType, Converter)
     * @see #checkForCollision(ConverterType, Converter)
     */
    protected boolean validatePut(ConverterType converterType, Converter converter) {
      // check if the carpet matches the curtains
      checkForCompatibility(converterType, converter);
      
      // check for collisions here
      return checkForCollision(converterType, converter);
    }

    /**
     * Checks if the converter type and the converter are mutually compatible.
     * <p>
     * The restrictions are:
     * <ul>
     * <li>neither {@code converterType} nor {@code converter} can be {@code null}.</li>
     * <li>{@code converterType} must be assignable from {@code converter}'s converter type.</li>
     * </ul>
     * 
     * @param converterType a converter type.
     * @param converter a converter.
     * @throws IllegalArgumentException if either {@code converterType} or {@code converter} are {@code null}.
     * @throws ConverterTypeIncompatibleWithConverterException if {@code converterType} and {@code converter} are not 
     * compatible.
     * @see ConverterType#isAssignableFrom(ConverterType)
     */
    protected void checkForCompatibility(ConverterType converterType, Converter converter) 
    throws ConverterTypeIncompatibleWithConverterException {
      nonNull(converterType, "converterType"); 
      nonNull(converter, "converter");
      
      if(! converterType.isAssignableFrom(converter.getType()))
        throw new ConverterTypeIncompatibleWithConverterException(converterType, converter);
    }

    /**
     * Checks if the converter type and the converter can be stored in this map.
     * 
     * @param converterType a converter type.
     * @param converter a converter.
     * @return {@code true} if {@code converterType} is already associated with {@code converter} in this map, or 
     * {@code false} if there's no converter.
     * @throws IllegalArgumentException if either {@code converterType}, {@code converter} or {@code map} are 
     * {@code null}.
     * @throws ConverterCollisionException if this map already has a different converter associated to 
     * {@code converterType}.
     * @see #checkMapForCollision(ConverterType, Converter, Map)
     */
    protected boolean checkForCollision(ConverterType converterType, Converter converter) 
    throws ConverterCollisionException {
      return checkMapForCollision(converterType, converter, this);
    }
    
    /**
     * Checks if the converter type and the converter can be stored in the map.
     * <p>
     * The restrictions are:
     * <ul>
     * <li>neither {@code converterType} nor {@code converter} nor {@code map} can be {@code null}.</li>
     * <li>{@code map} must not have {@code converterType} associated to a converter other than {@code converter}.</li>
     * </ul>
     * 
     * @param converterType a converter type.
     * @param converter a converter.
     * @param map a map.
     * @return {@code true} if {@code converterType} is already associated with {@code converter} in {@code map}, or 
     * {@code false} if there's no converter in {@code map}.
     * @throws IllegalArgumentException if either {@code converterType}, {@code converter} or {@code map} are 
     * {@code null}.
     * @throws ConverterCollisionException if this map already has a different converter associated to the given 
     * converter type.
     */
    protected static boolean checkMapForCollision(ConverterType converterType, Converter converter, 
        Map<? extends ConverterType, ? extends Converter> map) throws ConverterCollisionException {
      nonNull(converterType, "converterType");
      nonNull(converter, "converter");
      nonNull(map, "map");
      
      if(! map.containsKey(converterType))
        return false;
      
      if(areEqual(converter, map.get(converterType)))
        return true;
      
      // converter collision, throw up
      throw new ConverterCollisionException(converterType, converter, map.get(converterType));
    }
    
    /**
     * Attempts to add the converters in the given map to this map, doing nothing if the given map is null or empty. 
     * All entries are validated before actual insertion.
     * 
     * @see #validatePut(ConverterType, Converter)
     */
    @Override
    public void putAll(Map<? extends ConverterType, ? extends Converter> map) {
      if(map == null || map.isEmpty())
        return;
      
      for(Entry<? extends ConverterType, ? extends Converter> entry : map.entrySet())
        validatePut(entry.getKey(), entry.getValue());
      
      for(Entry<? extends ConverterType, ? extends Converter> entry : map.entrySet())
        super.put(entry.getKey(), entry.getValue());
    }
    
    /**
     * Ensures that a {@code null} key is never contained in this map.
     */
    @Override
    public boolean containsKey(Object key) {
      if(key == null)
        return false;
      
      return super.containsKey(key);
    }
    
    /**
     * Ensures that a {@code null} key is never contained in this map, so invoking this operation on {@code null} 
     * does nothing and returns {@code null}.
     */
    @Override
    public Converter remove(Object key) {
      if(key == null)
        return null;
      
      return super.remove(key);
    }
  }
  
  /**
   * A {@link ConverterMap} which 
   * {@linkplain #checkMapForCollision(ConverterType, Converter, Map) checks for collisions} against itself and a 
   * master map. 
   * 
   * @author Humberto S. N. dos Anjos
   */
  protected static class DependentConverterMap extends ConverterMap {
    private static final long serialVersionUID = 1L;

    private Map<? extends ConverterType, ? extends Converter> masterMap;

    /**
     * Creates a new {@link DependentConverterMap} object, which will be backed by {@code masterMap}.
     * 
     * @param masterMap the master map which backs this map.
     * @throws IllegalArgumentException if {@code masterMap} is {@code null}.
     */
    public DependentConverterMap(Map<? extends ConverterType, ? extends Converter> masterMap) {
      this.masterMap = nonNull(masterMap);
    }
    
    /**
     * Checks for collision in this and in the master map.
     */
    @Override
    protected boolean checkForCollision(ConverterType converterType, Converter converter) 
    throws ConverterCollisionException {
      return super.checkForCollision(converterType, converter)
          || super.checkMapForCollision(converterType, converter, getMasterMap());
    }
    
    /**
     * Returns the map which backs this instance.
     * 
     * @return the map which backs this instance.
     */
    public Map<? extends ConverterType, ? extends Converter> getMasterMap() {
      return masterMap;
    }
  }
  
  private Map<ConverterType, Converter> converterMap;
  
  /**
   * Constructs a new {@link Transmuter}.
   */
  public Transmuter() {
    converterMap = new ConverterMap();
  }
  
  // operations
  /**
   * Performs a conversion, taking {@code from} and generating a new object of type {@code toType}.
   * 
   * @param from the object to convert.
   * @param toType the type of the converted object.
   * @param <From> the input type of the conversion.
   * @param <To> the output type of the conversion.
   * @return an instance of {@code toType}.
   * @throws NoCompatibleConvertersFoundException if no converters for {@code from}'s type to {@code toType} were found.
   * @throws TooManyConvertersFoundException if more than one converter for {@code from}'s type to {@code toType} was found.
   * @throws IllegalArgumentException if {@code from} or {@code toType} is null (or {@code void} for {@code toType}). 
   * @throws InvocationException if there was an error during the converter's invocation.
   */
  public <From, To> To convert(From from, Class<To> toType) {
    return convert(from, TypeToken.get(toType));
  }
  
  /**
   * Performs a conversion, taking {@code from} and generating a new object of type {@code toType}.
   * 
   * @param from the object to convert.
   * @param toType the type of the converted object.
   * @param <From> the input type of the conversion.
   * @param <To> the output type of the conversion.
   * @return an instance of {@code toType}.
   * @throws NoCompatibleConvertersFoundException if no converters for {@code from}'s type to {@code toType} were found.
   * @throws TooManyConvertersFoundException if more than one converter for {@code from}'s type to {@code toType} was found.
   * @throws IllegalArgumentException if {@code from} or {@code toType} is null (or {@code void} for {@code toType}). 
   * @throws InvocationException if there was an error during the converter's invocation.
   */
  @SuppressWarnings("unchecked")
  public <From, To> To convert(From from, TypeToken<To> toType) {
    return convert(from, TypeToken.get((Class<From>) classOf(from)), toType);
  }

  /**
   * Performs a conversion, taking {@code from} (which is considered to be of type {@code fromType} for the purposes
   * of this operation) and generating a new object of type {@code toType}. 
   * 
   * @param from the object to convert.
   * @param fromType the type of the object to convert.
   * @param toType the type of the converted object.
   * @param <From> the input type of the conversion.
   * @param <To> the output type of the conversion.
   * @param <SubFrom> the actual type of the object to convert. Used only to ensure that {@code from} is a subtype of 
   * {@code fromType}.
   * @return an instance of {@code toType}.
   * @throws NoCompatibleConvertersFoundException if no converters for {@code fromType} to {@code toType} were found.
   * @throws TooManyConvertersFoundException if more than one converter for {@code fromType} to {@code toType} was found.
   * @throws IllegalArgumentException if {@code fromType} or {@code toType} is null or void. 
   * @throws InvocationException if there was an error during the converter's invocation.
   */
  public <From, To, SubFrom extends From> To convert(SubFrom from, Class<From> fromType, Class<To> toType) {
    return convert(from, TypeToken.get(fromType), TypeToken.get(toType));
  }
  
  /**
   * Performs a conversion, taking {@code from} (which is considered to be of type {@code fromType} for the purposes
   * of this operation) and generating a new object of type {@code toType}. 
   * 
   * @param from the object to convert.
   * @param fromType the type of the object to convert.
   * @param toType the type of the converted object.
   * @param <From> the input type of the conversion.
   * @param <To> the output type of the conversion.
   * @param <SubFrom> the actual type of the object to convert. Used only to ensure that {@code from} is a subtype of 
   * {@code fromType}.
   * @return an instance of {@code toType}.
   * @throws NoCompatibleConvertersFoundException if no converters for {@code fromType} to {@code toType} were found.
   * @throws TooManyConvertersFoundException if more than one converter for {@code fromType} to {@code toType} was found.
   * @throws IllegalArgumentException if {@code fromType} or {@code toType} is null or void. 
   * @throws InvocationException if there was an error during the converter's invocation.
   */
  @SuppressWarnings("unchecked")
  public <From, To, SubFrom extends From> To convert(SubFrom from, TypeToken<From> fromType, TypeToken<To> toType) {
    return (To) convertRaw(from, fromType, toType);
  }

  /**
   * Performs a conversion, taking {@code from} and generating a new object of type {@code toType}.
   * 
   * This method doesn't use generics for compile-time checking, returning the result as a raw {@link Object}. 
   * So, due to erasure-imposed limitations, {@code from}'s runtime class will be considered as the input type.
   * This means that both invocations below:
   * 
   * <pre>
   * convertRaw(new ArrayList&lt;String&gt;(), TypeToken.STRING);
   * convertRaw(new ArrayList&lt;Map&lt;java.util.Date, Set&lt;Thread&gt;&gt;&gt;&gt;(), TypeToken.STRING);
   * </pre>
   * 
   * will attempt to use the same converter.
   * 
   * @param from the object to convert.
   * @param toType the type of the converted object.
   * @return an instance of {@code toType}.
   * @throws NoCompatibleConvertersFoundException if no converters for {@code from}'s type to {@code toType} were found.
   * @throws TooManyConvertersFoundException if more than one converter for {@code from}'s type to {@code toType} was found.
   * @throws IllegalArgumentException if {@code from} or {@code toType} is null (or {@code void} for {@code toType}). 
   * @throws InvocationException if there was an error during the converter's invocation.
   */
  protected Object convertRaw(Object from, TypeToken<?> toType) {
    return convertRaw(from, TypeToken.get(classOf(from)), toType);
  }
  
  /**
   * Performs a conversion, taking {@code from} (which is considered to be of type {@code fromType} for the purposes
   * of this operation) and generating a new object of type {@code toType}.
   * 
   * This method doesn't use generics for compile-time checking, returning the result as a raw {@link Object}. 
   * 
   * @param from the object to convert.
   * @param fromType the type of the object to convert.
   * @param toType the type of the converted object.
   * @return an instance of {@code toType}.
   * @throws NoCompatibleConvertersFoundException if no converters for {@code fromType} to {@code toType} were found.
   * @throws TooManyConvertersFoundException if more than one converter for {@code fromType} to {@code toType} was found.
   * @throws IllegalArgumentException if {@code fromType} or {@code toType} is null or void. 
   * @throws InvocationException if there was an error during the converter's invocation.
   */
  protected Object convertRaw(Object from, TypeToken<?> fromType, TypeToken<?> toType) 
  throws NoCompatibleConvertersFoundException, TooManyConvertersFoundException, IllegalArgumentException, 
  InvocationException {
    return getConverterFor(new ConverterType(fromType, toType)).invoke(from);
  }
  
  /**
   * Attempts to register all given {@linkplain Converter converters} in this instance, keyed by their 
   * {@linkplain ConverterType types}. Does nothing if the given iterable is {@code null}.
   * <p>
   * Delegates to {@link #tryRegister(Iterable) tryRegister} for the actual legwork, so subclasses which 
   * wish to alter the registration algorithm should override it instead of this method.
   * <p> 
   * This method simply checks {@code tryRegister}'s final report to determine if an exception must be thrown. 
   * The exception will hold all the problems found with the given arguments. In that case, no converters from 
   * {@code converters} will be registered, even if they're valid.
   * 
   * @param converters a bundle of converters
   * @throws ConverterRegistrationException if there is some error during the operation.
   * @see #tryRegister(Iterable)
   */
  public void register(Iterable<? extends Converter> converters) throws ConverterRegistrationException {
    try {
      Notification notification = tryRegister(converters);
      
      if(notification == null) // no notification given
        throw new ConverterRegistrationException(new NotificationNotFoundException());
      
      if(notification.hasErrors())
        throw new ConverterRegistrationException(notification.getErrors());
    } catch(ConverterRegistrationException e) {
      throw e;
    } catch(MultipleCausesException e) {
      // call me a paranoid, but just in case
      throw new ObjectInstantiationException(getClass(), e.getCauses());
    } catch(Exception e) {
      // should never happen :P
      throw new ObjectInstantiationException(getClass(), e);
    }
  }
  
  /**
   * Registers all given {@linkplain Converter converters} in this instance, keyed by their 
   * {@linkplain ConverterType types}. Does nothing if the given iterable is {@code null}.
   * <p>
   * This method will iterate through all the converters and {@linkplain DependentConverterMap check} if there is no 
   * registered converter with the same type, registering all the converters in one fell swoop if no problem is found.
   * <p>
   * This method returns a {@link Notification} object, which accumulates any problems verified here and reports the 
   * final status of the registration.
   * 
   * @param converters a bundle of converters
   * @return a {@link Notification} with all errors found during registration. Should not be null.
   * @see DependentConverterMap
   */
  protected Notification tryRegister(Iterable<? extends Converter> converters) {
    Notification notification = new Notification();
    
    if(converters == null)
      return notification;
    
    // XXX can't use foreach here, since the hasNext() and next() operations themselves may fail
    try {
      Map<ConverterType, Converter> temp = new DependentConverterMap(getConverterMap());
      Iterator<? extends Converter> iterator = converters.iterator();
    
      // if hasNext() fails, there's no iterating at all here; snitch and move on
      while(iterator.hasNext()) {
        try {
          // an individual next() may fail, but not necessarily all them will; 
          // keep going and store all mishaps 
          Converter converter = iterator.next();
          temp.put(converter.getType(), converter);
        } catch(MultipleCausesException e) {
          notification.add(e.getCauses());
        } catch(Exception e) {
          notification.add(e);
        }
      }
      
      if(notification.hasErrors()) // some next()s blew up
        return notification;
      
      // everything worked so far...
      getConverterMap().putAll(temp);
    } catch(MultipleCausesException e) {
      notification.add(e.getCauses());
    } catch(Exception e) {
      notification.add(e);
    }
    
    return notification;
  }
  
  /**
   * Checks if there is a registered converter with this exact converter type. 
   * 
   * @param fromType the input type.
   * @param toType the output type.
   * @return {@code true} if there is a converter for the given pairing.
   */
  public boolean isRegistered(Type fromType, Type toType) {
    return isRegistered(new ConverterType(fromType, toType));
  }
  
  /**
   * Checks if there is a registered converter with this exact converter type.
   * 
   * @param fromType the input type.
   * @param toType the output type.
   * @return {@code true} if there is a converter for the given pairing.
   */
  public boolean isRegistered(TypeToken<?> fromType, TypeToken<?> toType) {
    return isRegistered(new ConverterType(fromType, toType));
  }
  
  /**
   * Checks if there is a registered converter with this exact converter type.
   * 
   * @param converterType a converter type.
   * @return {@code true} if there is a converter for the given converter type.
   */
  public boolean isRegistered(ConverterType converterType) {
    return getConverterMap().containsKey(converterType);
  }
  
  /**
   * Unregisters the converter for the converter type represented by the given types. 
   * Does nothing if no such converter exists. 
   * 
   * @param fromType the input type.
   * @param toType the output type.
   */
  public void unregister(Type fromType, Type toType) {
    if(nonNullOrVoid(fromType) || nonNullOrVoid(toType))
      return;
    
    unregister(new ConverterType(fromType, toType));
  }

  /**
   * Unregisters the converter for the converter type represented by the given types. 
   * Does nothing if no such converter exists. 
   * 
   * @param fromType the input type.
   * @param toType the output type.
   */
  public void unregister(TypeToken<?> fromType, TypeToken<?> toType) {
    if(nonNullOrVoid(fromType) || nonNullOrVoid(toType))
      return;
    
    unregister(new ConverterType(fromType, toType));
  }
  
  private boolean nonNullOrVoid(Type type) {
    return type == null || TypeToken.ValueType.VOID.matches(type);
  }
  
  private boolean nonNullOrVoid(TypeToken<?> type) {
    return type == null || TypeToken.ValueType.VOID.matches(type);
  }
  
  /**
   * Unregisters the converter for the given converter type. Does nothing if no such converter exists. 
   * 
   * @param converterType a converter type.
   * @return the converter previously associated with the given converter type, or {@code null} if there was 
   * no such converter.
   */
  public Converter unregister(ConverterType converterType) {
    return getConverterMap().remove(converterType);
  }
  
  // helper methods
  /**
   * Attempts to return a converter compatible with the given converter type. 
   * <p>
   * There can only be one exact match registered in the transmuter, which will be returned here; lacking that, 
   * a compatible converter will be looked for. An exception will be thrown if no converter is found, or if more than 
   * one compatible (non-exact match) converter is found, since this method cannot decide which should be returned.
   * 
   * @param converterType a converter type.
   * @return a converter compatible with the given converter type. May not an exact match.
   * @throws NoCompatibleConvertersFoundException no compatible converters were found.
   * @throws TooManyConvertersFoundException more than one compatible converter was found.
   */
  protected Converter getConverterFor(ConverterType converterType) 
  throws NoCompatibleConvertersFoundException, TooManyConvertersFoundException {
    if(converterType == null)
      throw new NoCompatibleConvertersFoundException(converterType);
    
    // TODO determine a search algorithm for a "most compatible" converter type
    // ??? parameterize it?
    Converter converter = getConverterMap().get(converterType);
    if(converter != null)
      return converter;
    
    List<Converter> compatibles = getCompatibleConvertersFor(converterType);
    
    if(compatibles.isEmpty())
      throw new NoCompatibleConvertersFoundException(converterType);
    
    if(compatibles.size() > 1)
      throw new TooManyConvertersFoundException(converterType, compatibles);
    
    return compatibles.get(0);
  }
  
  /**
   * Returns a list of all the compatible converters found in this instance. Will be empty if the given converter type
   * is {@code null} or has no compatible converters. 
   * 
   * @param converterType a converter type.
   * @return a list with all the compatible converters found.  
   */
  protected List<Converter> getCompatibleConvertersFor(ConverterType converterType) {
    List<Converter> compatibles = new ArrayList<Converter>();
    if(converterType == null)
      return compatibles;
    
    for(Entry<ConverterType, Converter> entry : getConverterMap().entrySet()) {
      if(entry.getKey().isAssignableFrom(converterType))
        compatibles.add(entry.getValue());
    }
    
    return compatibles;
  } 
  
  // properties
  /**
   * Returns a map holding all the registered converters, keyed by their converter types. This map is backed by 
   * this instance, i.e. changes made in this transmuter are seen in the map and vice-versa.
   * 
   * @return a map holding all the registered converters, keyed by their converter types and backed by this instance.
   */
  protected Map<ConverterType, Converter> getConverterMap() {
    return converterMap;
  }
}
