package com.googlecode.transmuter.core;

import static com.googlecode.transmuter.util.ObjectUtils.classOf;
import static com.googlecode.transmuter.util.ObjectUtils.nonNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.googlecode.transmuter.converter.Converter;
import com.googlecode.transmuter.converter.ConverterType;
import com.googlecode.transmuter.converter.exception.InvocationException;
import com.googlecode.transmuter.core.util.ConverterMap;
import com.googlecode.transmuter.core.util.DependentConverterMap;
import com.googlecode.transmuter.exception.ConverterRegistrationException;
import com.googlecode.transmuter.exception.NoCompatibleConvertersFoundException;
import com.googlecode.transmuter.exception.TooManyConvertersFoundException;
import com.googlecode.transmuter.type.TypeToken;
import com.googlecode.transmuter.util.CollectionUtils;
import com.googlecode.transmuter.util.Notification;
import com.googlecode.transmuter.util.exception.MultipleCausesException;
import com.googlecode.transmuter.util.exception.NotificationNotFoundException;

/**
 * The main object in the library. A transmuter provides a centralized type conversion operation, using registered 
 * converters. These converters may later be used when a conversion operation is made with matching types.
 * <p>
 * There cannot be more than one registered converter with the exact same {@linkplain ConverterType type}; the 
 * existing one must be explicitly unregistered before the new one is included.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class Transmuter {
  
  protected static class DefaultConverterSelector implements ConverterSelector {
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
  }
  
  protected static final ConverterSelector DEFAULT_SELECTOR = new DefaultConverterSelector();
  
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
   * <p>
   * Due to erasure-imposed limitations, {@code from}'s runtime class will be considered as the input type.
   * This means that all the invocations below:
   * 
   * <pre>
   * convert(new ArrayList&lt;String&gt;(), String.class);
   * convert(new ArrayList&lt;Map&lt;java.util.Date, Set&lt;Thread&gt;&gt;&gt;&gt;(), String.class);
   * convert(new ArrayList(), String.class);
   * </pre>
   * 
   * will attempt to use the same converter.
   * <p>
   * For parameterized input types, 
   * {@linkplain #convert(Object, TypeToken, TypeToken) the appropriate input type should be specified}.
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
   * @see #convert(Object, TypeToken, TypeToken)
   */
  public <From, To> To convert(From from, Class<To> toType) {
    return convert(from, TypeToken.get(toType));
  }
  
  /**
   * Performs a conversion, taking {@code from} and generating a new object of type {@code toType}.
   * <p>
   * Due to erasure-imposed limitations, {@code from}'s runtime class will be considered as the input type.
   * This means that all the invocations below:
   * 
   * <pre>
   * convert(new ArrayList&lt;String&gt;(), TypeToken.STRING);
   * convert(new ArrayList&lt;Map&lt;java.util.Date, Set&lt;Thread&gt;&gt;&gt;&gt;(), TypeToken.STRING);
   * convert(new ArrayList(), TypeToken.STRING);
   * </pre>
   * 
   * will attempt to use the same converter.
   * <p>
   * For parameterized input types, 
   * {@linkplain #convert(Object, TypeToken, TypeToken) the appropriate input type should be specified}.
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
   * @see #convert(Object, TypeToken, TypeToken)
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
   * <p>
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
   * <p>
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
      throw new ConverterRegistrationException(e.getCauses());
    } catch(Exception e) {
      // should never happen :P
      throw new ConverterRegistrationException(e);
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
    
      // if hasNext() fails, there's no iterating to do; snitch and move on
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
      
      if(notification.hasErrors()) // somebody blew up
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
    if(nullOrVoid(fromType) || nullOrVoid(toType))
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
    if(nullOrVoid(fromType) || nullOrVoid(toType))
      return;
    
    unregister(new ConverterType(fromType, toType));
  }
  
  // I could write a doc comment, but these two are quite self-explaining 
  private boolean nullOrVoid(Type type) {
    return type == null || TypeToken.ValueType.VOID.matches(type);
  }
  
  private boolean nullOrVoid(TypeToken<?> type) {
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
    return getConverterFor(converterType, DEFAULT_SELECTOR);
  }
  
  /**
   * Attempts to return a converter compatible with the given converter type using the given selector. 
   * <p>
   * There can only be one exact match registered in the transmuter, which will be returned here; lacking that, 
   * a compatible converter will be looked for. An exception will be thrown if no converter is found, or if more than 
   * one compatible (non-exact match) converter is found if the selector cannot decide which should be returned.
   * 
   * @param converterType a converter type.
   * @param selector a converter selector.
   * @return a converter compatible with the given converter type. May not an exact match.
   * @throws IllegalArgumentException {@code selector} was {@code null}.
   * @throws NoCompatibleConvertersFoundException no compatible converters were found.
   * @throws TooManyConvertersFoundException more than one compatible converter was found, and the selector was unable
   * to decide which should be picked.
   */
  protected Converter getConverterFor(ConverterType converterType, ConverterSelector selector) 
  throws IllegalArgumentException, NoCompatibleConvertersFoundException, TooManyConvertersFoundException {
    nonNull(selector, "selector");
    
    return selector.getConverterFor(converterType, getConverterMap().values());
  }
  
  // properties
  /**
   * Returns a map holding all the registered converters, keyed by their converter types. This map is backed by 
   * this instance, i.e. changes made in this transmuter are seen in the map and vice-versa.
   * 
   * @return a map holding all the registered converters, keyed by their converter types and backed by this instance.
   */
  public Map<ConverterType, Converter> getConverterMap() {
    return converterMap;
  }
}
