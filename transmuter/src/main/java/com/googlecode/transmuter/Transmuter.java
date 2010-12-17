package com.googlecode.transmuter;

import static com.googlecode.transmuter.util.ObjectUtils.areEqual;
import static com.googlecode.transmuter.util.ObjectUtils.classOf;
import static com.googlecode.transmuter.util.ObjectUtils.nonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.googlecode.transmuter.converter.Binding;
import com.googlecode.transmuter.converter.ConverterType;
import com.googlecode.transmuter.converter.exception.BindingInvocationException;
import com.googlecode.transmuter.converter.exception.ConverterTypeIncompatibleWithBindingException;
import com.googlecode.transmuter.converter.exception.ConverterTypeInstantiationException;
import com.googlecode.transmuter.exception.ConverterCollisionException;
import com.googlecode.transmuter.exception.ConverterRegistrationException;
import com.googlecode.transmuter.exception.NoCompatibleConvertersFoundException;
import com.googlecode.transmuter.exception.TooManyConvertersFoundException;
import com.googlecode.transmuter.type.TypeToken;
import com.googlecode.transmuter.util.exception.MultipleCausesException;

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
  protected static class ConverterMap extends HashMap<ConverterType, Binding> {
    private static final long serialVersionUID = 1L;

    public ConverterMap() { /* empty block */ }

    /**
     * Validates the converter type and the binding (using {@link #validatePut(ConverterType, Binding) validatePut}) 
     * before insertion, throwing an exception if a problem is found.
     * <p>
     * In particular, bindings cannot be overwritten; they must be specifically removed from this map before a new 
     * {@code put} operation with the given converter type can be done.
     * 
     * @return {@code null} if there was no previous binding for {@code converterType}, or {@code binding} if it was 
     * already associated with {@code converterType}.
     * @throws RuntimeException all exceptions thrown by {@link #validatePut(ConverterType, Binding)}. 
     * @see #validatePut(ConverterType, Binding)
     */
    @Override
    public Binding put(ConverterType converterType, Binding binding) {
      return validatePut(converterType, binding) 
           ? binding
           : super.put(converterType, binding);
    }

    /**
     * Checks if the converter type and the binding can be stored in this map.
     * <p>
     * The restrictions implemented here are:
     * <ul>
     * <li>neither {@code converterType} nor {@code binding} can be {@code null}.</li>
     * <li>a converter type must be obtainable from {@code binding}.</li>
     * <li>{@code converterType} must be assignable from {@code binding}'s converter type.</li>
     * <li>this map must not have {@code converterType} associated to a binding other than {@code binding}.</li>
     * </ul>
     * 
     * @param converterType a converter type.
     * @param binding a binding.
     * @return {@code true} if {@code converterType} is already associated with {@code binding}, or {@code false} 
     * if {@code converterType} is not associated to a binding here.
     * @throws IllegalArgumentException if either {@code converterType} or {@code binding} are {@code null}.
     * @throws ConverterTypeIncompatibleWithBindingException if {@code converterType} and {@code binding} are not 
     * compatible.
     * @throws ConverterTypeInstantiationException if a converter type cannot be obtained from {@code binding}.
     * @throws ConverterCollisionException if this map already has a different binding associated to 
     * {@code converterType}.
     * @see #checkForCompatibility(ConverterType, Binding)
     * @see #checkForCollision(ConverterType, Binding)
     */
    protected boolean validatePut(ConverterType converterType, Binding binding) {
      // check if the binding matches the converter type
      checkForCompatibility(converterType, binding);
      
      // check for collisions here
      return checkForCollision(converterType, binding);
    }

    /**
     * Checks if the converter type and the binding are mutually compatible.
     * <p>
     * The restrictions are:
     * <ul>
     * <li>neither {@code converterType} nor {@code binding} can be {@code null}.</li>
     * <li>a converter type must be obtainable using {@code binding}.</li>
     * <li>{@code converterType} must be assignable from {@code binding}'s converter type.</li>
     * </ul>
     * 
     * @param converterType a converter type.
     * @param binding a binding.
     * @throws IllegalArgumentException if either {@code converterType} or {@code binding} are {@code null}.
     * @throws ConverterTypeIncompatibleWithBindingException if {@code converterType} and {@code binding} are not 
     * compatible.
     * @throws ConverterTypeInstantiationException if {@code binding} cannot be used to extract a converter type.
     * @see ConverterType#from(Binding)
     * @see ConverterType#isAssignableFrom(ConverterType)
     */
    protected void checkForCompatibility(ConverterType converterType, Binding binding) 
    throws ConverterTypeIncompatibleWithBindingException {
      nonNull(converterType, "converterType"); 
      nonNull(binding, "binding");
      
      if(! converterType.isAssignableFrom(ConverterType.from(binding)))
        throw new ConverterTypeIncompatibleWithBindingException(converterType, binding);
    }

    /**
     * Checks if the converter type and the binding can be stored in this map.
     * 
     * @param converterType a converter type.
     * @param binding a binding.
     * @return {@code true} if {@code converterType} is already associated with {@code binding} in this map, or 
     * {@code false} if there's no binding.
     * @throws IllegalArgumentException if either {@code converterType}, {@code binding} or {@code map} are 
     * {@code null}.
     * @throws ConverterCollisionException if this map already has a different binding associated to 
     * {@code converterType}.
     * @see #checkMapForCollision(ConverterType, Binding, Map)
     */
    protected boolean checkForCollision(ConverterType converterType, Binding binding) 
    throws ConverterCollisionException {
      return checkMapForCollision(converterType, binding, this);
    }
    
    /**
     * Checks if the converter type and the binding can be stored in the map.
     * <p>
     * The restrictions are:
     * <ul>
     * <li>neither {@code converterType} nor {@code binding} nor {@code map} can be {@code null}.</li>
     * <li>{@code map} must not have {@code converterType} associated to a binding different than {@code binding}.</li>
     * </ul>
     * 
     * @param converterType a converter type.
     * @param binding a binding.
     * @param map a map.
     * @return {@code true} if {@code converterType} is already associated with {@code binding} in {@code map}, or 
     * {@code false} if there's no binding in {@code map}.
     * @throws IllegalArgumentException if either {@code converterType}, {@code binding} or {@code map} are 
     * {@code null}.
     * @throws ConverterCollisionException if this map already has a different binding associated to the converter type.
     */
    protected static boolean checkMapForCollision(ConverterType converterType, Binding binding, 
        Map<? extends ConverterType, ? extends Binding> map) throws ConverterCollisionException {
      nonNull(converterType, "converterType");
      nonNull(binding, "binding");
      nonNull(map, "map");
      
      if(! map.containsKey(converterType))
        return false;
      
      if(areEqual(binding, map.get(converterType)))
        return true;
      
      // converter collision, throw up
      throw new ConverterCollisionException(converterType, binding, map.get(converterType));
    }
    
    /**
     * Attempts to add the bindings in the given map to this map, doing nothing if the given map is null or empty. 
     * All entries are validated before actual insertion.
     * 
     * @see #validatePut(ConverterType, Binding)
     */
    @Override
    public void putAll(Map<? extends ConverterType, ? extends Binding> map) {
      if(map == null || map.isEmpty())
        return;
      
      for(Entry<? extends ConverterType, ? extends Binding> entry : map.entrySet())
        validatePut(entry.getKey(), entry.getValue());
      
      for(Entry<? extends ConverterType, ? extends Binding> entry : map.entrySet())
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
    public Binding remove(Object key) {
      if(key == null)
        return null;
      
      return super.remove(key);
    }
  }
  
  /**
   * A {@link ConverterMap} which 
   * {@link #checkMapForCollision(ConverterType, Binding, Map) checks for collisions} against itself and a master map. 
   * 
   * @author Humberto S. N. dos Anjos
   */
  protected static class DependentConverterMap extends ConverterMap {
    private static final long serialVersionUID = 1L;

    private Map<? extends ConverterType, ? extends Binding> masterMap;

    /**
     * Creates a new {@link DependentConverterMap} object, which will be backed by {@code masterMap}.
     * 
     * @param masterMap the master map which backs this map.
     * @throws IllegalArgumentException if {@code masterMap} is {@code null}.
     */
    public DependentConverterMap(Map<? extends ConverterType, ? extends Binding> masterMap) {
      this.masterMap = nonNull(masterMap);
    }
    
    /**
     * Checks for collision in this and in the master map.
     */
    @Override
    protected boolean checkForCollision(ConverterType converterType, Binding binding) 
    throws ConverterCollisionException {
      return super.checkForCollision(converterType, binding)
          || super.checkMapForCollision(converterType, binding, getMasterMap());
    }
    
    /**
     * Returns the map which backs this instance.
     * 
     * @return the map which backs this instance.
     */
    public Map<? extends ConverterType, ? extends Binding> getMasterMap() {
      return masterMap;
    }
  }
  
  private Map<ConverterType, Binding> converterMap;
  
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
   * @throws BindingInvocationException if there was an error during the converter's invocation.
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
   * @throws BindingInvocationException if there was an error during the converter's invocation.
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
   * @throws BindingInvocationException if there was an error during the converter's invocation.
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
   * @throws BindingInvocationException if there was an error during the converter's invocation.
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
   * For example, this means that both invocations below:
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
   * @throws BindingInvocationException if there was an error during the converter's invocation.
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
   * @throws BindingInvocationException if there was an error during the converter's invocation.
   */
  protected Object convertRaw(Object from, TypeToken<?> fromType, TypeToken<?> toType) 
  throws NoCompatibleConvertersFoundException, TooManyConvertersFoundException, IllegalArgumentException, 
  BindingInvocationException {
    return getConverterFor(new ConverterType(fromType, toType)).invoke(from);
  }
  
  /**
   * Scans the given object for converter methods, registering them as bindings keyed by their 
   * {@linkplain ConverterType types}. Does nothing if the given object is {@code null}.
   * <p>
   * This method will iterate through all the given object's public methods and, for all methods marked with the 
   * {@link Converts} annotation, it will:
   * <ul>
   * <li>extract the {@linkplain ConverterType converter type};</li>
   * <li>{@linkplain Binding bind} the method with the given object;</li>
   * <li>and {@linkplain DependentConverterMap check} if there is no registered converter with the same type,</li>
   * </ul>
   * registering all the converters in one fell swoop if no problem is found. Non-public methods, even if marked 
   * with {@link Converts}, will be ignored.
   * <p>
   * Errors encountered during the process will be bundled together and thrown as a single exception. In that case, 
   * no converters will be registered, even if they're valid.
   * 
   * @param object an object, hopefully with converter methods.
   * @throws ConverterRegistrationException if there is some error during the operation.
   * @see ConverterType#from(Method, Type)
   * @see Binding#Binding(Object, Method)
   * @see DependentConverterMap
   */
  public void register(Object object) throws ConverterRegistrationException {
    if(object == null)
      return;
    
    Map<ConverterType, Binding> temp = new DependentConverterMap(getConverterMap());
    List<Exception> exceptions = new ArrayList<Exception>();
    
    for(Method method : object.getClass().getMethods()) {
      if(! method.isAnnotationPresent(Converts.class))
        continue;
      
      try {
        temp.put(ConverterType.from(method, object.getClass()), new Binding(object, method));
      } catch (MultipleCausesException e) {
        exceptions.addAll(e.getCauses());
      } catch(Exception e) {
        exceptions.add(e);
      }
    }
    
    if(! exceptions.isEmpty())
      throw new ConverterRegistrationException(exceptions);
    
    try {
      getConverterMap().putAll(temp);
      return;
    } catch (MultipleCausesException e) {
      exceptions.addAll(e.getCauses());
    } catch(Exception e) {
      exceptions.add(e);
    }
    
    // something happened at putAll()
    throw new ConverterRegistrationException(exceptions);
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
    if(fromType == null || TypeToken.ValueType.VOID.matches(fromType)
    || toType == null || TypeToken.ValueType.VOID.matches(toType))
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
    if(fromType == null || TypeToken.ValueType.VOID.matches(fromType)
    || toType == null || TypeToken.ValueType.VOID.matches(toType))
      return;
    
    unregister(new ConverterType(fromType, toType));
  }
  
  /**
   * Unregisters the converter for the given converter type. Does nothing if no such converter exists. 
   * 
   * @param converterType a converter type.
   * @return the binding previously associated with the given converter type, or {@code null} if there was 
   * no such binding.
   */
  public Binding unregister(ConverterType converterType) {
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
  protected Binding getConverterFor(ConverterType converterType) 
  throws NoCompatibleConvertersFoundException, TooManyConvertersFoundException {
    if(converterType == null)
      throw new NoCompatibleConvertersFoundException(converterType);
    
    // TODO determine a search algorithm for a "most compatible" converter type
    // ??? parameterize it?
    Binding converter = getConverterMap().get(converterType);
    if(converter != null)
      return converter;
    
    List<Binding> compatibleBindings = getCompatibleConvertersFor(converterType);
    
    if(compatibleBindings.isEmpty())
      throw new NoCompatibleConvertersFoundException(converterType);
    
    if(compatibleBindings.size() > 1)
      throw new TooManyConvertersFoundException(converterType, compatibleBindings);
    
    return compatibleBindings.get(0);
  }
  
  /**
   * Returns a list of all the compatible bindings found in this instance. Will be empty if the given converter type
   * is {@code null} or has no compatible bindings. 
   * 
   * @param converterType a converter type.
   * @return a list with all the compatible bindings found.  
   */
  protected List<Binding> getCompatibleConvertersFor(ConverterType converterType) {
    List<Binding> compatibleBindings = new ArrayList<Binding>();
    if(converterType == null)
      return compatibleBindings;
    
    for(Entry<ConverterType, Binding> entry : getConverterMap().entrySet()) {
      if(entry.getKey().isAssignableFrom(converterType))
        compatibleBindings.add(entry.getValue());
    }
    
    return compatibleBindings;
  } 
  
  // properties
  /**
   * Returns a map holding all the registered converters, keyed by their converter types. This map is backed by 
   * this instance, i.e. changes made in this transmuter are seen in the map and vice-versa.
   * 
   * @return a map holding all the registered converters, keyed by their converter types and backed by this instance.
   */
  protected Map<ConverterType, Binding> getConverterMap() {
    return converterMap;
  }
}
